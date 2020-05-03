package org.dpppt.android.app.debug;

import org.dpppt.android.app.debug.model.DebugAppState;
import org.dpppt.android.app.main.model.AppState;
import org.dpppt.android.app.main.model.TracingStatusInterface;
import org.dpppt.android.sdk.InfectionStatus;
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
	public boolean isInfected() {
		return InfectionStatus.INFECTED.equals(status.getInfectionStatus());
	}

	@Override
	public boolean isExposed() {
		return InfectionStatus.EXPOSED.equals(status.getInfectionStatus());
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
		if (
				InfectionStatus.INFECTED.equals(status.getInfectionStatus()) ||
						InfectionStatus.EXPOSED.equals(status.getInfectionStatus())
		) {
			return hasError ? AppState.EXPOSED_ERROR : AppState.EXPOSED;
		} else if (hasError) {
			return AppState.ERROR;
		} else {
			return AppState.TRACING;
		}
	}

}
