package org.dpppt.android.app.debug;

import org.dpppt.android.app.debug.model.DebugAppState;
import org.dpppt.android.app.main.model.AppState;
import org.dpppt.android.app.main.model.TracingStatusInterface;
import org.dpppt.android.sdk.TracingStatus;

public class TracingStatusWrapper implements TracingStatusInterface {

	private DebugAppState debugAppState = DebugAppState.NONE;
	private TracingStatus status;

	public TracingStatusWrapper(DebugAppState debugAppState) {
		this.debugAppState = debugAppState;
	}

	public void setStatus(TracingStatus status) {
		this.status = status;
	}

	@Override
	public boolean isReportedAsExposed() {
		return status.isReportedAsExposed() || debugAppState == DebugAppState.REPORTED_EXPOSED;
	}

	@Override
	public boolean wasContactExposed() {
		return status.wasContactExposed() || debugAppState == DebugAppState.CONTACT_EXPOSED;
	}

	@Override
	public void setDebugAppState(DebugAppState debugAppState) {
		this.debugAppState = debugAppState;
	}

	@Override
	public DebugAppState getDebugAppState() {
		return debugAppState;
	}

	@Override
	public AppState getAppState() {
		boolean hasError = status.getErrors().size() > 0 || !(status.isAdvertising() || status.isReceiving());
		switch (debugAppState) {
			case NONE:
				if (status.isReportedAsExposed() || status.wasContactExposed()) {
					return hasError ? AppState.EXPOSED_ERROR : AppState.EXPOSED;
				} else if (hasError) {
					return AppState.ERROR;
				} else {
					return AppState.TRACING;
				}
			case HEALTHY:
				return hasError ? AppState.ERROR : AppState.TRACING;
			case REPORTED_EXPOSED:
			case CONTACT_EXPOSED:
				return hasError ? AppState.EXPOSED_ERROR : AppState.EXPOSED;
		}
		throw new IllegalStateException("Unkown debug AppState: " + debugAppState.toString());
	}

}
