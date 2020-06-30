/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.app.reports;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import org.dpppt.android.app.R;
import org.dpppt.android.app.storage.SecureStorage;
import org.dpppt.android.app.util.DateUtils;
import org.dpppt.android.app.util.NotificationUtil;
import org.dpppt.android.app.util.PhoneUtil;
import org.dpppt.android.app.viewmodel.TracingViewModel;
import org.dpppt.android.sdk.internal.database.models.ExposureDay;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class ReportsFragment extends Fragment {

	public static ReportsFragment newInstance() {
		return new ReportsFragment();
	}

	private TracingViewModel tracingViewModel;
	private SecureStorage secureStorage;

	private ReportsSlidePageAdapter pagerAdapter;

	private ViewPager2 headerViewPager;
	private LockableScrollView scrollView;
	private View scrollViewFirstchild;
	private CirclePageIndicator circlePageIndicator;

	private View healthyView;
	private View saveOthersView;
	private View hotlineView;
	private View infectedView;

	private TextView callHotlineLastText1;
	private TextView callHotlineLastText2;
	private TextView daysLeftText1;
	private TextView daysLeftText2;

	private boolean hotlineJustCalled = false;

	private int originalFirstChildPadding = 0;

	public ReportsFragment() {
		super(R.layout.fragment_reports);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
		secureStorage = SecureStorage.getInstance(getContext());
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.reports_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		headerViewPager = view.findViewById(R.id.reports_header_viewpager);
		scrollView = view.findViewById(R.id.reports_scrollview);
		scrollViewFirstchild = view.findViewById(R.id.reports_scrollview_firstChild);
		circlePageIndicator = view.findViewById(R.id.reports_pageindicator);

		healthyView = view.findViewById(R.id.reports_healthy);
		saveOthersView = view.findViewById(R.id.reports_save_others);
		hotlineView = view.findViewById(R.id.reports_hotline);
		infectedView = view.findViewById(R.id.reports_infected);

		callHotlineLastText1 = hotlineView.findViewById(R.id.card_encounters_last_call);
		callHotlineLastText2 = saveOthersView.findViewById(R.id.card_encounters_last_call);
		daysLeftText1 = hotlineView.findViewById(R.id.card_encounters_days_left);
		daysLeftText2 = saveOthersView.findViewById(R.id.card_encounters_days_left);

		Button callHotlineButton1 = hotlineView.findViewById(R.id.card_encounters_button);
		Button callHotlineButton2 = saveOthersView.findViewById(R.id.card_encounters_button);

		callHotlineButton1.setOnClickListener(view1 -> callHotline());
		callHotlineButton2.setOnClickListener(view1 -> callHotline());

		View link1 = infectedView.findViewById(R.id.card_encounters_link);
		View link2 = hotlineView.findViewById(R.id.card_encounters_link);
		View link3 = saveOthersView.findViewById(R.id.card_encounters_link);
		View link4 = healthyView.findViewById(R.id.card_encounters_link);

		link1.setOnClickListener(v -> openLink(R.string.meldungen_explanation_link_url));
		link2.setOnClickListener(v -> openLink(R.string.meldungen_explanation_link_url));
		link3.setOnClickListener(v -> openLink(R.string.meldungen_explanation_link_url));
		link4.setOnClickListener(v -> openLink(R.string.no_meldungen_box_url));

		pagerAdapter = new ReportsSlidePageAdapter();
		headerViewPager.setAdapter(pagerAdapter);
		circlePageIndicator.setViewPager(headerViewPager);

		tracingViewModel.getAppStatusLiveData().observe(getViewLifecycleOwner(), tracingStatusInterface -> {

			healthyView.setVisibility(View.GONE);
			saveOthersView.setVisibility(View.GONE);
			hotlineView.setVisibility(View.GONE);
			infectedView.setVisibility(View.GONE);

			List<Pair<ReportsPagerFragment.Type, Long>> items = new ArrayList<>();
			if (tracingStatusInterface.isReportedAsInfected()) {
				infectedView.setVisibility(View.VISIBLE);
				items.add(new Pair<>(ReportsPagerFragment.Type.POSITIVE_TESTED, secureStorage.getInfectedDate()));
			} else if (tracingStatusInterface.wasContactReportedAsExposed()) {
				List<ExposureDay> exposureDays = tracingStatusInterface.getExposureDays();
				boolean isHotlineCallPending = secureStorage.isHotlineCallPending();
				if (isHotlineCallPending) {
					hotlineView.setVisibility(View.VISIBLE);
				} else {
					saveOthersView.setVisibility(View.VISIBLE);
				}
				daysLeftText1.setVisibility(View.GONE);
				daysLeftText2.setVisibility(View.GONE);
				for (int i = 0; i < exposureDays.size(); i++) {
					ExposureDay exposureDay = exposureDays.get(i);
					long exposureTimestamp = exposureDay.getExposedDate().getStartOfDay(TimeZone.getDefault());
					if (i == 0) {
						items.add(new Pair<>(ReportsPagerFragment.Type.POSSIBLE_INFECTION, exposureTimestamp));
						int daysDiff = DateUtils.getDaysDiffUntil(exposureTimestamp, 10);
						if (daysDiff == 1) {
							daysLeftText1.setText(R.string.date_in_one_day);
							daysLeftText2.setText(R.string.date_in_one_day);
							daysLeftText1.setVisibility(View.VISIBLE);
							daysLeftText2.setVisibility(View.VISIBLE);
						} else if (daysDiff > 1) {
							String dateStr = getString(R.string.date_in_days).replace("{COUNT}", String.valueOf(daysDiff));
							daysLeftText1.setText(dateStr);
							daysLeftText2.setText(dateStr);
							daysLeftText1.setVisibility(View.VISIBLE);
							daysLeftText2.setVisibility(View.VISIBLE);
						}
					} else {
						items.add(new Pair<>(ReportsPagerFragment.Type.NEW_CONTACT, exposureTimestamp));
					}
				}
			} else {
				healthyView.setVisibility(View.VISIBLE);
				items.add(new Pair<>(ReportsPagerFragment.Type.NO_REPORTS, null));
			}

			/* Debug items
			items.clear();
			items.add(new Pair<>(ReportsPagerFragment.Type.NO_REPORTS, null));
			items.add(new Pair<>(ReportsPagerFragment.Type.POSSIBLE_INFECTION, 1585835019000L));
			items.add(new Pair<>(ReportsPagerFragment.Type.NEW_CONTACT, 1585835019000L));
			items.add(new Pair<>(ReportsPagerFragment.Type.POSITIVE_TESTED, 1585835019000L));
			 */

			pagerAdapter.updateItems(items);
		});

		NotificationManager notificationManager =
				(NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NotificationUtil.NOTIFICATION_ID_CONTACT);
	}

	private void openLink(@StringRes int stringRes) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(stringRes)));
		startActivity(browserIntent);
	}

	private void callHotline() {
		hotlineJustCalled = true;
		secureStorage.justCalledHotline();
		PhoneUtil.callHelpline(getContext());
	}

	@Override
	public void onResume() {
		super.onResume();

		if (hotlineJustCalled) {
			hotlineJustCalled = false;
			hotlineView.setVisibility(View.GONE);
			saveOthersView.setVisibility(View.VISIBLE);
		}

		long lastHotlineCallTimestamp = secureStorage.lastHotlineCallTimestamp();
		if (lastHotlineCallTimestamp != 0) {
			((TextView) hotlineView.findViewById(R.id.card_encounters_title)).setText(R.string.meldungen_detail_call_again);

			String date = DateUtils.getFormattedDateTime(lastHotlineCallTimestamp);
			date = getString(R.string.meldungen_detail_call_last_call).replace("{DATE}", date);
			callHotlineLastText1.setText(date);
			callHotlineLastText2.setText(date);
		} else {
			callHotlineLastText1.setText("");
			callHotlineLastText2.setText("");
		}
	}

	public void doHeaderAnimation(View info, View image, Button button) {
		secureStorage.setReportsHeaderAnimationPending(false);

		ViewGroup rootView = (ViewGroup) getView();

		scrollViewFirstchild.setPadding(scrollViewFirstchild.getPaddingLeft(),
				rootView.getHeight(),
				scrollViewFirstchild.getPaddingRight(), scrollViewFirstchild.getPaddingBottom());
		scrollViewFirstchild.setVisibility(View.VISIBLE);

		rootView.post(() -> {

			AutoTransition autoTransition = new AutoTransition();
			autoTransition.setDuration(300);
			autoTransition.addListener(new Transition.TransitionListener() {
				@Override
				public void onTransitionStart(@NonNull Transition transition) {

				}

				@Override
				public void onTransitionEnd(@NonNull Transition transition) {
					headerViewPager.post(() -> {
						setupScrollBehavior();
					});
				}

				@Override
				public void onTransitionCancel(@NonNull Transition transition) {

				}

				@Override
				public void onTransitionPause(@NonNull Transition transition) {

				}

				@Override
				public void onTransitionResume(@NonNull Transition transition) {

				}
			});

			TransitionManager.beginDelayedTransition(rootView, autoTransition);

			updateHeaderSize(false);

			scrollViewFirstchild.setPadding(scrollViewFirstchild.getPaddingLeft(),
					originalFirstChildPadding,
					scrollViewFirstchild.getPaddingRight(), scrollViewFirstchild.getPaddingBottom());

			info.setVisibility(View.VISIBLE);
			image.setVisibility(View.GONE);
			button.setVisibility(View.GONE);

			circlePageIndicator.setVisibility(View.VISIBLE);
			headerViewPager.setUserInputEnabled(true);
		});
	}

	private void updateHeaderSize(boolean isReportsHeaderAnimationPending) {
		ViewGroup.LayoutParams headerLp = headerViewPager.getLayoutParams();
		FrameLayout headerLayout = (FrameLayout) headerViewPager.getParent();
		ViewGroup.LayoutParams headerLayoutLp = headerLayout.getLayoutParams();
		if (isReportsHeaderAnimationPending) {
			headerLp.height = ViewGroup.LayoutParams.MATCH_PARENT;
			headerLayoutLp.height = ViewGroup.LayoutParams.MATCH_PARENT;
		} else {
			headerLp.height = getResources().getDimensionPixelSize(R.dimen.header_height_reports);
			headerLayoutLp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		}
		headerViewPager.setLayoutParams(headerLp);
		headerLayout.setLayoutParams(headerLayoutLp);
	}

	private void setupScrollBehavior() {
		if (!isVisible()) return;

		Rect rect = new Rect();
		headerViewPager.getDrawingRect(rect);
		scrollView.setScrollPreventRect(rect);

		View headerParent = (View) headerViewPager.getParent();

		int scrollRangePx = scrollViewFirstchild.getPaddingTop();
		int translationRangePx = -getResources().getDimensionPixelSize(R.dimen.spacing_huge);
		scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
			float progress = computeScrollAnimProgress(scrollY, scrollRangePx);
			headerParent.setAlpha(1 - progress);
			headerParent.setTranslationY(progress * translationRangePx);
		});
		scrollView.post(() -> {
			float progress = computeScrollAnimProgress(scrollView.getScrollY(), scrollRangePx);
			headerParent.setAlpha(1 - progress);
			headerParent.setTranslationY(progress * translationRangePx);
		});
	}

	private float computeScrollAnimProgress(int scrollY, int scrollRange) {
		return Math.min(scrollY, scrollRange) / (float) scrollRange;
	}

	private class ReportsSlidePageAdapter extends FragmentStateAdapter {

		List<Pair<ReportsPagerFragment.Type, Long>> items = new ArrayList<>();

		boolean isReportsHeaderAnimationPending = false;

		ReportsSlidePageAdapter() {
			super(ReportsFragment.this);
		}

		void updateItems(List<Pair<ReportsPagerFragment.Type, Long>> items) {

			isReportsHeaderAnimationPending = secureStorage.isReportsHeaderAnimationPending();

			this.items.clear();
			this.items.addAll(items);
			notifyDataSetChanged();

			if (items.size() > 1) {
				if (!isReportsHeaderAnimationPending) circlePageIndicator.setVisibility(View.VISIBLE);
				ViewGroup.LayoutParams lp = headerViewPager.getLayoutParams();
				lp.height = getResources().getDimensionPixelSize(R.dimen.header_height_reports_with_indicator);
				headerViewPager.setLayoutParams(lp);
				scrollViewFirstchild.setPadding(scrollViewFirstchild.getPaddingLeft(),
						getResources().getDimensionPixelSize(R.dimen.top_item_padding_reports_width_indicator),
						scrollViewFirstchild.getPaddingRight(), scrollViewFirstchild.getPaddingBottom());
			} else {
				circlePageIndicator.setVisibility(View.GONE);
				ViewGroup.LayoutParams lp = headerViewPager.getLayoutParams();
				lp.height = getResources().getDimensionPixelSize(R.dimen.header_height_reports);
				headerViewPager.setLayoutParams(lp);
				scrollViewFirstchild.setPadding(scrollViewFirstchild.getPaddingLeft(),
						getResources().getDimensionPixelSize(R.dimen.top_item_padding_reports),
						scrollViewFirstchild.getPaddingRight(), scrollViewFirstchild.getPaddingBottom());
			}

			updateHeaderSize(isReportsHeaderAnimationPending);

			if (isReportsHeaderAnimationPending) {
				headerViewPager.setUserInputEnabled(false);

				originalFirstChildPadding = scrollViewFirstchild.getPaddingTop();

				scrollViewFirstchild.setVisibility(View.GONE);
			}

			headerViewPager.post(() -> {
				headerViewPager.setCurrentItem(items.size() - 1, false);

				setupScrollBehavior();
			});
		}

		@NonNull
		@Override
		public Fragment createFragment(int position) {

			Pair<ReportsPagerFragment.Type, Long> item = items.get(position);
			ReportsPagerFragment.Type type = item.first;
			long timestamp = item.second == null ? 0 : item.second;

			boolean showAnimationControls = isReportsHeaderAnimationPending && position == items.size() - 1;

			switch (type) {
				case NO_REPORTS:
					return ReportsPagerFragment.newInstance(ReportsPagerFragment.Type.NO_REPORTS, 0, false);
				case POSSIBLE_INFECTION:
					return ReportsPagerFragment
							.newInstance(ReportsPagerFragment.Type.POSSIBLE_INFECTION, timestamp, showAnimationControls);
				case NEW_CONTACT:
					return ReportsPagerFragment
							.newInstance(ReportsPagerFragment.Type.NEW_CONTACT, timestamp, showAnimationControls);
				case POSITIVE_TESTED:
					return ReportsPagerFragment.newInstance(ReportsPagerFragment.Type.POSITIVE_TESTED, timestamp, false);
			}

			throw new IllegalArgumentException();
		}

		@Override
		public int getItemCount() {
			return items.size();
		}

	}

}
