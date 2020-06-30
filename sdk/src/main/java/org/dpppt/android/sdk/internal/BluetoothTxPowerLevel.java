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

public enum BluetoothTxPowerLevel {
	ADVERTISE_TX_POWER_ULTRA_LOW(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW),
	ADVERTISE_TX_POWER_LOW(AdvertiseSettings.ADVERTISE_TX_POWER_LOW),
	ADVERTISE_TX_POWER_MEDIUM(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM),
	ADVERTISE_TX_POWER_HIGH(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);

	private final int systemValue;

	BluetoothTxPowerLevel(final int systemValue) {
		this.systemValue = systemValue;
	}

	public int getSystemValue() {
		return systemValue;
	}
}
