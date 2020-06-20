/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.app.util;

import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.dpppt.android.app.R;
import org.dpppt.android.app.main.model.NotificationStateError;

public class NotificatonErrorStateHelper {

	public static void updateNotificationErrorView(View reportErrorView, NotificationStateError notificationStateError) {
		if (notificationStateError == null) {
			reportErrorView.setVisibility(View.GONE);
			return;
		}
		reportErrorView.setVisibility(View.VISIBLE);
		ImageView iconView = reportErrorView.findViewById(R.id.error_status_image);
		TextView titleView = reportErrorView.findViewById(R.id.error_status_title);
		TextView textView = reportErrorView.findViewById(R.id.error_status_text);
		TextView errorCode = reportErrorView.findViewById(R.id.error_status_code);
		errorCode.setVisibility(View.GONE);
		TextView buttonView = reportErrorView.findViewById(R.id.error_status_button);

		iconView.setImageResource(NotificationStateError.getIcon(notificationStateError));
		iconView.setVisibility(View.VISIBLE);

		titleView.setText(NotificationStateError.getTitle(notificationStateError));
		titleView.setVisibility(View.VISIBLE);

		if (NotificationStateError.getText(notificationStateError) != -1) {
			textView.setText(NotificationStateError.getText(notificationStateError));
			textView.setVisibility(View.VISIBLE);
		} else {
			textView.setVisibility(View.GONE);
		}

		if (NotificationStateError.getButtonText(notificationStateError) != -1) {
			buttonView.setText(NotificationStateError.getButtonText(notificationStateError));
			buttonView.setVisibility(View.VISIBLE);
			buttonView.setPaintFlags(buttonView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		} else {
			buttonView.setVisibility(View.GONE);
		}
	}

}
