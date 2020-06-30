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

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.backend.BackendBucketRepository;
import org.dpppt.android.sdk.internal.database.models.Contact;
import org.dpppt.android.sdk.internal.database.models.Handshake;

public class ContactsFactory {

	private static final long WINDOW_DURATION = 5 * 60 * 1000l;

	public static List<Contact> mergeHandshakesToContacts(Context context, List<Handshake> handshakes) {
		HashMap<EphId, List<Handshake>> handshakeMapping = new HashMap<>();

		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);

		// group handhakes by id
		for (Handshake handshake : handshakes) {
			if (!handshakeMapping.containsKey(handshake.getEphId())) {
				handshakeMapping.put(handshake.getEphId(), new ArrayList<>());
			}
			handshakeMapping.get(handshake.getEphId()).add(handshake);
		}

		//filter result to only contain actual contacts in close proximity
		List<Contact> contacts = new ArrayList<>();
		for (List<Handshake> handshakeList : handshakeMapping.values()) {

			int contactCounter = 0;

			long startTime = min(handshakeList, (h) -> h.getTimestamp());
			for (long offset = 0; offset < CryptoModule.MILLISECONDS_PER_EPOCH; offset += WINDOW_DURATION) {
				long windowStart = startTime + offset;
				long windowEnd = startTime + offset + WINDOW_DURATION;
				Double windowMean = mean(handshakeList, (h) -> h.getTimestamp() >= windowStart && h.getTimestamp() < windowEnd);

				if (windowMean != null && windowMean < appConfigManager.getContactAttenuationThreshold()) {
					contactCounter++;
				}
			}

			if (contactCounter > 0) {
				contacts.add(
						new Contact(-1, floorTimestampToBucket(handshakeList.get(0).getTimestamp()),
								handshakeList.get(0).getEphId(),
								contactCounter,
								0));
			}
		}

		return contacts;
	}

	private static Double mean(List<Handshake> handshakes, Condition condition) {
		Double valueSum = null;
		int count = 0;
		for (Handshake handshake : handshakes) {
			if (condition.test(handshake)) {
				if (valueSum == null) {
					valueSum = 0.0;
				}
				valueSum += handshake.getAttenuation();
				count++;
			}
		}
		if (valueSum != null) {
			return valueSum / count;
		} else {
			return null;
		}
	}

	private static <T> Long min(List<T> values, ToLongConverter<T> converter) {
		Long min = null;
		for (T val : values) {
			if (min == null || converter.toLong(val) < min) {
				min = converter.toLong(val);
			}
		}
		return min;
	}

	private interface Condition {
		boolean test(Handshake handshake);

	}


	private interface ToLongConverter<T> {
		long toLong(T value);

	}

	private static long floorTimestampToBucket(long timestamp) {
		return timestamp - (timestamp % BackendBucketRepository.BATCH_LENGTH);
	}

}
