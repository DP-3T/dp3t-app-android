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

import android.bluetooth.le.ScanSettings;

public enum BluetoothScanMode {
	SCAN_MODE_LOW_POWER(ScanSettings.SCAN_MODE_LOW_POWER),
	SCAN_MODE_BALANCED(ScanSettings.SCAN_MODE_BALANCED),
	SCAN_MODE_LOW_LATENCY(ScanSettings.SCAN_MODE_LOW_LATENCY),
	SCAN_MODE_OPPORTUNISTIC(ScanSettings.SCAN_MODE_OPPORTUNISTIC);

	private final int systemValue;

	BluetoothScanMode(final int systemValue) {
		this.systemValue = systemValue;
	}

	public int getSystemValue() {
		return systemValue;
	}
}
