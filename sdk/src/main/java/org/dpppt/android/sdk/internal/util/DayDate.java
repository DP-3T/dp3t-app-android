/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.sdk.internal.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(DayDateJsonAdapter.class)
public class DayDate {

	private static final SimpleDateFormat dayDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

	static {
		dayDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private long timestampRepresentation;

	public DayDate() {
		this(System.currentTimeMillis());
	}

	public DayDate(String dayDate) throws ParseException {
		synchronized (dayDateFormat) {
			timestampRepresentation = convertToDay(dayDateFormat.parse(dayDate).getTime());
		}
	}

	public DayDate(long timestamp) {
		timestampRepresentation = convertToDay(timestamp);
	}

	public String formatAsString() {
		synchronized (dayDateFormat) {
			return dayDateFormat.format(new Date(timestampRepresentation));
		}
	}

	public DayDate getNextDay() {
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		calendar.setTimeInMillis(timestampRepresentation);
		calendar.add(Calendar.DATE, 1);
		return new DayDate(calendar.getTimeInMillis());
	}

	public long getStartOfDayTimestamp() {
		return timestampRepresentation;
	}

	public long getStartOfDay(TimeZone timeZone) {
		Calendar cal_utc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		cal_utc.setTimeInMillis(timestampRepresentation);
		Calendar cal_timezone = new GregorianCalendar(timeZone);
		cal_timezone.set(Calendar.YEAR, cal_utc.get(Calendar.YEAR));
		cal_timezone.set(Calendar.MONTH, cal_utc.get(Calendar.MONTH));
		cal_timezone.set(Calendar.DAY_OF_MONTH, cal_utc.get(Calendar.DAY_OF_MONTH));
		cal_timezone.set(Calendar.HOUR_OF_DAY, 0);
		cal_timezone.set(Calendar.MINUTE, 0);
		cal_timezone.set(Calendar.SECOND, 0);
		cal_timezone.set(Calendar.MILLISECOND, 0);
		return cal_timezone.getTimeInMillis();
	}

	public boolean isBefore(DayDate other) {
		return timestampRepresentation < other.timestampRepresentation;
	}

	public boolean isBeforeOrEquals(DayDate other) {
		return timestampRepresentation <= other.timestampRepresentation;
	}

	public boolean isBeforeOrEquals(long timestamp) {
		return timestampRepresentation <= timestamp;
	}

	private long convertToDay(long time) {
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		calendar.setTimeInMillis(time);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DayDate dayDate = (DayDate) o;
		return timestampRepresentation == dayDate.timestampRepresentation;
	}

	@Override
	public int hashCode() {
		return Objects.hash(timestampRepresentation);
	}

	public DayDate subtractDays(int days) {
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(timestampRepresentation);
		cal.add(Calendar.DATE, -days);
		return new DayDate(cal.getTimeInMillis());
	}

}
