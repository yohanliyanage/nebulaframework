package org.nebulaframework.core.grid.cluster.manager.services.facade;

import java.io.Serializable;
import java.util.UUID;

import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.job.GridJob;

public class ClusterManagerServicesFacadeImpl implements ClusterManagerServicesFacade{

	private ClusterManager cluster;
	
	public ClusterManagerServicesFacadeImpl(ClusterManager cluster) {
		super();
		this.cluster = cluster;
	}

	public String submitJob(UUID owner, GridJob<? extends Serializable> job) {
		return this.cluster.getJobService().submitJob(owner, job);
	}

	public boolean requestJob(String jobId) {
		return this.cluster.getJobService().requestJob(jobId);
	}

}
