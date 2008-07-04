package org.nebulaframework.core.grid.cluster.node.services.job.submission;

import java.io.Serializable;

import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.future.GridJobFuture;

public interface JobSubmissionService {
	public GridJobFuture submitJob(GridJob<? extends Serializable> job);
	public GridJobFuture[] submitArchive(GridArchive archive);
}
