/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.sdk.internal.database.models;

import org.dpppt.android.sdk.internal.crypto.EphId;

public class Handshake {

	private int id;
	private long timestamp;
	private EphId ephId;
	private int txPowerLevel;
	private int rssi;
	private String primaryPhy;
	private String secondaryPhy;
	private long timestampNanos;

	public Handshake(int id, long timestamp, EphId ephId, int txPowerLevel, int rssi, String primaryPhy, String secondaryPhy,
					 long timestampNanos) {
		this.id = id;
		this.timestamp = timestamp;
		this.ephId = ephId;
		this.txPowerLevel = txPowerLevel;
		this.rssi = rssi;

		this.primaryPhy = primaryPhy;
		this.secondaryPhy = secondaryPhy;
		this.timestampNanos = timestampNanos;
	}

	public EphId getEphId() {
		return ephId;
	}

	public void setEphId(EphId ephId) {
		this.ephId = ephId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getTxPowerLevel() {
		return txPowerLevel;
	}

	public int getRssi() {
		return rssi;
	}

	public String getPrimaryPhy() {
		return primaryPhy;
	}

	public String getSecondaryPhy() {
		return secondaryPhy;
	}

	public long getTimestampNanos() {
		return timestampNanos;
	}

	public int getAttenuation() {
		return txPowerLevel - rssi;
	}

}
