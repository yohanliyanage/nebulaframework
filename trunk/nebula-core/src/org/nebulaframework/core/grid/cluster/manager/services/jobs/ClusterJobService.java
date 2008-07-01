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
	public abstract String submitJob(UUID owner,
			GridJob<? extends Serializable> job);

}