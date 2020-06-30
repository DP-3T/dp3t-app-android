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

import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

public class LocationServiceUtil {

	public static Boolean isLocationEnabled(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			return lm != null && lm.isLocationEnabled();
		} else {
			int mode = Settings.Secure.getInt(context.getContentResolver(),
					Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
			return mode != Settings.Secure.LOCATION_MODE_OFF;
		}
	}

}
