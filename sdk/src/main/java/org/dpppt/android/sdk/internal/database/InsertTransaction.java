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

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;

public class InsertTransaction implements Runnable {

	private SQLiteDatabase db;
	private String tableName;
	private ContentValues values;

	InsertTransaction(@NonNull SQLiteDatabase db, @NonNull String tableName, @NonNull ContentValues values) {
		this.db = db;
		this.tableName = tableName;
		this.values = values;
	}

	@Override
	public void run() {
		db.beginTransaction();
		try {
			db.insert(tableName, null, values);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			// anything
		} finally {
			db.endTransaction();
		}
	}

}
