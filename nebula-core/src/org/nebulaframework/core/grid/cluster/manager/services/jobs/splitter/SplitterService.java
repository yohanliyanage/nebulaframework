package org.nebulaframework.core.grid.cluster.manager.services.jobs.splitter;

import java.io.Serializable;

import org.nebulaframework.core.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.core.task.GridTask;

public interface SplitterService {
	public void startSplitter(GridJobProfile profile);
	public void reEnqueueTask(final String jobId, final int taskId, GridTask<? extends Serializable> task);
}
