/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.sdk.internal.backend;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;

interface Repository {

	default OkHttpClient.Builder getClientBuilder(@NonNull Context context) {
		String versionName;
		PackageManager manager = context.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			versionName = info.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			versionName = "unknown";
		}

		String userAgent = context.getPackageName() + ";" + versionName + ";Android;" + Build.VERSION.SDK_INT;

		OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
		okHttpBuilder.addInterceptor(chain -> {
			Request request = chain.request()
					.newBuilder()
					.header("User-Agent", userAgent)
					.build();
			return chain.proceed(request);
		});

		int cacheSize = 50 * 1024 * 1024; // 50 MB
		Cache cache = new Cache(context.getCacheDir(), cacheSize);
		okHttpBuilder.cache(cache);

		okHttpBuilder.certificatePinner(CertificatePinning.getCertificatePinner());

		return okHttpBuilder;
	}

}
