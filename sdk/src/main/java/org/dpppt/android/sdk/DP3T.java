/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteException;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import okhttp3.CertificatePinner;
import org.dpppt.android.sdk.backend.ResponseCallback;
import org.dpppt.android.sdk.backend.SignatureException;
import org.dpppt.android.sdk.backend.models.ApplicationInfo;
import org.dpppt.android.sdk.backend.models.ExposeeAuthMethod;
import org.dpppt.android.sdk.backend.models.ExposeeAuthMethodJson;
import org.dpppt.android.sdk.internal.*;
import org.dpppt.android.sdk.internal.backend.CertificatePinning;
import org.dpppt.android.sdk.internal.backend.ServerTimeOffsetException;
import org.dpppt.android.sdk.internal.backend.StatusCodeException;
import org.dpppt.android.sdk.internal.backend.models.ExposeeRequest;
import org.dpppt.android.sdk.internal.crypto.CryptoModule;
import org.dpppt.android.sdk.internal.database.Database;
import org.dpppt.android.sdk.internal.database.models.ExposureDay;
import org.dpppt.android.sdk.internal.logger.Logger;
import org.dpppt.android.sdk.internal.util.DayDate;
import org.dpppt.android.sdk.internal.util.ProcessUtil;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.dpppt.android.sdk.internal.util.Base64Util.toBase64;

public class DP3T {

	private static final String TAG = "DP3T Interface";

	public static final String UPDATE_INTENT_ACTION = "org.dpppt.android.sdk.UPDATE_ACTION";

	private static String appId;

	public static void init(Context context, String appId, PublicKey signaturePublicKey) {
		init(context, appId, false, signaturePublicKey);
	}

	public static void init(Context context, String appId, boolean enableDevDiscoveryMode, PublicKey signaturePublicKey) {
		if (ProcessUtil.isMainProcess(context)) {
			DP3T.appId = appId;
			AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
			appConfigManager.setAppId(appId);
			appConfigManager.setDevDiscoveryModeEnabled(enableDevDiscoveryMode);
			appConfigManager.triggerLoad();

			executeInit(context, signaturePublicKey);
		}
	}

	public static void init(Context context, ApplicationInfo applicationInfo, PublicKey signaturePublicKey) {
		if (ProcessUtil.isMainProcess(context)) {
			DP3T.appId = applicationInfo.getAppId();
			AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
			appConfigManager.setManualApplicationInfo(applicationInfo);

			executeInit(context, signaturePublicKey);
		}
	}

	private static void executeInit(Context context, PublicKey signaturePublicKey) {
		CryptoModule.getInstance(context).init();

		new Database(context).removeOldData();

		SyncWorker.setBucketSignaturePublicKey(signaturePublicKey);

		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		boolean advertising = appConfigManager.isAdvertisingEnabled();
		boolean receiving = appConfigManager.isReceivingEnabled();
		if (advertising || receiving) {
			start(context, advertising, receiving);
		}
	}

	private static void checkInit() throws IllegalStateException {
		if (appId == null) {
			throw new IllegalStateException("You have to call DP3T.init() in your application onCreate()");
		}
	}

	public static void start(Context context) {
		start(context, true, true);
	}

	protected static void start(Context context, boolean advertise, boolean receive) {
		checkInit();
		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		appConfigManager.setAdvertisingEnabled(advertise);
		appConfigManager.setReceivingEnabled(receive);
		long scanInterval = appConfigManager.getScanInterval();
		long scanDuration = appConfigManager.getScanDuration();
		Intent intent = new Intent(context, TracingService.class).setAction(TracingService.ACTION_START);
		intent.putExtra(TracingService.EXTRA_ADVERTISE, advertise);
		intent.putExtra(TracingService.EXTRA_RECEIVE, receive);
		intent.putExtra(TracingService.EXTRA_SCAN_INTERVAL, scanInterval);
		intent.putExtra(TracingService.EXTRA_SCAN_DURATION, scanDuration);
		ContextCompat.startForegroundService(context, intent);
		SyncWorker.startSyncWorker(context);
		BroadcastHelper.sendUpdateBroadcast(context);
	}

	public static boolean isStarted(Context context) {
		checkInit();
		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		return appConfigManager.isAdvertisingEnabled() || appConfigManager.isReceivingEnabled();
	}

	public static void sync(Context context) {
		checkInit();
		try {
			SyncWorker.doSync(context);
		} catch (IOException | StatusCodeException | ServerTimeOffsetException | SQLiteException | SignatureException ignored) {
			// has been handled upstream
		}
	}

	public static TracingStatus getStatus(Context context) {
		checkInit();
		Database database = new Database(context);
		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		Collection<TracingStatus.ErrorState> errorStates = ErrorHelper.checkTracingErrorStatus(context);
		List<ExposureDay> exposureDays = database.getExposureDays();
		InfectionStatus infectionStatus;
		if (appConfigManager.getIAmInfected()) {
			infectionStatus = InfectionStatus.INFECTED;
		} else if (exposureDays.size() > 0) {
			infectionStatus = InfectionStatus.EXPOSED;
		} else {
			infectionStatus = InfectionStatus.HEALTHY;
		}
		return new TracingStatus(
				database.getContacts().size(),
				appConfigManager.isAdvertisingEnabled(),
				appConfigManager.isReceivingEnabled(),
				appConfigManager.getLastSyncDate(),
				infectionStatus,
				exposureDays,
				errorStates
		);
	}

	public static void sendIAmInfected(Context context, Date onset, ExposeeAuthMethod exposeeAuthMethod, ArrayList<String> countryCodeList,
	                                   ResponseCallback<Void> callback) {
		checkInit();

		DayDate onsetDate = new DayDate(onset.getTime());
		ExposeeRequest exposeeRequest = CryptoModule.getInstance(context).getSecretKeyForPublishing(onsetDate, exposeeAuthMethod, countryCodeList);

		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		try {
			appConfigManager.getBackendReportRepository(context).addExposee(exposeeRequest, exposeeAuthMethod,
					new ResponseCallback<Void>() {
						@Override
						public void onSuccess(Void response) {
							appConfigManager.setIAmInfected(true);
							CryptoModule.getInstance(context).reset();
							stop(context);
							callback.onSuccess(response);
						}

						@Override
						public void onError(Throwable throwable) {
							callback.onError(throwable);
						}
					});
		} catch (IllegalStateException e) {
			callback.onError(e);
			Logger.e(TAG, e);
		}
	}

	public static void sendFakeInfectedRequest(Context context, Date onset, ExposeeAuthMethod exposeeAuthMethod)
			throws NoSuchAlgorithmException, IOException {
		checkInit();

		DayDate onsetDate = new DayDate(onset.getTime());
		ExposeeAuthMethodJson jsonAuthMethod = null;
		if (exposeeAuthMethod instanceof ExposeeAuthMethodJson) {
			jsonAuthMethod = (ExposeeAuthMethodJson) exposeeAuthMethod;
		}
		ExposeeRequest exposeeRequest = new ExposeeRequest(toBase64(CryptoModule.getInstance(context).getNewRandomKey()),
				onsetDate.getStartOfDayTimestamp(), 1, jsonAuthMethod, new ArrayList<>());
		AppConfigManager.getInstance(context).getBackendReportRepository(context)
				.addExposeeSync(exposeeRequest, exposeeAuthMethod);
	}

	public static void stop(Context context) {
		checkInit();

		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		appConfigManager.setAdvertisingEnabled(false);
		appConfigManager.setReceivingEnabled(false);

		Intent intent = new Intent(context, TracingService.class).setAction(TracingService.ACTION_STOP);
		context.startService(intent);
		SyncWorker.stopSyncWorker(context);
		BroadcastHelper.sendUpdateBroadcast(context);
	}

	public static void setMatchingParameters(Context context, float contactAttenuationThreshold, int numberOfWindowsForExposure) {
		checkInit();

		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		appConfigManager.setContactAttenuationThreshold(contactAttenuationThreshold);
		appConfigManager.setNumberOfWindowsForExposure(numberOfWindowsForExposure);
	}

	public static void setCertificatePinner(@NonNull CertificatePinner certificatePinner) {
		CertificatePinning.setCertificatePinner(certificatePinner);
	}

	public static IntentFilter getUpdateIntentFilter() {
		return new IntentFilter(DP3T.UPDATE_INTENT_ACTION);
	}

	public static void clearData(Context context, Runnable onDeleteListener) {
		checkInit();
		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		if (appConfigManager.isAdvertisingEnabled() || appConfigManager.isReceivingEnabled()) {
			throw new IllegalStateException("Tracking must be stopped for clearing the local data");
		}

		CryptoModule.getInstance(context).reset();
		appConfigManager.clearPreferences();
		Logger.clear();
		Database db = new Database(context);
		db.recreateTables(response -> onDeleteListener.run());
	}

}
