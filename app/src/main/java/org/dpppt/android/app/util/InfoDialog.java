/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.dpppt.android.app.util;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import org.dpppt.android.app.R;

public class InfoDialog extends DialogFragment {

	private static final String ARG_TEXT_ID = "arg_text_id";
	private static final String ARG_BUTTON_LABEL_ID = "arg_button_label_id";
	private static final String ARG_TEXT_STRING = "arg_text_string";
	private static final String ARG_DETAIL_STRING = "ARG_DETAIL_STRING";

	private Button button;
	private View.OnClickListener onClickListener;

	public static InfoDialog newInstance(@StringRes int text) {
		Bundle args = new Bundle();
		args.putInt(ARG_TEXT_ID, text);
		InfoDialog fragment = new InfoDialog();
		fragment.setArguments(args);
		return fragment;
	}

	public static InfoDialog newInstanceWithButtonLabel(@StringRes int text, @StringRes int buttonLabelId) {
		Bundle args = new Bundle();
		args.putInt(ARG_TEXT_ID, text);
		args.putInt(ARG_BUTTON_LABEL_ID, buttonLabelId);
		InfoDialog fragment = new InfoDialog();
		fragment.setArguments(args);
		return fragment;
	}

	public static InfoDialog newInstanceWithDetail(String text, String detail) {
		Bundle args = new Bundle();
		args.putString(ARG_TEXT_STRING, text);
		args.putString(ARG_DETAIL_STRING, detail);
		InfoDialog fragment = new InfoDialog();
		fragment.setArguments(args);
		return fragment;
	}

	public static InfoDialog newInstance(String text) {
		Bundle args = new Bundle();
		args.putString(ARG_TEXT_STRING, text);
		InfoDialog fragment = new InfoDialog();
		fragment.setArguments(args);
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.dialog_info, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Bundle args = getArguments();

		TextView textView = view.findViewById(R.id.dialog_info_text);
		if (args.containsKey(ARG_TEXT_ID)) {
			textView.setText(args.getInt(ARG_TEXT_ID));
		} else {
			textView.setText(args.getString(ARG_TEXT_STRING));
		}

		TextView detailView = view.findViewById(R.id.dialog_info_subtext);
		String detail = args.getString(ARG_DETAIL_STRING);
		if (detail != null) {
			detailView.setVisibility(View.VISIBLE);
			detailView.setText(detail);
		} else {
			detailView.setVisibility(View.GONE);
		}

		button = view.findViewById(R.id.dialog_info_button);
		if (args.containsKey(ARG_BUTTON_LABEL_ID)) button.setText(args.getInt(ARG_BUTTON_LABEL_ID));
		button.setOnClickListener(v -> {
			if (onClickListener != null) {
				onClickListener.onClick(v);
			} else {
				getDialog().dismiss();
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	public void setButtonOnClickListener(View.OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

}
