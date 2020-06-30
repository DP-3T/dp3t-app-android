/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.app.debug;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.dpppt.android.app.R;
import org.dpppt.android.app.debug.debug.model.DebugAppState;
import org.dpppt.android.app.util.InfoDialog;
import org.dpppt.android.app.viewmodel.TracingViewModel;
import org.dpppt.android.sdk.InfectionStatus;
import org.dpppt.android.sdk.TracingStatus;

public class DebugFragment extends Fragment {

	private static final DateFormat DATE_FORMAT_SYNC = SimpleDateFormat.getDateTimeInstance();
	private TracingViewModel tracingViewModel;

	public static void startDebugFragment(FragmentManager parentFragmentManager) {
		parentFragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
				.replace(R.id.main_fragment_container, DebugFragment.newInstance())
				.addToBackStack(DebugFragment.class.getCanonicalName())
				.commit();
	}

	public static DebugFragment newInstance() {
		return new DebugFragment();
	}

	public DebugFragment() {
		super(R.layout.fragment_debug);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.contacts_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		setupSdkViews(view);
		setupStateOptions(view);

	}

	private void setupSdkViews(View view) {
		TextView statusText = view.findViewById(R.id.debug_sdk_state_text);
		tracingViewModel.getTracingStatusLiveData().observe(getViewLifecycleOwner(), status -> {
			statusText.setText(formatStatusString(status));
			boolean isTracing = (status.isAdvertising() || status.isReceiving()) && status.getErrors().size() == 0;
			statusText.setBackgroundTintList(ColorStateList.valueOf(
					isTracing ? getResources().getColor(R.color.status_green_bg, null)
							: getResources().getColor(R.color.status_purple_bg, null)));
		});

		view.findViewById(R.id.debug_button_sync).setOnClickListener(v -> tracingViewModel.sync());

		view.findViewById(R.id.debug_button_reset).setOnClickListener(v -> {
			AlertDialog progressDialog = new AlertDialog.Builder(getContext())
					.setView(R.layout.dialog_loading)
					.show();

			setDebugAppState(DebugAppState.NONE);
			tracingViewModel.resetSdk(() -> {
				progressDialog.dismiss();
				InfoDialog.newInstance(R.string.android_debug_sdk_reset_text)
						.show(getChildFragmentManager(), InfoDialog.class.getCanonicalName());
				updateRadioGroup(getView().findViewById(R.id.debug_state_options_group));
			});
		});
	}

	private void setupStateOptions(View view) {
		RadioGroup optionsGroup = view.findViewById(R.id.debug_state_options_group);
		optionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
			switch (checkedId) {
				case R.id.debug_state_option_none:
					setDebugAppState(DebugAppState.NONE);
					break;
				case R.id.debug_state_option_healthy:
					setDebugAppState(DebugAppState.HEALTHY);
					break;
				case R.id.debug_state_option_exposed:
					setDebugAppState(DebugAppState.CONTACT_EXPOSED);
					break;
				case R.id.debug_state_option_infected:
					setDebugAppState(DebugAppState.REPORTED_EXPOSED);
					break;
			}
		});

		updateRadioGroup(optionsGroup);
	}

	private void updateRadioGroup(RadioGroup optionsGroup) {
		int preSetId = -1;
		switch (getDebugAppState()) {
			case NONE:
				preSetId = R.id.debug_state_option_none;
				break;
			case HEALTHY:
				preSetId = R.id.debug_state_option_healthy;
				break;
			case CONTACT_EXPOSED:
				preSetId = R.id.debug_state_option_exposed;
				break;
			case REPORTED_EXPOSED:
				preSetId = R.id.debug_state_option_infected;
				break;
		}
		optionsGroup.check(preSetId);
	}

	private SpannableString formatStatusString(TracingStatus status) {
		SpannableStringBuilder builder = new SpannableStringBuilder();
		boolean isTracing = (status.isAdvertising() || status.isReceiving()) && status.getErrors().size() == 0;
		builder.append(getString(isTracing ? R.string.tracing_active_title : R.string.android_tracing_error_title)).append("\n")
				.setSpan(new StyleSpan(Typeface.BOLD), 0, builder.length() - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

		long lastSyncDateUTC = status.getLastSyncDate();
		String lastSyncDateString =
				lastSyncDateUTC > 0 ? DATE_FORMAT_SYNC.format(new Date(lastSyncDateUTC)) : "n/a";
		builder.append(getString(R.string.debug_sdk_state_last_synced))
				.append(lastSyncDateString).append("\n")
				.append(getString(R.string.debug_sdk_state_self_exposed))
				.append(getBooleanDebugString(status.getInfectionStatus() == InfectionStatus.INFECTED)).append("\n")
				.append(getString(R.string.debug_sdk_state_contact_exposed))
				.append(getBooleanDebugString(status.getInfectionStatus() == InfectionStatus.EXPOSED)).append("\n")
				.append(getString(R.string.debug_sdk_state_number_contacts))
				.append(String.valueOf(status.getNumberOfContacts()));

		Collection<TracingStatus.ErrorState> errors = status.getErrors();
		if (errors != null && errors.size() > 0) {
			int start = builder.length();
			builder.append("\n");
			for (TracingStatus.ErrorState error : errors) {
				builder.append("\n").append(error.toString());
			}
			builder.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red_main, null)),
					start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		return new SpannableString(builder);
	}

	private String getBooleanDebugString(boolean value) {
		return getString(value ? R.string.debug_sdk_state_boolean_true : R.string.debug_sdk_state_boolean_false);
	}

	public DebugAppState getDebugAppState() {
		return ((TracingStatusWrapper) tracingViewModel.getTracingStatusInterface()).getDebugAppState();
	}

	public void setDebugAppState(DebugAppState debugAppState) {
		((TracingStatusWrapper) tracingViewModel.getTracingStatusInterface()).setDebugAppState(getContext(), debugAppState);
	}

}
