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

import java.util.ArrayList;
import java.util.List;

import org.dpppt.android.sdk.backend.models.ApplicationInfo;

public class ApplicationsList {

	private List<ApplicationInfo> applications;

	public ApplicationsList() {
		applications = new ArrayList<>();
	}

	public List<ApplicationInfo> getApplications() {
		return applications;
	}

}
