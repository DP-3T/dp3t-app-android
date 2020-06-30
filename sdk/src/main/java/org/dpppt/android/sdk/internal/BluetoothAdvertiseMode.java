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

import android.bluetooth.le.AdvertiseSettings;

public enum BluetoothAdvertiseMode {
	ADVERTISE_MODE_LOW_POWER(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER),
	ADVERTISE_MODE_BALANCED(AdvertiseSettings.ADVERTISE_MODE_BALANCED),
	ADVERTISE_MODE_LOW_LATENCY(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);

	private final int systemValue;

	BluetoothAdvertiseMode(final int systemValue) {
		this.systemValue = systemValue;
	}

	public int getSystemValue() {
		return systemValue;
	}
}
