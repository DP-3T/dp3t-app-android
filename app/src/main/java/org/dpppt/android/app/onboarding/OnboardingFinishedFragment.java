/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.app.onboarding;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.dpppt.android.app.R;

public class OnboardingFinishedFragment extends Fragment {

	public static OnboardingFinishedFragment newInstance() {
		return new OnboardingFinishedFragment();
	}

	public OnboardingFinishedFragment() {
		super(R.layout.fragment_onboarding_finished);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		view.findViewById(R.id.onboarding_continue_button)
				.setOnClickListener(v -> {
					((OnboardingActivity) getActivity()).continueToNextPage();
				});
	}

}
