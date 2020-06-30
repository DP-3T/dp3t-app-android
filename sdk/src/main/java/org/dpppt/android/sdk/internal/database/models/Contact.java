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

import org.dpppt.android.sdk.internal.crypto.EphId;

public class Contact {

	private int id;
	private long date;
	private EphId ephId;
	private int windowCount;
	private int associatedKnownCase;

	public Contact(int id, long date, EphId ephId, int windowCount, int associatedKnownCase) {
		this.id = id;
		this.date = date;
		this.ephId = ephId;
		this.windowCount = windowCount;
		this.associatedKnownCase = associatedKnownCase;
	}

	public EphId getEphId() {
		return ephId;
	}

	public long getDate() {
		return date;
	}

	public double getWindowCount() {
		return windowCount;
	}

	public int getAssociatedKnownCase() {
		return associatedKnownCase;
	}

	public int getId() {
		return id;
	}

}
