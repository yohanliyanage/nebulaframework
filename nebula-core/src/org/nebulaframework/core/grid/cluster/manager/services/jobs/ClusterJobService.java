package org.nebulaframework.core.grid.cluster.manager.services.jobs;

import java.io.Serializable;
import java.util.UUID;

import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.deploy.GridJobInfo;
import org.nebulaframework.core.job.exceptions.GridJobPermissionDeniedException;
import org.nebulaframework.core.job.exceptions.GridJobRejectionException;

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
	 * @throws GridJobRejectionException if job is rejected
	 */
	public String submitJob(UUID owner,
			GridJob<? extends Serializable> job) throws GridJobRejectionException;
	
	/**
	 * Submits a Job to the Cluster Manager, which results in job enqueue and
	 * infrastructure allocation. This overloaded version accepts a GridArchive.
	 * 
	 * @param owner
	 *            Owner of Job (Node Id)
	 * @param job
	 *            Job
	 * @param archive GridArchive, if exists. This may be <tt>null</tt>.
	 * @return String indicating Job Id
	 * @throws GridJobRejectionException if job is rejected
	 */	
	public String submitJob(UUID owner,
			GridJob<? extends Serializable> job, GridArchive archive) throws GridJobRejectionException;
	
	/**
	 * Requests permission to participate in specified Job
	 * @param jobId JobId of Job
	 * @return {@link GridJobInfo} Grid Job Information
	 * @throws GridJobPermissionDeniedException if permission denied
	 */
	// TODO Pass GridNode profile ?
	public GridJobInfo requestJob(String jobId) throws GridJobPermissionDeniedException;
	
}