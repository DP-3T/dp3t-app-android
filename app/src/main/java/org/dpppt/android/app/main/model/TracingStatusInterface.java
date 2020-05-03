package org.dpppt.android.app.main.model;

import org.dpppt.android.app.debug.model.DebugAppState;

public interface TracingStatusInterface {

	boolean isInfected();

	boolean isExposed();

	void setDebugAppState(DebugAppState debugAppState);

	DebugAppState getDebugAppState();

	AppState getAppState();

}
