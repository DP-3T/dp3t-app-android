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
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import org.dpppt.android.app.R;
import org.dpppt.android.sdk.TracingStatus;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TracingErrorStateHelper {

	private static final List<TracingStatus.ErrorState> possibleErrorStatesOrderedByPriority = Arrays.asList(
			TracingStatus.ErrorState.BLE_NOT_SUPPORTED,
			TracingStatus.ErrorState.MISSING_LOCATION_PERMISSION,
			TracingStatus.ErrorState.BLE_DISABLED,
			TracingStatus.ErrorState.LOCATION_SERVICE_DISABLED,
			TracingStatus.ErrorState.BATTERY_OPTIMIZER_ENABLED,
			TracingStatus.ErrorState.SYNC_ERROR_TIMING,
			TracingStatus.ErrorState.BLE_INTERNAL_ERROR,
			TracingStatus.ErrorState.BLE_ADVERTISING_ERROR,
			TracingStatus.ErrorState.BLE_SCANNER_ERROR);

	private static final List<TracingStatus.ErrorState> possibleNotificationErrorStatesOrderedByPriority = Arrays.asList(
			TracingStatus.ErrorState.SYNC_ERROR_DATABASE,
			TracingStatus.ErrorState.SYNC_ERROR_SERVER,
			TracingStatus.ErrorState.SYNC_ERROR_NETWORK,
			TracingStatus.ErrorState.SYNC_ERROR_SIGNATURE
	);

	@StringRes
	private static int getTitle(TracingStatus.ErrorState tracingErrorState) {
		switch (tracingErrorState) {
			case LOCATION_SERVICE_DISABLED:
				return R.string.error_location_services_title;
			case BLE_DISABLED:
				return R.string.bluetooth_turned_off_title;
			case MISSING_LOCATION_PERMISSION:
				return R.string.error_location_permission_title;
			case BATTERY_OPTIMIZER_ENABLED:
				return R.string.error_battery_optimization_title;
			case SYNC_ERROR_TIMING:
				return R.string.time_inconsistency_title;
			case SYNC_ERROR_SERVER:
			case SYNC_ERROR_NETWORK:
			case SYNC_ERROR_SIGNATURE:
			case SYNC_ERROR_DATABASE:
				return R.string.sync_error_title;
			case BLE_NOT_SUPPORTED:
			case BLE_INTERNAL_ERROR:
			case BLE_ADVERTISING_ERROR:
			case BLE_SCANNER_ERROR:
			default:
				return R.string.begegnungen_restart_error_title;
		}
	}

	@StringRes
	private static int getText(TracingStatus.ErrorState tracingErrorState) {
		return tracingErrorState.getErrorString();
	}

	@DrawableRes
	private static int getIcon(TracingStatus.ErrorState tracingErrorState) {
		switch (tracingErrorState) {
			case LOCATION_SERVICE_DISABLED:
				return R.drawable.ic_gps_off;
			case BLE_DISABLED:
				return R.drawable.ic_bluetooth_off;
			case MISSING_LOCATION_PERMISSION:
				return R.drawable.ic_location_off_red;
			case BATTERY_OPTIMIZER_ENABLED:
				return R.drawable.ic_battery;
			case SYNC_ERROR_TIMING:
				return R.drawable.ic_time;
			case SYNC_ERROR_SERVER:
			case SYNC_ERROR_NETWORK:
			case SYNC_ERROR_DATABASE:
			case SYNC_ERROR_SIGNATURE:
			case BLE_NOT_SUPPORTED:
			case BLE_INTERNAL_ERROR:
			case BLE_ADVERTISING_ERROR:
			case BLE_SCANNER_ERROR:
			default:
				return R.drawable.ic_warning_red;
		}
	}

	@StringRes
	private static int getButtonText(TracingStatus.ErrorState errorState) {
		switch (errorState) {
			case LOCATION_SERVICE_DISABLED:
				return R.string.error_location_services_button;
			case BLE_DISABLED:
				return R.string.bluetooth_turn_on_button_title;
			case MISSING_LOCATION_PERMISSION:
			case BATTERY_OPTIMIZER_ENABLED:
				return R.string.error_location_permission_button;
			case SYNC_ERROR_SERVER:
			case SYNC_ERROR_NETWORK:
			case SYNC_ERROR_DATABASE:
			case SYNC_ERROR_SIGNATURE:
				return R.string.homescreen_meldung_data_outdated_retry_button;
			case SYNC_ERROR_TIMING:
			case BLE_NOT_SUPPORTED:
			case BLE_INTERNAL_ERROR:
			case BLE_ADVERTISING_ERROR:
			case BLE_SCANNER_ERROR:
			default:
				return -1;
		}
	}

	public static TracingStatus.ErrorState getErrorState(Collection<TracingStatus.ErrorState> errors) {
		for (TracingStatus.ErrorState errorState : possibleErrorStatesOrderedByPriority) {
			if (errors.contains(errorState)) {
				return errorState;
			}
		}
		return null;
	}

	public static TracingStatus.ErrorState getErrorStateForReports(Collection<TracingStatus.ErrorState> errors) {
		for (TracingStatus.ErrorState errorState : possibleNotificationErrorStatesOrderedByPriority) {
			if (errors.contains(errorState)) {
				return errorState;
			}
		}
		return null;
	}

	public static boolean isTracingErrorState(TracingStatus.ErrorState error) {
		return possibleErrorStatesOrderedByPriority.contains(error);
	}

	public static boolean isReportsErrorState(TracingStatus.ErrorState error) {
		return possibleNotificationErrorStatesOrderedByPriority.contains(error);
	}

	public static void updateErrorView(View tracingErrorView, TracingStatus.ErrorState errorState) {
		if (errorState == null) {
			tracingErrorView.setVisibility(View.GONE);
			return;
		}
		tracingErrorView.setVisibility(View.VISIBLE);
		ImageView iconView = tracingErrorView.findViewById(R.id.error_status_image);
		TextView titleView = tracingErrorView.findViewById(R.id.error_status_title);
		TextView textView = tracingErrorView.findViewById(R.id.error_status_text);
		TextView errorCode = tracingErrorView.findViewById(R.id.error_status_code);
		TextView buttonView = tracingErrorView.findViewById(R.id.error_status_button);

		iconView.setImageResource(TracingErrorStateHelper.getIcon(errorState));
		iconView.setVisibility(View.VISIBLE);

		titleView.setText(TracingErrorStateHelper.getTitle(errorState));
		titleView.setVisibility(View.VISIBLE);

		if (TracingErrorStateHelper.getText(errorState) != -1) {
			textView.setText(TracingErrorStateHelper.getText(errorState));
			textView.setVisibility(View.VISIBLE);
		} else {
			textView.setVisibility(View.GONE);
		}
		if (!TextUtils.isEmpty(TracingErrorStateHelper.getErrorCode(errorState))) {
			errorCode.setText(TracingErrorStateHelper.getErrorCode(errorState));
			errorCode.setVisibility(View.VISIBLE);
		} else {
			errorCode.setVisibility(View.GONE);
		}

		if (TracingErrorStateHelper.getButtonText(errorState) != -1) {
			buttonView.setText(TracingErrorStateHelper.getButtonText(errorState));
			buttonView.setVisibility(View.VISIBLE);
			buttonView.setPaintFlags(buttonView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		} else {
			buttonView.setVisibility(View.GONE);
		}
	}

	private static String getErrorCode(TracingStatus.ErrorState errorState) {
		switch (errorState) {
			case BLE_NOT_SUPPORTED:
				return "NSBNS";
			case BLE_INTERNAL_ERROR:
				return "NSBIR";
			case BLE_ADVERTISING_ERROR:
				return "NSBAE";
			case BLE_SCANNER_ERROR:
				return "NSBSE";
			case SYNC_ERROR_SERVER:
				return "RTSES";
			case SYNC_ERROR_NETWORK:
				return "RTSEN";
			case SYNC_ERROR_SIGNATURE:
				return "RTSESI";
			case SYNC_ERROR_DATABASE:
				return "RTSEDB";
			case SYNC_ERROR_TIMING:
			case MISSING_LOCATION_PERMISSION:
			case LOCATION_SERVICE_DISABLED:
			case BLE_DISABLED:
			case BATTERY_OPTIMIZER_ENABLED:
			default:
				return "";
		}
	}

}
