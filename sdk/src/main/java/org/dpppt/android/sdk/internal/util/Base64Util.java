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

import android.util.Base64;

public class Base64Util {

	public static String toBase64(byte[] data) {
		return new String(Base64.encode(data, Base64.NO_WRAP));
	}

	public static byte[] fromBase64(String data) {
		return Base64.decode(data, Base64.NO_WRAP);
	}

}
