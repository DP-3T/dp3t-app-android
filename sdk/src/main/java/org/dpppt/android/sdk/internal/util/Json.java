/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.dpppt.android.sdk.internal.util;

import androidx.core.util.Supplier;

import com.google.gson.Gson;

public final class Json {

	private static final Gson GSON = new Gson();

	public static <T> String toJson(T object) {
		return GSON.toJson(object);
	}

	public static <T> T safeFromJson(String json, Class<T> classOfT, Supplier<T> defaultValue) {
		try {
			return GSON.fromJson(json, classOfT);
		} catch (Exception e) {
			return defaultValue.get();
		}
	}

	private Json() { }

}
