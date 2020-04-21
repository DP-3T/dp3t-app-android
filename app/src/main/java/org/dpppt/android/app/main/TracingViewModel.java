/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.android.app.main;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.List;

import org.dpppt.android.app.util.DeviceFeatureHelper;
import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.TracingStatus;

import org.dpppt.android.app.main.model.AppState;

public class TracingViewModel extends AndroidViewModel {

	private final MutableLiveData<TracingStatus> tracingStatusLiveData = new MutableLiveData<>();
	private BroadcastReceiver tracingStatusBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			invalidateTracingStatus();
		}
	};

	private final MutableLiveData<Boolean> tracingEnabledLiveData = new MutableLiveData<>();
	private final MutableLiveData<Pair<Boolean, Boolean>> exposedLiveData = new MutableLiveData<>();
	private final MutableLiveData<Integer> numberOfHandshakesLiveData = new MutableLiveData<>(0);
	private final MutableLiveData<List<TracingStatus.ErrorState>> errorsLiveData = new MutableLiveData<>(Collections.emptyList());
	private final MutableLiveData<AppState> appStateLiveData = new MutableLiveData<>();

	private final MutableLiveData<Boolean> bluetoothEnabledLiveData = new MutableLiveData<>();
	private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
				invalidateBluetoothState();
				invalidateTracingStatus();
			}
		}
	};

	public TracingViewModel(@NonNull Application application) {
		super(application);

		tracingStatusLiveData.observeForever(status -> {
			tracingEnabledLiveData.setValue(status.isAdvertising() && status.isReceiving());
			numberOfHandshakesLiveData.setValue(status.getNumberOfHandshakes());
			exposedLiveData.setValue(new Pair<>(status.isReportedAsExposed(), status.wasContactExposed()));
			errorsLiveData.setValue(status.getErrors());

			boolean hasError = status.getErrors().size() > 0;
			if (status.isReportedAsExposed() || status.wasContactExposed()) {
				appStateLiveData.setValue(hasError ? AppState.EXPOSED_ERROR : AppState.EXPOSED);
			} else if (hasError) {
				appStateLiveData.setValue(AppState.ERROR);
			} else  if (status.isAdvertising() && status.isReceiving()) {
				appStateLiveData.setValue(AppState.TRACING_ACTIVATED);
			} else {
				appStateLiveData.setValue(AppState.TRACING_DEACTIVATED);
			}
		});

		invalidateBluetoothState();
		invalidateTracingStatus();

		application.registerReceiver(tracingStatusBroadcastReceiver, DP3T.getUpdateIntentFilter());
		application.registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
	}

	public void invalidateTracingStatus() {
		TracingStatus status = DP3T.getStatus(getApplication());
		tracingStatusLiveData.setValue(status);
	}

	public LiveData<Boolean> getTracingEnabledLiveData() {
		return tracingEnabledLiveData;
	}

	public LiveData<Pair<Boolean, Boolean>> getSelfOrContactExposedLiveData() {
		return exposedLiveData;
	}

	public LiveData<List<TracingStatus.ErrorState>> getErrorsLiveData() {
		return errorsLiveData;
	}

	public LiveData<AppState> getAppStateLiveData() {
		return appStateLiveData;
	}

	public LiveData<TracingStatus> getTracingStatusLiveData() {
		return tracingStatusLiveData;
	}

	public LiveData<Boolean> getBluetoothEnabledLiveData() {
		return bluetoothEnabledLiveData;
	}

	public void setTracingEnabled(boolean enabled) {
		if (enabled) {
			DP3T.start(getApplication());
		} else {
			DP3T.stop(getApplication());
		}
	}

	public void invalidateService() {
		if (tracingEnabledLiveData.getValue()) {
			DP3T.stop(getApplication());
			DP3T.start(getApplication());
		}
	}

	private void invalidateBluetoothState() {
		bluetoothEnabledLiveData.setValue(DeviceFeatureHelper.isBluetoothEnabled());
	}

	@Override
	protected void onCleared() {
		getApplication().unregisterReceiver(tracingStatusBroadcastReceiver);
		getApplication().unregisterReceiver(bluetoothReceiver);
	}

}
