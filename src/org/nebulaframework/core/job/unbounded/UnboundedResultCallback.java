package org.nebulaframework.core.job.unbounded;

import java.io.Serializable;

import org.nebulaframework.core.job.ResultCallback;

// TODO Fix Doc || Client Side Stuff here
public interface UnboundedResultCallback extends ResultCallback {
	public void onResult(Serializable result);
}
