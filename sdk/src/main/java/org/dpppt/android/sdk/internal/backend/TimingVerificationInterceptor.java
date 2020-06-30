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

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Date;

import okhttp3.Interceptor;
import okhttp3.Response;

public class TimingVerificationInterceptor implements Interceptor {

	private static final long ALLOWED_SERVER_TIME_DIFF = 60 * 1000L;

	@NonNull
	@Override
	public Response intercept(@NonNull Chain chain) throws IOException {
		Response response = chain.proceed(chain.request());

		Response networkResponse = response.networkResponse();
		Date serverTime = response.headers().getDate("Date");

		if (serverTime == null || networkResponse == null) {
			return response;
		}

		String ageString = response.header("Age");
		long age = ageString != null ? 1000 * Long.parseLong(ageString) : 0;
		long liveServerTime = serverTime.getTime() + age;

		if (Math.abs(networkResponse.receivedResponseAtMillis() - liveServerTime) > ALLOWED_SERVER_TIME_DIFF) {
			throw new ServerTimeOffsetException();
		}

		return response;
	}

}
