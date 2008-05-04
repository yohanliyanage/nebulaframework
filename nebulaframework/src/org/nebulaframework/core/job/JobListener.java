package org.nebulaframework.core.job;

import java.io.Serializable;
import java.util.UUID;

import org.nebulaframework.core.job.distribute.ResultQueue;
import org.nebulaframework.core.job.distribute.TaskQueue;

public interface JobListener {
	public <T extends Serializable> void startJob(UUID jobId, JobPriority priority, TaskQueue taskQueue, ResultQueue<T> resultQueue);
	public void endJob(UUID jobId);
}
