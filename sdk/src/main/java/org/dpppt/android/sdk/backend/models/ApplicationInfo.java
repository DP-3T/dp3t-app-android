/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.sdk.backend.models;

public class ApplicationInfo {

	private String appId;
	private String reportBaseUrl;
	private String bucketBaseUrl;

	public ApplicationInfo(String appId, String reportBaseUrl, String bucketBaseUrl) {
		this.appId = appId;
		this.reportBaseUrl = reportBaseUrl;
		this.bucketBaseUrl = bucketBaseUrl;
	}

	public String getAppId() {
		return appId;
	}

	public String getReportBaseUrl() {
		return reportBaseUrl;
	}

	public String getBucketBaseUrl() {
		return bucketBaseUrl;
	}

}
