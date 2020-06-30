/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.sdk.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import java.util.UUID;

import org.dpppt.android.sdk.internal.database.DatabaseHelper;

public class DeviceHelper {

	public static String getDeviceID(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("deviceID", Context.MODE_PRIVATE);
		String id = sharedPreferences.getString("id", null);
		if (id == null) {
			id = UUID.randomUUID().toString();
			sharedPreferences.edit().putString("id", id).apply();
		}
		return id;
	}

	public static void addDeviceInfoToDatabase(Context context) {
		SQLiteDatabase database = DatabaseHelper.getWritableDatabase(context);
		database.execSQL("drop table if exists " + TABLE_NAME);
		database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + MANUFACTURER + " TEXT NOT NULL, " +
				DEVICE + " TEXT NOT NULL, " + MODEL + " TEXT NOT NULL, " +
				BOARD + " TEXT NOT NULL, " + DEVICE_ID + " TEXT NOT NULL, " +
				OSVERSION + " INTEGER NOT NULL)");
		ContentValues insertValues = new ContentValues();
		insertValues.put(MANUFACTURER, Build.MANUFACTURER);
		insertValues.put(DEVICE, Build.DEVICE);
		insertValues.put(MODEL, Build.MODEL);
		insertValues.put(BOARD, Build.BOARD);
		insertValues.put(OSVERSION, Build.VERSION.SDK_INT);
		insertValues.put(DEVICE_ID, getDeviceID(context));
		database.insert(TABLE_NAME, null, insertValues);
	}

	private static final String TABLE_NAME = "device_info";
	private static final String MANUFACTURER = "manufacturor";
	private static final String OSVERSION = "os_version";
	private static final String DEVICE = "device";
	private static final String MODEL = "model";
	private static final String BOARD = "board";
	private static final String DEVICE_ID = "device_id";

}
