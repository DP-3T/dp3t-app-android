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

import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;

public class Transaction implements Runnable {

	private SQLiteDatabase db;
	private String[] queries;

	Transaction(@NonNull SQLiteDatabase db, String... queries) {
		this.db = db;
		this.queries = queries;
	}

	@Override
	public void run() {
		db.beginTransaction();
		try {
			for (String query : queries) {
				db.execSQL(query);
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			// anything
		} finally {
			db.endTransaction();
		}
	}

}
