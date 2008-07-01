package org.nebulaframework.core.grid.cluster.node.services.jobs;

import java.io.Serializable;

import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.future.GridJobFuture;

public interface JobSubmissionService {
	public GridJobFuture submitJob(GridJob<? extends Serializable> job);
}
