package org.nebulaframework.core.grid.cluster.manager.services.facade;

import java.io.Serializable;
import java.util.UUID;

import org.nebulaframework.core.grid.cluster.manager.services.jobs.ClusterJobService;
import org.nebulaframework.core.job.GridJob;

public interface ClusterManagerServicesFacade extends ClusterJobService{
	/**
	 * {@inheritDoc}
	 */
	public String submitJob(UUID owner,
			GridJob<? extends Serializable> job);

	/**
	 * {@inheritDoc}
	 */
	// TODO Pass GridNode profile ?
	public boolean requestJob(String jobId);
	
	public String requestJobClassName(String jobId);
}
