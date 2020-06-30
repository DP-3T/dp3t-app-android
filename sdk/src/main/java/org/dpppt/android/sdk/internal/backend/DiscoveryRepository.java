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
import androidx.annotation.NonNull;

import java.io.IOException;

import org.dpppt.android.sdk.backend.ResponseCallback;
import org.dpppt.android.sdk.internal.backend.models.ApplicationsList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DiscoveryRepository implements Repository {

	private DiscoveryService discoveryService;

	public DiscoveryRepository(@NonNull Context context) {

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl("https://discovery.dpppt.org/")
				.client(getClientBuilder(context).build())
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		discoveryService = retrofit.create(DiscoveryService.class);
	}

	public void getDiscovery(@NonNull ResponseCallback<ApplicationsList> responseCallback, boolean dev) {
		//TODO caching for no network connection

		Call<ApplicationsList> call = dev ? discoveryService.getDiscoveryDev() : discoveryService.getDiscovery();
		call.enqueue(new Callback<ApplicationsList>() {

			@Override
			public void onResponse(@NonNull Call<ApplicationsList> call, @NonNull Response<ApplicationsList> response) {
				if (response.isSuccessful()) {
					responseCallback.onSuccess(response.body());
				} else {
					onFailure(call, new StatusCodeException(response.raw()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<ApplicationsList> call, @NonNull Throwable throwable) {
				responseCallback.onError(throwable);
			}
		});
	}

	public ApplicationsList getDiscoverySync(boolean dev) throws IOException {
		return (dev ? discoveryService.getDiscoveryDev() : discoveryService.getDiscovery()).execute().body();
	}

}
