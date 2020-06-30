/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.dpppt.android.sdk.internal.database.models;

import org.dpppt.android.sdk.internal.util.DayDate;

public class ExposureDay {

	private int id;
	private DayDate exposedDate;
	private long reportDate;

	public ExposureDay(int id, DayDate exposedDate, long reportDate) {
		this.id = id;
		this.exposedDate = exposedDate;
		this.reportDate = reportDate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public DayDate getExposedDate() {
		return exposedDate;
	}

	public void setExposedDate(DayDate exposedDate) {
		this.exposedDate = exposedDate;
	}

	public long getReportDate() {
		return reportDate;
	}

	public void setReportDate(long reportDate) {
		this.reportDate = reportDate;
	}

}
