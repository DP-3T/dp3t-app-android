package org.dpppt.android.app.debug;

import org.dpppt.android.app.debug.model.DebugAppState;
import org.dpppt.android.app.main.model.AppState;
import org.dpppt.android.app.main.model.TracingStatusInterface;
import org.dpppt.android.sdk.TracingStatus;

public class TracingStatusWrapper implements TracingStatusInterface {

	private DebugAppState debugAppState = DebugAppState.NONE;
	private TracingStatus status;

	public TracingStatusWrapper(DebugAppState debugAppState) {
		this.debugAppState = DebugAppState.NONE; ;
	}

	public void setStatus(TracingStatus status) {
		this.status = status;
	}

	@Override
	public boolean isReportedAsExposed() {
		return status.isReportedAsExposed();
	}

	@Override
	public boolean wasContactExposed() {
		return status.wasContactExposed();
	}

	@Override
	public void setDebugAppState(DebugAppState debugAppState) {
		//do not implement
	}

	@Override
	public DebugAppState getDebugAppState() {
		return DebugAppState.NONE;
	}

	@Override
	public AppState getAppState() {
		boolean hasError = status.getErrors().size() > 0 || !(status.isAdvertising() || status.isReceiving());
		if (status.isReportedAsExposed() || status.wasContactExposed()) {
			return hasError ? AppState.EXPOSED_ERROR : AppState.EXPOSED;
		} else if (hasError) {
			return AppState.ERROR;
		} else {
			return AppState.TRACING;
		}
	}

}
