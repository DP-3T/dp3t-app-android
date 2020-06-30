/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.app.whattodo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import org.dpppt.android.app.R;
import org.dpppt.android.app.inform.InformActivity;

public class WtdPositiveTestFragment extends Fragment {

	public static WtdPositiveTestFragment newInstance() {
		return new WtdPositiveTestFragment();
	}

	public WtdPositiveTestFragment() {
		super(R.layout.fragment_what_to_do_test);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.wtd_test_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		view.findViewById(R.id.wtd_inform_button).setOnClickListener(v -> {
			Intent intent = new Intent(getActivity(), InformActivity.class);
			startActivity(intent);
		});
	}

}
