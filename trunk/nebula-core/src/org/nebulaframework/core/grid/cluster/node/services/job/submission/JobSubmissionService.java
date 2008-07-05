package org.nebulaframework.core.grid.cluster.node.services.job.submission;

import java.io.Serializable;
import java.util.Map;

import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.exceptions.GridJobRejectionException;
import org.nebulaframework.core.job.future.GridJobFuture;

public interface JobSubmissionService {
	public GridJobFuture submitJob(GridJob<? extends Serializable> job) throws GridJobRejectionException;
	public Map<String,GridJobFuture> submitArchive(GridArchive archive);
}
