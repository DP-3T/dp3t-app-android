/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.sdk.internal;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import androidx.core.content.ContextCompat;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.dpppt.android.sdk.TracingStatus.ErrorState;
import org.dpppt.android.sdk.internal.backend.SyncErrorState;
import org.dpppt.android.sdk.internal.gatt.BluetoothServiceStatus;
import org.dpppt.android.sdk.internal.logger.Logger;
import org.dpppt.android.sdk.internal.util.LocationServiceUtil;

public class ErrorHelper {

	private static final String TAG = "ErrorHelper";

	public static Collection<ErrorState> checkTracingErrorStatus(Context context) {
		Set<ErrorState> errors = new HashSet<>();

		if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			errors.add(ErrorState.BLE_NOT_SUPPORTED);
		} else {
			final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
				errors.add(ErrorState.BLE_DISABLED);
			}
		}

		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		boolean batteryOptimizationsDeactivated =
				powerManager == null || powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
		if (!batteryOptimizationsDeactivated) {
			errors.add(ErrorState.BATTERY_OPTIMIZER_ENABLED);
		}

		boolean locationPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
				PackageManager.PERMISSION_GRANTED;
		if (!locationPermissionGranted) {
			errors.add(ErrorState.MISSING_LOCATION_PERMISSION);
		}

		if (!LocationServiceUtil.isLocationEnabled(context)) {
			errors.add(ErrorState.LOCATION_SERVICE_DISABLED);
		}

		if (!AppConfigManager.getInstance(context).getLastSyncNetworkSuccess()) {
			ErrorState syncError = SyncErrorState.getInstance().getSyncError();
			if (syncError == null) {
				Logger.w(TAG, "lost sync error state");
				syncError = ErrorState.SYNC_ERROR_NETWORK;
			}
			errors.add(syncError);
		}

		if (!errors.contains(ErrorState.BLE_DISABLED)) {
			BluetoothServiceStatus bluetoothServiceStatus = BluetoothServiceStatus.getInstance(context);
			switch (bluetoothServiceStatus.getAdvertiseStatus()) {
				case BluetoothServiceStatus.ADVERTISE_OK:
					// ok
					break;
				case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
				case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
					errors.add(ErrorState.BLE_INTERNAL_ERROR);
					break;
				case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
					errors.add(ErrorState.BLE_NOT_SUPPORTED);
					break;
				case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
				case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
				default:
					errors.add(ErrorState.BLE_ADVERTISING_ERROR);
					break;
			}
			switch (bluetoothServiceStatus.getScanStatus()) {
				case BluetoothServiceStatus.SCAN_OK:
					// ok
					break;
				case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:
					errors.add(ErrorState.BLE_INTERNAL_ERROR);
					break;
				case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:
					errors.add(ErrorState.BLE_NOT_SUPPORTED);
					break;
				case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
				case ScanCallback.SCAN_FAILED_ALREADY_STARTED:
				default:
					errors.add(ErrorState.BLE_SCANNER_ERROR);
					break;
			}
		}

		return errors;
	}

}
