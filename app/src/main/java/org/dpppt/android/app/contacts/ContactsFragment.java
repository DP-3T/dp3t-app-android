/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.android.app.contacts;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.dpppt.android.app.R;
import org.dpppt.android.app.main.TracingViewModel;
import org.dpppt.android.app.util.DeviceFeatureHelper;
import org.dpppt.android.app.util.TracingStatusHelper;

public class ContactsFragment extends Fragment {

	private TracingViewModel tracingViewModel;

	private Button locationPermissionButton;
	private Button bluetoothButton;
	private Button batteryOptimizationButton;

	private boolean requestedSomething = false;

	public static ContactsFragment newInstance() {
		return new ContactsFragment();
	}

	public ContactsFragment() { super(R.layout.fragment_contacts); }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.contacts_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		View contactStatusView = view.findViewById(R.id.contacts_status);

		Switch tracingSwitch = view.findViewById(R.id.contacts_tracking_switch);
		tracingSwitch.setOnClickListener(v -> tracingViewModel.setTracingEnabled(tracingSwitch.isChecked()));

		tracingViewModel.getErrorsLiveData().observe(getViewLifecycleOwner(), errorStates -> {
			tracingSwitch.setEnabled(errorStates.isEmpty());
		});

		tracingViewModel.getTracingStatusLiveData().observe(getViewLifecycleOwner(), status -> {
			boolean running = status.isAdvertising() && status.isReceiving();
			tracingSwitch.setChecked(running);

			TracingStatusHelper.State state = (status.getErrors().size() > 0 || !running)
											  ? TracingStatusHelper.State.WARNING
											  : TracingStatusHelper.State.OK;
			int titleRes = state == TracingStatusHelper.State.OK ? R.string.tracing_active_title
																 : R.string.tracing_error_title;
			int textRes = state == TracingStatusHelper.State.OK ? R.string.tracing_active_text
																: R.string.tracing_error_text;
			TracingStatusHelper.updateStatusView(contactStatusView, state, titleRes, textRes);

			invalidateErrorResolverButtons();
		});

		tracingViewModel.getNumberOfHandshakesLiveData().observe(getViewLifecycleOwner(), counter -> {
			TextView counterView = contactStatusView.findViewById(R.id.status_contact_counter);
			counterView.setText(getString(R.string.tracing_active_counter, counter));
		});

		locationPermissionButton = view.findViewById(R.id.contact_location_permission_button);
		locationPermissionButton.setOnClickListener(v -> {
			requestedSomething = true;
			String[] permissions = new String[] { Manifest.permission.ACCESS_FINE_LOCATION };
			requestPermissions(permissions, 1);
		});

		batteryOptimizationButton = view.findViewById(R.id.contact_battery_button);
		batteryOptimizationButton.setOnClickListener(v -> {
			requestedSomething = true;
			startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
					Uri.parse("package:" + requireContext().getPackageName())));
		});

		bluetoothButton = view.findViewById(R.id.contact_bluetooth_button);
		bluetoothButton.setOnClickListener(v -> {
			Toast.makeText(v.getContext(), getString(R.string.activate_bluetooth_button) + " ...", Toast.LENGTH_SHORT).show();
			BluetoothAdapter.getDefaultAdapter().enable();
		});
		tracingViewModel.getBluetoothEnabledLiveData().observe(getViewLifecycleOwner(), bluetoothEnabled -> {
			bluetoothButton.setVisibility(bluetoothEnabled ? View.GONE : View.VISIBLE);
		});

		invalidateErrorResolverButtons();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (requestedSomething) {
			tracingViewModel.invalidateService();
			invalidateErrorResolverButtons();
			requestedSomething = false;
		}
	}

	private void invalidateErrorResolverButtons() {
		boolean locationPermissionGranted = DeviceFeatureHelper.isLocationPermissionGranted(requireContext());
		locationPermissionButton.setVisibility(locationPermissionGranted ? View.GONE : View.VISIBLE);

		boolean batteryOptDeactivated = DeviceFeatureHelper.isBatteryOptimizationDeactivated(requireContext());
		batteryOptimizationButton.setVisibility(batteryOptDeactivated ? View.GONE : View.VISIBLE);
	}

}
