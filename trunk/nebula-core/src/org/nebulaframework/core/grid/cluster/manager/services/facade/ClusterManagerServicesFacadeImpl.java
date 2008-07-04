package org.nebulaframework.core.grid.cluster.manager.services.facade;

import java.io.Serializable;
import java.util.UUID;

import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.deploy.GridJobInfo;
import org.nebulaframework.core.job.exceptions.GridJobPermissionDeniedException;
import org.nebulaframework.core.job.exceptions.GridJobRejectionException;

public class ClusterManagerServicesFacadeImpl implements ClusterManagerServicesFacade{

	private ClusterManager cluster;
	
	public ClusterManagerServicesFacadeImpl(ClusterManager cluster) {
		super();
		this.cluster = cluster;
	}

	public String submitJob(UUID owner, GridJob<? extends Serializable> job) throws  GridJobRejectionException {
		return this.cluster.getJobService().submitJob(owner, job);
	}

	public GridJobInfo requestJob(String jobId) throws GridJobPermissionDeniedException {
		return this.cluster.getJobService().requestJob(jobId);
	}

	public String submitJob(UUID owner, GridJob<? extends Serializable> job,
			GridArchive archive) throws GridJobRejectionException {
		return this.cluster.getJobService().submitJob(owner, job, archive);
	}


}
