package org.nebulaframework.core.grid.cluster.manager.services.jobs;

import java.io.Serializable;
import java.util.UUID;

import org.nebulaframework.core.job.GridJob;

public interface ClusterJobService {

	/**
	 * Submits a Job to the Cluster Manager, which results in job enqueue and
	 * infrastructure allocation.
	 * 
	 * @param owner
	 *            Owner of Job (Node Id)
	 * @param job
	 *            Job
	 * @return String indicating Job Id
	 */
	public String submitJob(UUID owner,
			GridJob<? extends Serializable> job);

	
	/**
	 * Requests permission to participate in specified Job
	 * @param jobId JobId of Job
	 * @return boolean value indicating permission (true for granted)
	 */
	// TODO Pass GridNode profile ?
	public boolean requestJob(String jobId);

	
	/**
	 * Requests the ClassName for specified Job, to do remote loading if needed
	 * @param jobId JobId of Job
	 * @return String name of GridJob Class
	 */
	public String requestJobClassName(String jobId);
}