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

import android.bluetooth.*;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;

import java.util.Arrays;

import org.dpppt.android.sdk.internal.crypto.EphId;
import org.dpppt.android.sdk.internal.logger.Logger;

import static org.dpppt.android.sdk.internal.crypto.CryptoModule.EPHID_LENGTH;

public class GattConnectionTask {

	private static final String TAG = "GattConnectionTask";

	private static final long GATT_READ_TIMEOUT = 10 * 1000L;

	private Context context;
	private BluetoothDevice bluetoothDevice;
	private ScanResult scanResult;
	private Callback callback;

	private BluetoothGatt bluetoothGatt;
	private long startTime;

	public GattConnectionTask(Context context, BluetoothDevice bluetoothDevice, ScanResult scanResult, Callback callback) {
		this.context = context;
		this.bluetoothDevice = bluetoothDevice;
		this.scanResult = scanResult;
		this.callback = callback;
	}

	public void execute() {
		Logger.d(TAG, "Connecting GATT to: " + bluetoothDevice.getAddress());

		final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

			@Override
			public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
				super.onConnectionStateChange(gatt, status, newState);
				if (newState == BluetoothProfile.STATE_CONNECTING) {
					Logger.d(TAG, "connecting... " + status);
				} else if (newState == BluetoothProfile.STATE_CONNECTED) {
					Logger.d(TAG, "connected " + status);
					Logger.d(TAG, "requesting mtu...");
					gatt.requestMtu(512);
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED || newState == BluetoothProfile.STATE_DISCONNECTING) {
					Logger.d(TAG, "Gatt Connection disconnected " + status);
					finish();
				}
			}

			@Override
			public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
				Logger.d(TAG, "discovering services...");
				gatt.discoverServices();
			}

			@Override
			public void onServicesDiscovered(BluetoothGatt gatt, int status) {
				BluetoothGattService service = gatt.getService(BleServer.SERVICE_UUID);

				if (service == null) {
					Logger.d(TAG, "No GATT service for " + BleServer.SERVICE_UUID + " found, status=" + status);
					finish();
					return;
				}

				Logger.i(TAG, "Service " + service.getUuid() + " found");

				BluetoothGattCharacteristic characteristic = service.getCharacteristic(BleServer.TOTP_CHARACTERISTIC_UUID);

				boolean initiatedRead = gatt.readCharacteristic(characteristic);
				if (!initiatedRead) {
					Logger.e(TAG, "Failed to initiate characteristic read");
				} else {
					Logger.i(TAG, "Read initiated");
				}
			}

			@Override
			public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
				Logger.i(TAG, "onCharacteristicRead [status:" + status + "] " + characteristic.getUuid() + ": " +
						Arrays.toString(characteristic.getValue()));

				if (characteristic.getUuid().equals(BleServer.TOTP_CHARACTERISTIC_UUID)) {
					if (status == BluetoothGatt.GATT_SUCCESS) {
						if (characteristic.getValue().length == EPHID_LENGTH) {
							callback.onEphIdRead(new EphId(characteristic.getValue()), gatt.getDevice());
							scanResult.getRssi();
						} else {
							Logger.e(TAG, "got wrong sized ephid " + characteristic.getValue().length);
						}
					} else {
						Logger.w(TAG, "Failed to read characteristic. Status: " + status);
						// TODO error
					}
				}
				finish();
				Logger.d(TAG, "Closed Gatt Connection");
			}
		};

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			bluetoothGatt = bluetoothDevice.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
		} else {
			bluetoothGatt = bluetoothDevice.connectGatt(context, false, gattCallback);
		}

		startTime = System.currentTimeMillis();
	}

	public void checkForTimeout() {
		if (System.currentTimeMillis() - startTime > GATT_READ_TIMEOUT) {
			Logger.d(TAG, "timeout");
			finish();
		}
	}

	public boolean isFinished() {
		return bluetoothGatt == null;
	}

	public synchronized void finish() {
		if (bluetoothGatt != null) {
			Logger.d(TAG, "disconnect() and close(): " + bluetoothGatt.getDevice().getAddress());
			// Order matters! Call disconnect() before close() as the latter de-registers our client
			// and essentially makes disconnect a NOP.
			bluetoothGatt.disconnect();
			bluetoothGatt.close();
			bluetoothGatt = null;
		}
		Logger.d(TAG, "Reset and wait for next BLE device");
	}


	public interface Callback {

		void onEphIdRead(EphId ephId, BluetoothDevice device);

	}

}
