/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.android.app.notifications;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.dpppt.android.app.R;
import org.dpppt.android.app.main.TracingViewModel;
import org.dpppt.android.app.util.PhoneUtil;
import org.dpppt.android.app.util.TracingStatusHelper;

public class NotificationsFragment extends Fragment {

	private TracingViewModel tracingViewModel;

	public static NotificationsFragment newInstance() {
		return new NotificationsFragment();
	}

	public NotificationsFragment() { super(R.layout.fragment_notifications); }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.notifications_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		View infoBubbleExposed = view.findViewById(R.id.notifications_status_exposed_layout);
		View exposedInfoGroup = infoBubbleExposed.findViewById(R.id.notifications_exposed_information_group);
		Button hotlineButton = infoBubbleExposed.findViewById(R.id.notifications_call_hotline_button);
		View statusBubble = view.findViewById(R.id.notifications_bubble);
		ImageView statusBubbleTriangle = view.findViewById(R.id.notifications_bubble_triangle);
		View statusView = view.findViewById(R.id.notifications_status_view);

		hotlineButton.setOnClickListener(v -> PhoneUtil.callHelpline(getContext()));

		tracingViewModel.getInfectedOrExposedLiveData().observe(getViewLifecycleOwner(), infectedOrExposed -> {
			boolean isExposed = infectedOrExposed.first || infectedOrExposed.second;
			TracingStatusHelper.State state =
					!(isExposed) ? TracingStatusHelper.State.OK
								 : TracingStatusHelper.State.INFO;
			int title =
					isExposed ? (infectedOrExposed.first ? R.string.meldungen_infected_title : R.string.meldungen_meldung_title)
							  : R.string.meldungen_no_meldungen_title;
			int text = isExposed ? (infectedOrExposed.first ? R.string.meldungen_infected_text :
									R.string.meldungen_meldung_text)
								 : R.string.meldungen_no_meldungen_text;
			ColorStateList bubbleColor =
					ColorStateList.valueOf(getContext().getColor(isExposed ? R.color.status_blue : R.color.status_green_bg));

			TracingStatusHelper.updateStatusView(statusView, state, title, text);
			statusBubble.setBackgroundTintList(bubbleColor);
			statusBubbleTriangle.setImageTintList(bubbleColor);
			infoBubbleExposed
					.setBackground(isExposed ? getResources().getDrawable(R.drawable.bg_status_bubble_stroke_grey, null)
											 : null);
			exposedInfoGroup.setVisibility(isExposed ? View.VISIBLE : View.GONE);
			if (isExposed) {
				((TextView) exposedInfoGroup.findViewById(R.id.notifications_info_text_specific)).setText(
						infectedOrExposed.first ? R.string.meldungen_hinweis_info_text1_infected
												   : R.string.meldungen_hinweis_info_text1);
			}
		});
	}

}
