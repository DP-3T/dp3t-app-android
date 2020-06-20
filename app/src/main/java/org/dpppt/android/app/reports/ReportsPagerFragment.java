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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dpppt.android.app.R;
import org.dpppt.android.app.util.DateUtils;

public class ReportsPagerFragment extends Fragment {

	private static final String ARG_TYPE = "ARG_TYPE";
	private static final String ARG_TIMESTAMP = "ARG_TIMESTAMP";
	private static final String ARG_SHOWANIMATIONCONTROLS = "ARG_SHOWANIMATIONCONTROLS";

	public static ReportsPagerFragment newInstance(@NonNull Type type, long timestamp, boolean showAnimationControls) {
		ReportsPagerFragment reportsPagerFragment = new ReportsPagerFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_TYPE, type.ordinal());
		args.putLong(ARG_TIMESTAMP, timestamp);
		args.putBoolean(ARG_SHOWANIMATIONCONTROLS, showAnimationControls);
		reportsPagerFragment.setArguments(args);
		return reportsPagerFragment;
	}

	public enum Type {
		NO_REPORTS,
		POSSIBLE_INFECTION,
		NEW_CONTACT,
		POSITIVE_TESTED
	}

	private Type type;
	private long timestamp;
	private boolean showAnimationControls;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		type = Type.values()[getArguments().getInt(ARG_TYPE)];
		timestamp = getArguments().getLong(ARG_TIMESTAMP);
		showAnimationControls = getArguments().getBoolean(ARG_SHOWANIMATIONCONTROLS);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		View view = null;

		switch (type) {
			case NO_REPORTS:
				view = inflater.inflate(R.layout.fragment_reports_pager_no_reports, container, false);
				break;
			case POSSIBLE_INFECTION:
				view = inflater.inflate(R.layout.fragment_reports_pager_possible_infection, container, false);
				break;
			case NEW_CONTACT:
				view = inflater.inflate(R.layout.fragment_reports_pager_possible_infection, container, false);
				TextView title = view.findViewById(R.id.fragment_reports_pager_title);
				TextView subTitle = view.findViewById(R.id.fragment_reports_pager_subtitle);
				title.setText(R.string.meldung_detail_new_contact_title);
				subTitle.setText(R.string.meldung_detail_new_contact_subtitle);
				break;
			case POSITIVE_TESTED:
				view = inflater.inflate(R.layout.fragment_reports_pager_positive_tested, container, false);
				break;
		}

		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

		if (timestamp != 0) {
			TextView date = view.findViewById(R.id.fragment_reports_pager_date);

			String dateStr = DateUtils.getFormattedDate(timestamp) + " / ";
			int daysDiff = DateUtils.getDaysDiff(timestamp);

			if (daysDiff == 0) {
				dateStr += getString(R.string.date_today);
			} else if (daysDiff == 1) {
				dateStr += getString(R.string.date_one_day_ago);
			} else {
				dateStr += getString(R.string.date_days_ago).replace("{COUNT}", String.valueOf(daysDiff));
			}
			date.setText(dateStr);
		}

		if (type == Type.POSSIBLE_INFECTION || type == Type.NEW_CONTACT) {

			if (showAnimationControls) {
				View info = view.findViewById(R.id.fragment_reports_pager_info);
				View image = view.findViewById(R.id.fragment_reports_pager_image);
				Button button = view.findViewById(R.id.fragment_reports_pager_button);

				info.setVisibility(View.GONE);
				image.setVisibility(View.VISIBLE);
				button.setVisibility(View.VISIBLE);

				button.setOnClickListener(view1 -> {
					((ReportsFragment) getParentFragment()).doHeaderAnimation(info, image, button);
				});
			}
		}
	}

}
