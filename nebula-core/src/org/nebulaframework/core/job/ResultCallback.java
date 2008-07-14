package org.nebulaframework.core.job;

import java.io.Serializable;

public interface ResultCallback {
	void onResult(Serializable result);
}
