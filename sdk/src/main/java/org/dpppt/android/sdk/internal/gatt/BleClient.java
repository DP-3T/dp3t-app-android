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
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.BroadcastHelper;
import org.dpppt.android.sdk.internal.crypto.CryptoModule;
import org.dpppt.android.sdk.internal.crypto.EphId;
import org.dpppt.android.sdk.internal.database.Database;
import org.dpppt.android.sdk.internal.database.models.Handshake;
import org.dpppt.android.sdk.internal.logger.Logger;

import static org.dpppt.android.sdk.internal.gatt.BleServer.SERVICE_UUID;

public class BleClient {

	private static final String TAG = "BleClient";

	private final Context context;
	private BluetoothLeScanner bleScanner;
	private ScanCallback bleScanCallback;
	private GattConnectionThread gattConnectionThread;

	private HashMap<String, List<Handshake>> scanResultMap = new HashMap<>();
	private HashMap<String, EphId> connectedEphIdMap = new HashMap<>();

	public BleClient(Context context) {
		this.context = context;
		gattConnectionThread = new GattConnectionThread();
		gattConnectionThread.start();
	}

	public BluetoothState start() {
		final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
			BroadcastHelper.sendErrorUpdateBroadcast(context);
			return bluetoothAdapter == null ? BluetoothState.NOT_SUPPORTED : BluetoothState.DISABLED;
		}
		bleScanner = bluetoothAdapter.getBluetoothLeScanner();
		if (bleScanner == null) {
			return BluetoothState.NOT_SUPPORTED;
		}

		List<ScanFilter> scanFilters = new ArrayList<>();
		scanFilters.add(new ScanFilter.Builder()
				.setServiceUuid(new ParcelUuid(SERVICE_UUID))
				.build());

		// Scan for Apple devices as iOS does not advertise service uuid when in background,
		// but instead pushes it to the "overflow" area (manufacturer data). For now let's
		// connect to all Apple devices until we find the algorithm used to put the service uuid
		// into the manufacturer data
		scanFilters.add(new ScanFilter.Builder()
				.setManufacturerData(0x004c, new byte[0])
				.build());

		ScanSettings.Builder settingsBuilder = new ScanSettings.Builder()
				.setScanMode(AppConfigManager.getInstance(context).getBluetoothScanMode().getSystemValue())
				.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
				.setReportDelay(0)
				.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
				.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			settingsBuilder
					.setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
					.setLegacy(true);
		}
		ScanSettings scanSettings = settingsBuilder.build();

		BluetoothServiceStatus bluetoothServiceStatus = BluetoothServiceStatus.getInstance(context);

		bleScanCallback = new ScanCallback() {
			private static final String TAG = "ScanCallback";

			public void onScanResult(int callbackType, ScanResult result) {
				bluetoothServiceStatus.updateScanStatus(BluetoothServiceStatus.SCAN_OK);
				if (result.getScanRecord() != null) {
					onDeviceFound(result);
				}
			}

			@Override
			public void onBatchScanResults(List<ScanResult> results) {
				bluetoothServiceStatus.updateScanStatus(BluetoothServiceStatus.SCAN_OK);
				Logger.d(TAG, "Batch size " + results.size());
				for (ScanResult result : results) {
					onScanResult(0, result);
				}
			}

			public void onScanFailed(int errorCode) {
				bluetoothServiceStatus.updateScanStatus(errorCode);
				Logger.e(TAG, "error: " + errorCode);
			}
		};

		bleScanner.startScan(scanFilters, scanSettings, bleScanCallback);
		Logger.i(TAG, "started BLE scanner, scanMode: " + scanSettings.getScanMode() + " scanFilters: " + scanFilters.size());

		return BluetoothState.ENABLED;
	}

	private void onDeviceFound(ScanResult scanResult) {
		try {
			BluetoothDevice bluetoothDevice = scanResult.getDevice();
			final String deviceAddr = bluetoothDevice.getAddress();

			int power = scanResult.getScanRecord().getTxPowerLevel();
			if (power == Integer.MIN_VALUE) {
				Logger.d(TAG, "No power levels found for " + deviceAddr + ", use default of 12dbm");
				power = 12;
			}

			List<Handshake> handshakesForDevice = scanResultMap.get(deviceAddr);
			if (handshakesForDevice == null) {
				handshakesForDevice = new ArrayList<>();
				scanResultMap.put(deviceAddr, handshakesForDevice);
			}

			byte[] payload = scanResult.getScanRecord().getServiceData(new ParcelUuid(SERVICE_UUID));
			boolean correctPayload = payload != null && payload.length == CryptoModule.EPHID_LENGTH;
			Logger.d(TAG, "found " + deviceAddr + "; power: " + power + "; rssi: " + scanResult.getRssi() +
					"; haspayload: " + correctPayload);
			if (correctPayload) {
				// if Android, optimize (meaning: send/read payload directly in the advertisement
				Logger.i(TAG, "handshake with " + deviceAddr + " (servicedata payload)");
				handshakesForDevice.add(createHandshake(new EphId(payload), scanResult, power));
			} else {
				if (handshakesForDevice.isEmpty()) {
					gattConnectionThread.addTask(new GattConnectionTask(context, bluetoothDevice, scanResult,
							(ephId, device) -> {
								connectedEphIdMap.put(device.getAddress(), ephId);
								Logger.i(TAG, "handshake with " + device.getAddress() + " (gatt connection)");
							}));
				}
				handshakesForDevice.add(createHandshake(null, scanResult, power));
			}
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
	}

	private Handshake createHandshake(EphId ephId, ScanResult scanResult, int power) {
		return new Handshake(-1, System.currentTimeMillis(), ephId, power, scanResult.getRssi(),
				BleCompat.getPrimaryPhy(scanResult), BleCompat.getSecondaryPhy(scanResult),
				scanResult.getTimestampNanos());
	}

	public synchronized void stopScan() {
		final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
			bleScanner = null;
			BroadcastHelper.sendErrorUpdateBroadcast(context);
			return;
		}
		if (bleScanner != null) {
			Logger.i(TAG, "stopping BLE scanner");
			bleScanner.stopScan(bleScanCallback);
			bleScanner = null;
		}
	}

	public synchronized void stop() {
		gattConnectionThread.terminate();
		stopScan();

		Database database = new Database(context);
		for (Map.Entry<String, List<Handshake>> entry : scanResultMap.entrySet()) {
			String device = entry.getKey();
			List<Handshake> handshakes = scanResultMap.get(device);
			if (connectedEphIdMap.containsKey(device)) {
				EphId ephId = connectedEphIdMap.get(device);
				for (Handshake handshake : handshakes) {
					handshake.setEphId(ephId);
					database.addHandshake(context, handshake);
				}
			} else {
				for (Handshake handshake : handshakes) {
					if (handshake.getEphId() != null) {
						database.addHandshake(context, handshake);
					}
				}
			}
		}
	}

}
