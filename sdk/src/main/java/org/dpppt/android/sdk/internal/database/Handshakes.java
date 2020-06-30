/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.sdk.internal.database;

interface Handshakes {

	String TABLE_NAME = "handshakes";

	String ID = "id";
	String TIMESTAMP = "timestamp";
	String EPHID = "ephID";
	String TX_POWER_LEVEL = "tx_power_level";
	String RSSI = "rssi";
	String PHY_PRIMARY = "phy_primary";
	String PHY_SECONDARY = "phy_secondary";
	String TIMESTAMP_NANOS = "timestamp_nanos";

	String[] PROJECTION = {
			ID,
			TIMESTAMP,
			EPHID,
			TX_POWER_LEVEL,
			RSSI,
			PHY_PRIMARY,
			PHY_SECONDARY,
			TIMESTAMP_NANOS
	};

	static String create() {
		return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
				ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
				TIMESTAMP + " INTEGER NOT NULL, " +
				EPHID + " BLOB NOT NULL, " +
				TX_POWER_LEVEL + " INTEGER, " +
				RSSI + " INTEGER," +
				PHY_PRIMARY + " TEXT," +
				PHY_SECONDARY + " TEXT," +
				TIMESTAMP_NANOS + " INTEGER" +
				")";
	}

	static String drop() {
		return "DROP TABLE IF EXISTS " + TABLE_NAME;
	}

}
