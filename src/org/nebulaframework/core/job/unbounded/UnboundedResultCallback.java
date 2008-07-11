package org.nebulaframework.core.job.unbounded;

import java.io.Serializable;

// TODO Fix Doc || Client Side Stuff here
public interface UnboundedResultCallback<R extends Serializable> {
	public void onResult(R result);
}
