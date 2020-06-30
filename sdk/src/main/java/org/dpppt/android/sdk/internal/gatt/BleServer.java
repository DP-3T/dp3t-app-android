/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.sdk.internal.gatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;

import java.util.UUID;

import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.crypto.CryptoModule;
import org.dpppt.android.sdk.internal.logger.Logger;

public class BleServer {

	private static final String TAG = "BleServer";

	private static final String DP3T_16_BIT_UUID = "FD68";
	public static final UUID SERVICE_UUID = UUID.fromString("0000" + DP3T_16_BIT_UUID + "-0000-1000-8000-00805F9B34FB");
	public static final UUID TOTP_CHARACTERISTIC_UUID = UUID.fromString("8c8494e3-bab5-1848-40a0-1b06991c0001");

	private final Context context;
	private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
		@Override
		public void onStartFailure(int errorCode) {
			Logger.e(TAG, "advertise onStartFailure: " + errorCode);
			BluetoothServiceStatus.getInstance(context).updateAdvertiseStatus(errorCode);
		}

		@Override
		public void onStartSuccess(AdvertiseSettings settingsInEffect) {
			Logger.i(TAG, "advertise onStartSuccess: " + settingsInEffect.toString());
			BluetoothServiceStatus.getInstance(context).updateAdvertiseStatus(BluetoothServiceStatus.ADVERTISE_OK);
		}
	};
	private BluetoothAdapter mAdapter;
	private BluetoothLeAdvertiser mLeAdvertiser;

	public BleServer(Context context) {
		this.context = context;
	}

	private byte[] getAdvertiseData() {
		CryptoModule cryptoModule = CryptoModule.getInstance(context);
		byte[] advertiseData = cryptoModule.getCurrentEphId().getData();
		String calibrationTestDeviceName = AppConfigManager.getInstance(context).getCalibrationTestDeviceName();
		if (calibrationTestDeviceName != null) {
			byte[] nameBytes = calibrationTestDeviceName.getBytes();
			for (int i = 0; i < AppConfigManager.CALIBRATION_TEST_DEVICE_NAME_LENGTH; i++) {
				advertiseData[i] = nameBytes[i];
			}
			long curMinInEpoch = ((System.currentTimeMillis() - cryptoModule.getCurrentEpochStart()) / (60 * 1000));
			byte[] minData = Long.toString(curMinInEpoch).getBytes();
			advertiseData[AppConfigManager.CALIBRATION_TEST_DEVICE_NAME_LENGTH] = minData[0];
			if (minData.length > 1) {
				advertiseData[AppConfigManager.CALIBRATION_TEST_DEVICE_NAME_LENGTH + 1] = minData[1];
			}
		}
		return advertiseData;
	}

	public BluetoothState startAdvertising() {
		BluetoothManager mManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

		if (mManager == null || !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			return BluetoothState.NOT_SUPPORTED;
		}

		mAdapter = mManager.getAdapter();
		mLeAdvertiser = mAdapter.getBluetoothLeAdvertiser();
		if (mLeAdvertiser == null) {
			return BluetoothState.NOT_SUPPORTED;
		}

		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);

		AdvertiseSettings.Builder settingBuilder = new AdvertiseSettings.Builder();
		settingBuilder.setAdvertiseMode(appConfigManager.getBluetoothAdvertiseMode().getSystemValue());
		settingBuilder.setTxPowerLevel(appConfigManager.getBluetoothTxPowerLevel().getSystemValue());
		settingBuilder.setConnectable(true);
		settingBuilder.setTimeout(0);
		AdvertiseSettings settings = settingBuilder.build();

		AdvertiseData.Builder advBuilder = new AdvertiseData.Builder();
		advBuilder.setIncludeTxPowerLevel(true);
		advBuilder.setIncludeDeviceName(false);
		advBuilder.addServiceUuid(new ParcelUuid(SERVICE_UUID));

		if (appConfigManager.isScanResponseEnabled()) {
			AdvertiseData.Builder scanResponse = new AdvertiseData.Builder();
			scanResponse.setIncludeTxPowerLevel(false);
			scanResponse.setIncludeDeviceName(false);
			scanResponse.addServiceData(new ParcelUuid(SERVICE_UUID), getAdvertiseData());
			mLeAdvertiser.startAdvertising(settings, advBuilder.build(), scanResponse.build(), advertiseCallback);
			Logger.d(TAG, "started advertising (with scanResponse), advertiseMode " + settings.getMode() + " powerLevel " +
					settings.getTxPowerLevel());
		} else {
			advBuilder.addServiceData(new ParcelUuid(SERVICE_UUID), getAdvertiseData());
			mLeAdvertiser.startAdvertising(settings, advBuilder.build(), advertiseCallback);
			Logger.d(TAG, "started advertising (only advertiseData), advertiseMode " + settings.getMode() + " powerLevel " +
					settings.getTxPowerLevel());
		}

		return BluetoothState.ENABLED;
	}

	public void stopAdvertising() {
		if (mLeAdvertiser != null) {
			mLeAdvertiser.stopAdvertising(advertiseCallback);
		}
	}

	public void stop() {
		stopAdvertising();
		mAdapter = null;
	}

}
