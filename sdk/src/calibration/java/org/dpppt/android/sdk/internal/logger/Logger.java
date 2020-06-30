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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Logger {

	private static Logger instance = null;

	private final LogLevel minLevel;
	private final LogDatabase database;

	public static void init(Context context, LogLevel level) {
		instance = new Logger(context, level);
	}

	private Logger(Context context, LogLevel level) {
		this.minLevel = level;
		this.database = new LogDatabase(context);
	}

	public static void d(String tag, String message) {
		if (instance != null) {
			instance.log(LogLevel.DEBUG, tag, message);
		}
	}

	public static void i(String tag, String message) {
		if (instance != null) {
			instance.log(LogLevel.INFO, tag, message);
		}
	}

	public static void w(String tag, String message) {
		if (instance != null) {
			instance.log(LogLevel.WARNING, tag, message);
		}
	}

	public static void e(String tag, String message) {
		if (instance != null) {
			instance.log(LogLevel.ERROR, tag, message);
		}
	}

	public static void e(String tag, Throwable throwable) {
		StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		e(tag, sw.toString());
	}

	public static List<LogEntry> getLogs(long sinceTime) {
		if (instance != null) {
			return instance.database.getLogsSince(sinceTime);
		} else {
			return new ArrayList<>();
		}
	}

	public static void clear() {
		if (instance != null) {
			instance.database.clear();
		}
	}

	public static List<String> getTags() {
		if (instance != null) {
			return instance.database.getTags();
		}
		return Collections.emptyList();
	}

	private void log(LogLevel level, String tag, String message) {
		if (level.getImportance() < minLevel.getImportance()) {
			return;
		}

		level.getLogcat().log(tag, message);

		database.log(level.getKey(), tag, message, System.currentTimeMillis());
	}

}
