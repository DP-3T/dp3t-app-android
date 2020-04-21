/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.android.app.trigger;

import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.dpppt.android.app.trigger.views.ChainedEditText;
import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.internal.backend.CallbackListener;
import org.dpppt.android.sdk.internal.backend.ResponseException;
import org.dpppt.android.sdk.internal.backend.models.ExposeeAuthData;

import org.dpppt.android.app.R;
import org.dpppt.android.app.util.InfoDialog;

public class TriggerFragment extends Fragment {

	private static final String REGEX_CODE_PATTERN = "\\d{6}";

	private ChainedEditText authCodeInput;

	public static TriggerFragment newInstance() {
		return new TriggerFragment();
	}

	public TriggerFragment() {
		super(R.layout.fragment_trigger);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Button buttonSend = view.findViewById(R.id.trigger_fragment_button_trigger);
		authCodeInput = view.findViewById(R.id.trigger_fragment_input);
		authCodeInput.addTextChangedListener(new ChainedEditText.ChainedEditTextListener() {
			@Override
			public void onTextChanged(String input) {
				buttonSend.setEnabled(input.matches(REGEX_CODE_PATTERN));
			}

			@Override
			public void onEditorSendAction() {
				if (buttonSend.isEnabled()) buttonSend.callOnClick();
			}
		});

		buttonSend.setOnClickListener(v -> {
			// TODO: HARDCODED ONSET DATE - NOT FINAL
			Calendar calendarNow = new GregorianCalendar();
			calendarNow.add(Calendar.DAY_OF_YEAR, -14);

			AlertDialog progressDialog = new AlertDialog.Builder(getContext())
					.setView(R.layout.dialog_loading)
					.show();
			String inputBase64 = new String(Base64.encode(authCodeInput.getText().getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP),
					StandardCharsets.UTF_8);
			DP3T.sendIWasExposed(v.getContext(), new Date(calendarNow.getTimeInMillis()),
					new ExposeeAuthData(inputBase64), new CallbackListener<Void>() {
						@Override
						public void onSuccess(Void response) {
							progressDialog.dismiss();
							getParentFragmentManager().beginTransaction()
									.replace(R.id.main_fragment_container, ThankYouFragment.newInstance())
									.addToBackStack(ThankYouFragment.class.getCanonicalName())
									.commit();
						}

						@Override
						public void onError(Throwable throwable) {
							progressDialog.dismiss();
							String error;
							error = getString(R.string.unexpected_error_title).replace("{ERROR}",
									throwable instanceof ResponseException ? throwable.getMessage() :
									throwable instanceof IOException ? throwable.getLocalizedMessage() : "");
							InfoDialog.newInstance(error)
									.show(getChildFragmentManager(), InfoDialog.class.getCanonicalName());
							throwable.printStackTrace();
						}
					});
		});

		view.findViewById(R.id.cancel_button).setOnClickListener(v -> {
			getActivity().onBackPressed();
		});

		view.findViewById(R.id.trigger_fragment_no_code_button).setOnClickListener(v -> {
			getParentFragmentManager().beginTransaction()
					.replace(R.id.main_fragment_container, NoCodeFragment.newInstance())
					.addToBackStack(NoCodeFragment.class.getCanonicalName())
					.commit();
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		authCodeInput.requestFocus();
	}

}
