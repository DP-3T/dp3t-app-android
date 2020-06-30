/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.dpppt.android.app.onboarding.util;

import android.content.Context;
import android.graphics.Color;
import android.widget.Button;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import org.dpppt.android.app.R;


public class PermissionButtonUtil {
	public static void setButtonDefault(Button button, @StringRes int buttonLabel) {
		Context context = button.getContext();
		button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		button.setTextColor(Color.WHITE);
		button.setText(buttonLabel);
		button.setClickable(true);
		button.setElevation(context.getResources().getDimensionPixelSize(R.dimen.button_elevation));
		button.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.blue_main));
	}

	public static void setButtonOk(Button button, @StringRes int grantedLabel) {
		Context context = button.getContext();
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0);
		button.setCompoundDrawableTintList(ContextCompat.getColorStateList(context, R.color.blue_main));
		button.setTextColor(ContextCompat.getColor(context, R.color.blue_main));
		button.setText(grantedLabel);
		button.setClickable(false);
		button.setElevation(0);
		button.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.white));
	}

}
