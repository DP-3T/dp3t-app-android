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


public enum TracingState {
	ACTIVE,
	NOT_ACTIVE,
	ENDED;

	public static @StringRes
	int getTitle(TracingState tracingState) {
		switch (tracingState) {
			case ACTIVE:
				return R.string.tracing_active_title;
			case NOT_ACTIVE:
				return R.string.tracing_turned_off_title;
			case ENDED:
				return R.string.tracing_ended_title;
		}
		return -1;
	}

	public static @StringRes
	int getText(TracingState tracingState) {
		switch (tracingState) {
			case ACTIVE:
				return R.string.tracing_active_text;
			case NOT_ACTIVE:
				return R.string.tracing_turned_off_text;
			case ENDED:
				return R.string.tracing_ended_text;
		}
		return -1;
	}

	public static @DrawableRes
	int getIcon(TracingState tracingState) {
		switch (tracingState) {
			case ACTIVE:
				return R.drawable.ic_check;
			case NOT_ACTIVE:
				return R.drawable.ic_warning_red;
			case ENDED:
				return R.drawable.ic_stopp;
		}
		return -1;
	}

	public static @ColorRes
	int getTextColor(TracingState tracingState) {
		switch (tracingState) {
			case ACTIVE:
				return R.color.blue_main;
			case NOT_ACTIVE:
				return R.color.red_main;
			case ENDED:
				return R.color.purple_main;
		}
		return R.color.blue_main;
	}

	public static @ColorRes
	int getBackgroundColor(TracingState tracingState) {
		switch (tracingState) {
			case ACTIVE:
				return R.color.status_blue_bg;
			case NOT_ACTIVE:
				return R.color.grey_dark_lighter;
			case ENDED:
				return R.color.status_purple_bg;
		}
		return R.color.grey_dark_lighter;
	}

	public static int getIllu(TracingState state) {
		switch (state) {
			case ACTIVE:
				return R.drawable.ill_tracking_active;
			case ENDED:
				return R.drawable.ic_illu_tracing_ended;
			case NOT_ACTIVE:
			default:
				return -1;
		}
	}
}

