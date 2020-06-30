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

import org.dpppt.android.sdk.internal.backend.models.ExposeeRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

interface ReportService {

	@Headers("Accept: application/json")
	@POST("v1/exposed")
	Call<Void> addExposee(@Body ExposeeRequest exposeeRequest, @Header("Authorization") String authorizationHeader);

}
