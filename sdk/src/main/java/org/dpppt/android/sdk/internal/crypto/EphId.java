/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.dpppt.android.sdk.internal.crypto;

import java.util.Arrays;

public class EphId {

	private byte[] data;

	public EphId(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EphId ephId = (EphId) o;
		return Arrays.equals(data, ephId.data);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(data);
	}

}
