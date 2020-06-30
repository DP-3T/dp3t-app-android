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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.os.Build;

public class BleCompat {

	public static String getPrimaryPhy(ScanResult scanResult) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			return getPhyString(scanResult.getPrimaryPhy());
		} else {
			return "-";
		}
	}

	public static String getSecondaryPhy(ScanResult scanResult) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			return getPhyString(scanResult.getSecondaryPhy());
		} else {
			return "-";
		}
	}

	private static String getPhyString(int phy) {
		switch (phy) {
			case BluetoothDevice.PHY_LE_1M: return "1M";
			case BluetoothDevice.PHY_LE_2M: return "2M";
			case BluetoothDevice.PHY_LE_CODED: return "coded";
			case ScanResult.PHY_UNUSED: return "unused";
		}
		return "?";
	}

}
