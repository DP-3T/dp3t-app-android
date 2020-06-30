/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.sdk.internal.logger;

import android.content.Context;

import java.util.Collections;
import java.util.List;

public class Logger {

	public static void init(Context context, LogLevel level) {
	}

	public static void d(String tag, String message) {
		//ignore for production
	}

	public static void i(String tag, String message) {
		//ignore for production
	}

	public static void w(String tag, String message) {
		//ignore for production
	}

	public static void e(String tag, String message) {
		//ignore for production
	}

	public static void e(String tag, Throwable throwable) {
		//ignore for production
	}

	public static void clear() {
		//ignore for production
	}

	public static List<String> getTags() {
		//ignore for production
		return Collections.emptyList();
	}

}
