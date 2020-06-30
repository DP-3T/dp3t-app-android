/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.dpppt.android.sdk.internal.logger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.dpppt.android.sdk.internal.database.DatabaseHelper;

public class LogDatabaseHelper {

	public static void copyLogDatabase(Context context) {
		SQLiteDatabase database = DatabaseHelper.getWritableDatabase(context);
		database.beginTransaction();
		database.execSQL("drop table if exists " + LogDatabase.LogSpec.TABLE_NAME);
		LogDatabase.LogDatabaseHelper.executeCreate(database);
		for (LogEntry logEntry : Logger.getLogs(0)) {
			LogDatabase
					.insert(database, logEntry.getLevel().getKey(), logEntry.getTag(), logEntry.getMessage(), logEntry.getTime());
		}
		database.setTransactionSuccessful();
		database.endTransaction();
	}

}
