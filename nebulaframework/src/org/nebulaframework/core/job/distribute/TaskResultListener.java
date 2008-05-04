package org.nebulaframework.core.job.distribute;

import org.nebulaframework.core.GridTaskResult;


public interface TaskResultListener {
	public void resultCollected(GridTaskResult result);
}
