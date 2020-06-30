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

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class ProcessUtil {

	public static boolean isMainProcess(Context context) {
		return context.getPackageName().equals(getProcessName(context));
	}

	private static String getProcessName(Context context) {
		int mypid = android.os.Process.myPid();
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo info : infos) {
			if (info.pid == mypid) {
				return info.processName;
			}
		}
		// may never return null
		return null;
	}

}
