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

interface ExposureDays {

	String TABLE_NAME = "exposure_days";

	String ID = "id";
	String EXPOSED_DATE = "exposed_date";
	String REPORT_DATE = "report_date";

	String[] PROJECTION = {
			ID,
			EXPOSED_DATE,
			REPORT_DATE
	};

	static String create() {
		return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
				EXPOSED_DATE + " INTEGER NOT NULL, " +
				REPORT_DATE + " INTEGER NOT NULL, " +
				"UNIQUE (" + EXPOSED_DATE + ") )";
	}

	static String drop() {
		return "DROP TABLE IF EXISTS " + TABLE_NAME;
	}

}
