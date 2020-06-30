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

import java.io.IOException;
import java.text.ParseException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

class DayDateJsonAdapter extends TypeAdapter<DayDate> {

	@Override
	public void write(JsonWriter out, DayDate value) throws IOException {
		out.value(value.formatAsString());
	}

	@Override
	public DayDate read(JsonReader in) throws IOException {
		String value = in.nextString();
		try {
			return new DayDate(value);
		} catch (ParseException e) {
			throw new IOException("Unexpected DayDate format " + value, e);
		}
	}

}
