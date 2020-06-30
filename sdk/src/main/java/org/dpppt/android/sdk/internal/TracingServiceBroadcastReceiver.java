/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.sdk.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

import org.dpppt.android.sdk.internal.logger.Logger;

public class TracingServiceBroadcastReceiver extends BroadcastReceiver {

	public static final String TAG = "TracingServiceBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent i) {
		Logger.d(TAG, "received broadcast to start service");
		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		boolean advertising = appConfigManager.isAdvertisingEnabled();
		boolean receiving = appConfigManager.isReceivingEnabled();
		long scanInterval = appConfigManager.getScanInterval();
		long scanDuration = appConfigManager.getScanDuration();
		if (advertising || receiving) {
			Intent intent = new Intent(context, TracingService.class).setAction(i.getAction());
			intent.putExtra(TracingService.EXTRA_ADVERTISE, advertising);
			intent.putExtra(TracingService.EXTRA_RECEIVE, receiving);
			intent.putExtra(TracingService.EXTRA_SCAN_INTERVAL, scanInterval);
			intent.putExtra(TracingService.EXTRA_SCAN_DURATION, scanDuration);
			ContextCompat.startForegroundService(context, intent);
		}
	}

}
