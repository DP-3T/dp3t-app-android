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

public class LogEntry {

	private final long time;
	private final LogLevel level;
	private final String tag;
	private final String message;

	public LogEntry(long time, LogLevel level, String tag, String message) {
		this.time = time;
		this.level = level;
		this.tag = tag;
		this.message = message;
	}

	public long getTime() {
		return time;
	}

	public LogLevel getLevel() {
		return level;
	}

	public String getTag() {
		return tag;
	}

	public String getMessage() {
		return message;
	}

}
