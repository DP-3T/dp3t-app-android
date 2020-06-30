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

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import androidx.annotation.NonNull;

class DatabaseThread extends HandlerThread {

	private static DatabaseThread instance;

	private Looper looper;
	private Handler handler;
	private Handler mainHandler;

	static DatabaseThread getInstance(@NonNull Context context) {
		if (instance == null) {
			instance = new DatabaseThread(context);
		}
		return instance;
	}

	private DatabaseThread(Context context) {
		super("DatabaseThread");
		start();

		looper = getLooper();
		handler = new Handler(looper);
		mainHandler = new Handler(context.getMainLooper());
	}

	void post(@NonNull Runnable runnable) {
		handler.post(runnable);
	}

	void onResult(@NonNull Runnable runnable) {
		mainHandler.post(runnable);
	}

}
