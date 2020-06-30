/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.sdk.internal.backend.models;

import org.dpppt.android.sdk.backend.models.ExposeeAuthMethodJson;

import java.util.ArrayList;

public class ExposeeRequest {

	private final String key;
	private final long keyDate;
	private final ExposeeAuthMethodJson authData;
	private final int fake;
	private ArrayList<String> countryCodeList;

	public ExposeeRequest(String key, long keyDate, ExposeeAuthMethodJson authData, ArrayList<String> countryCodeList) {
		this(key, keyDate, 0, authData, countryCodeList);
	}

	public ExposeeRequest(String key, long keyDate, int fake, ExposeeAuthMethodJson authData, ArrayList<String> countryCodeList) {
		this.key = key;
		this.keyDate = keyDate;
		this.authData = authData;
		this.fake = fake;
		this.countryCodeList = countryCodeList;
	}

	public String getKey() {
		return key;
	}

	public long getKeyDate() {
		return keyDate;
	}

	public int getFake() {
		return fake;
	}

	public ExposeeAuthMethodJson getAuthData() {
		return authData;
	}

}
