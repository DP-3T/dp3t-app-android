/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.dpppt.android.app.storage;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SecureStorage {

	private static final String PREFERENCES = "SecureStorage";

	private static final String KEY_INFECTED_DATE = "infected_date";
	private static final String KEY_INFORM_TIME_REQ = "inform_time_req";
	private static final String KEY_INFORM_CODE_REQ = "inform_code_req";
	private static final String KEY_INFORM_TOKEN_REQ = "inform_token_req";
	private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
	private static final String KEY_LAST_SHOWN_CONTACT_ID = "last_shown_contact_id";
	private static final String KEY_HOTLINE_CALL_PENDING = "hotline_call_pending";
	private static final String KEY_HOTLINE_LAST_CALL_TIMESTAMP = "hotline_ever_called_timestamp";
	private static final String KEY_PENDING_REPORTS_HEADER_ANIMATION = "pending_reports_header_animation";
	private static final String KEY_CONFIG_FORCE_UPDATE = "config_do_force_update";
	private static final String KEY_CONFIG_HAS_INFOBOX = "has_ghettobox";
	private static final String KEY_CONFIG_INFOBOX_TITLE = "ghettobox_title";
	private static final String KEY_CONFIG_INFOBOX_TEXT = "ghettobox_text";
	private static final String KEY_CONFIG_INFOBOX_LINK_TITLE = "ghettobox_link_title";
	private static final String KEY_CONFIG_INFOBOX_LINK_URL = "ghettobox_link_url";
	private static final String KEY_CONFIG_FORCED_TRACE_SHUTDOWN = "forced_trace_shutdown";

	private static SecureStorage instance;

	private SharedPreferences prefs;

	private final MutableLiveData<Boolean> forceUpdateLiveData;
	private final MutableLiveData<Boolean> hasInfoboxLiveData;

	private SecureStorage(@NonNull Context context) {
		try {
			String masterKeys = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
			this.prefs = EncryptedSharedPreferences
					.create(PREFERENCES, masterKeys, context, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
							EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

		} catch (GeneralSecurityException | IOException e) {
			this.prefs = null;
			e.printStackTrace();
		}

		forceUpdateLiveData = new MutableLiveData<>(getDoForceUpdate());
		hasInfoboxLiveData = new MutableLiveData<>(getHasInfobox());
	}

	public static SecureStorage getInstance(Context context) {
		if (instance == null) {
			instance = new SecureStorage(context);
		}
		return instance;
	}

	public LiveData<Boolean> getForceUpdateLiveData() {
		return forceUpdateLiveData;
	}

	public LiveData<Boolean> getInfoBoxLiveData() {
		return hasInfoboxLiveData;
	}

	public long getInfectedDate() {
		return prefs.getLong(KEY_INFECTED_DATE, 0);
	}

	public void setInfectedDate(long date) {
		prefs.edit().putLong(KEY_INFECTED_DATE, date).apply();
	}

	public void saveInformTimeAndCodeAndToken(String informCode, String informToken) {
		prefs.edit().putLong(KEY_INFORM_TIME_REQ, System.currentTimeMillis())
				.putString(KEY_INFORM_CODE_REQ, informCode)
				.putString(KEY_INFORM_TOKEN_REQ, informToken)
				.apply();
	}

	public void clearInformTimeAndCodeAndToken() {
		prefs.edit().remove(KEY_INFORM_TIME_REQ)
				.remove(KEY_INFORM_CODE_REQ)
				.remove(KEY_INFORM_TOKEN_REQ)
				.apply();
	}

	public long getLastInformRequestTime() {
		return prefs.getLong(KEY_INFORM_TIME_REQ, 0);
	}

	public String getLastInformCode() {
		return prefs.getString(KEY_INFORM_CODE_REQ, null);
	}

	public String getLastInformToken() {
		return prefs.getString(KEY_INFORM_TOKEN_REQ, null);
	}

	public boolean getOnboardingCompleted() {
		return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
	}

	public void setOnboardingCompleted(boolean completed) {
		prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply();
	}

	public int getLastShownContactId() {
		return prefs.getInt(KEY_LAST_SHOWN_CONTACT_ID, -1);
	}

	public void setLastShownContactId(int contactId) {
		prefs.edit().putInt(KEY_LAST_SHOWN_CONTACT_ID, contactId).apply();
	}

	public boolean isHotlineCallPending() {
		return prefs.getBoolean(KEY_HOTLINE_CALL_PENDING, false);
	}

	public void setHotlineCallPending(boolean pending) {
		prefs.edit().putBoolean(KEY_HOTLINE_CALL_PENDING, pending).apply();
	}

	public long lastHotlineCallTimestamp() {
		return prefs.getLong(KEY_HOTLINE_LAST_CALL_TIMESTAMP, 0);
	}

	public void justCalledHotline() {
		prefs.edit().putBoolean(KEY_HOTLINE_CALL_PENDING, false)
				.putLong(KEY_HOTLINE_LAST_CALL_TIMESTAMP, System.currentTimeMillis())
				.apply();
	}

	public boolean isReportsHeaderAnimationPending() {
		return prefs.getBoolean(KEY_PENDING_REPORTS_HEADER_ANIMATION, false);
	}

	public void setReportsHeaderAnimationPending(boolean pending) {
		prefs.edit().putBoolean(KEY_PENDING_REPORTS_HEADER_ANIMATION, pending).apply();
	}

	public void setDoForceUpdate(boolean doForceUpdate) {
		prefs.edit().putBoolean(KEY_CONFIG_FORCE_UPDATE, doForceUpdate).apply();
		forceUpdateLiveData.postValue(doForceUpdate);
	}

	public boolean getDoForceUpdate() {
		return prefs.getBoolean(KEY_CONFIG_FORCE_UPDATE, false);
	}

	public void setHasInfobox(boolean hasInfobox) {
		prefs.edit().putBoolean(KEY_CONFIG_HAS_INFOBOX, hasInfobox).apply();
		hasInfoboxLiveData.postValue(hasInfobox);
	}

	public boolean getHasInfobox() {
		return prefs.getBoolean(KEY_CONFIG_HAS_INFOBOX, false);
	}

	public void setInfoboxTitle(String title) {
		prefs.edit().putString(KEY_CONFIG_INFOBOX_TITLE, title).apply();
	}

	public String getInfoboxTitle() {
		return prefs.getString(KEY_CONFIG_INFOBOX_TITLE, null);
	}

	public void setInfoboxText(String text) {
		prefs.edit().putString(KEY_CONFIG_INFOBOX_TEXT, text).apply();
	}

	public String getInfoboxText() {
		return prefs.getString(KEY_CONFIG_INFOBOX_TEXT, null);
	}

	public void setInfoboxLinkTitle(String title) {
		prefs.edit().putString(KEY_CONFIG_INFOBOX_LINK_TITLE, title).apply();
	}

	public boolean getForcedTraceShutdown() {
		return prefs.getBoolean(KEY_CONFIG_FORCED_TRACE_SHUTDOWN, false);
	}

	public void setForcedTraceShutdown(boolean forcedTraceShutdown) {
		prefs.edit().putBoolean(KEY_CONFIG_FORCED_TRACE_SHUTDOWN, forcedTraceShutdown).apply();
	}

	public String getInfoboxLinkTitle() {
		return prefs.getString(KEY_CONFIG_INFOBOX_LINK_TITLE, null);
	}

	public void setInfoboxLinkUrl(String url) {
		prefs.edit().putString(KEY_CONFIG_INFOBOX_LINK_URL, url).apply();
	}

	public String getInfoboxLinkUrl() {
		return prefs.getString(KEY_CONFIG_INFOBOX_LINK_URL, null);
	}

}