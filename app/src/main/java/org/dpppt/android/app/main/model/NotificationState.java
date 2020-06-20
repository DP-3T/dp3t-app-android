/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.dpppt.android.app.main.model;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import org.dpppt.android.app.R;


public enum NotificationState {
	NO_REPORTS,
	EXPOSED,
	POSITIVE_TESTED;

	@StringRes public static int getTitle(NotificationState notificationState) {
		switch (notificationState) {
			case NO_REPORTS:
				return R.string.meldungen_no_meldungen_title;
			case EXPOSED:
				return R.string.meldungen_meldung_title;
			case POSITIVE_TESTED:
				return R.string.meldung_homescreen_positiv_title;
		}
		return -1;
	}

	@StringRes public static int getText(NotificationState NotificationState) {
		switch (NotificationState) {
			case NO_REPORTS:
				return R.string.meldungen_no_meldungen_subtitle;
			case EXPOSED:
				return R.string.meldungen_meldung_text;
			case POSITIVE_TESTED:
				return R.string.meldung_homescreen_positiv_text;
		}
		return -1;
	}

	@DrawableRes public static int getIcon(NotificationState notificationState) {
		switch (notificationState) {
			case NO_REPORTS:
				return R.drawable.ic_check;
			case EXPOSED:
				return R.drawable.ic_info;
			case POSITIVE_TESTED:
				return R.drawable.ic_info;
		}
		return -1;
	}

	@ColorRes public static int getTitleTextColor(NotificationState notificationState) {
		switch (notificationState) {
			case NO_REPORTS:
				return R.color.green_main;
			case EXPOSED:
				return R.color.white;
			case POSITIVE_TESTED:
				return R.color.white;
		}
		return -1;
	}

	@ColorRes public static int geTextColor(NotificationState notificationState) {
		switch (notificationState) {
			case NO_REPORTS:
				return R.color.dark_main;
			case EXPOSED:
				return R.color.white;
			case POSITIVE_TESTED:
				return R.color.white;
		}
		return -1;
	}

	@ColorRes public static int getBackgroundColor(NotificationState notificationState) {
		switch (notificationState) {
			case NO_REPORTS:
				return R.color.status_green_bg;
			case EXPOSED:
				return R.color.blue_main;
			case POSITIVE_TESTED:
				return R.color.purple_main;
		}
		return -1;
	}

	public static int getIllu(NotificationState state) {
		switch (state) {
			case NO_REPORTS:
				return R.drawable.ill_no_message;
			case EXPOSED:
			case POSITIVE_TESTED:
			default:
				return -1;
		}
	}
}
