/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.sdk.util;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.dpppt.android.sdk.DP3TCalibrationHelper;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class FileUploadRepository {

	FileUpload uploadService;

	public FileUploadRepository() {
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl("https://dp3tdemoupload.azurewebsites.net/")
				.build();

		uploadService = retrofit.create(FileUpload.class);
	}

	public void uploadDatabase(Context context, String name, Callback<Void> callback) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		File dbFile = new File(context.getCacheDir(),
				sdf.format(new Date()) + "_" + name + "_" + DeviceHelper.getDeviceID(context) + "_dp3t_callibration_db.sqlite");

		try {
			DP3TCalibrationHelper.exportDb(context, new FileOutputStream(dbFile), () ->
					uploadFile(dbFile, callback)
			);
		} catch (FileNotFoundException e) {
			callback.onFailure(null, e);
		}
	}

	private void uploadFile(File file, Callback<Void> callback) {
		RequestBody requestFile =
				RequestBody.create(
						MediaType.parse("application/sqlite"),
						file
				);
		MultipartBody.Part body =
				MultipartBody.Part.createFormData("file", file.getName(), requestFile);

		uploadService.upload(body).enqueue(callback);
	}

}
