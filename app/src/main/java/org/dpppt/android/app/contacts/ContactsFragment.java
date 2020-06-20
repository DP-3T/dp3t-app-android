/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.app.contacts;

import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Switch;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.dpppt.android.app.R;
import org.dpppt.android.app.main.TracingBoxFragment;
import org.dpppt.android.app.main.views.HeaderView;
import org.dpppt.android.app.viewmodel.TracingViewModel;


public class ContactsFragment extends Fragment {

	private static final int REQUEST_CODE_BLE_INTENT = 330;

	private TracingViewModel tracingViewModel;
	private HeaderView headerView;
	private ScrollView scrollView;

	private View tracingStatusView;
	private View tracingErrorView;
	private Switch tracingSwitch;

	public static ContactsFragment newInstance() {
		return new ContactsFragment();
	}

	public ContactsFragment() { super(R.layout.fragment_contacts); }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
		getChildFragmentManager()
				.beginTransaction()
				.add(R.id.status_container, TracingBoxFragment.newInstance(false))
				.commit();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.contacts_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		tracingStatusView = view.findViewById(R.id.tracing_status);
		tracingErrorView = view.findViewById(R.id.tracing_error);
		tracingSwitch = view.findViewById(R.id.contacts_tracing_switch);

		headerView = view.findViewById(R.id.contacts_header_view);
		scrollView = view.findViewById(R.id.contacts_scroll_view);
		tracingViewModel.getAppStatusLiveData().observe(getViewLifecycleOwner(), tracingStatus -> {
			headerView.setState(tracingStatus);
		});
		setupScrollBehavior();
		setupTracingView();
	}

	private void setupTracingView() {

		tracingSwitch.setOnClickListener(v -> tracingViewModel.setTracingEnabled(tracingSwitch.isChecked()));

		tracingViewModel.getTracingStatusLiveData().observe(getViewLifecycleOwner(), status -> {
			boolean isTracing = status.isAdvertising() && status.isReceiving();
			tracingSwitch.setChecked(isTracing);
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		tracingViewModel.invalidateService();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		headerView.stopAnimation();
	}

	private void setupScrollBehavior() {

		int scrollRangePx = getResources().getDimensionPixelSize(R.dimen.top_item_padding);
		int translationRangePx = -getResources().getDimensionPixelSize(R.dimen.spacing_huge);
		scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
			float progress = computeScrollAnimProgress(scrollY, scrollRangePx);
			headerView.setAlpha(1 - progress);
			headerView.setTranslationY(progress * translationRangePx);
		});
		scrollView.post(() -> {
			float progress = computeScrollAnimProgress(scrollView.getScrollY(), scrollRangePx);
			headerView.setAlpha(1 - progress);
			headerView.setTranslationY(progress * translationRangePx);
		});
	}

	private float computeScrollAnimProgress(int scrollY, int scrollRange) {
		return Math.min(scrollY, scrollRange) / (float) scrollRange;
	}

}
