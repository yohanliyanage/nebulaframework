/*
 * Copyright (C) 2008 Yohan Liyanage. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.nebulaframework.grid.cluster.manager.services.jobs;

import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.grid.cluster.manager.ClusterManager;

/**
 * Internal interface definition for {@code ClusterJobService}, which
 * is responsible for the {@code GridJob} submission and execution, with 
 * in a {@code ClusterManager}. 
 * <p>
 * Internal interface extends the public interface,
 * but allows to access operations which are not exposed by the public API.
 * <p>
 * <b>Warning : </b>This is to be used by the internal system only, and is not a 
 * part of the public API. Use of this API is strongly discouraged. For the 
 * public API of this service, refer to {@link ClusterJobService}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClusterManager
 * @see GridJob
 *
 */
public interface InternalClusterJobService extends ClusterJobService {

	/**
	 * Attempts to cancel the given {@code GridJob}.
	 * 
	 * @param jobId JobId of Job to be canceled
	 * @return value {@code true} if successful, {@code false} otherwise
	 */
	public boolean cancelJob(String jobId);
	
	/**
	 * Notifies to GridNodes that a particular GridJob has finished execution.
	 * 
	 * @param jobId JobId of the finished GridJob
	 */
	public void notifyJobEnd(String jobId);

	/**
	 * Notifies to GridNodes that a particular GridJob has been canceled.
	 * 
	 * @param jobId JobId of the canceled GridJob.
	 */
	public void notifyJobCancel(String jobId);



	/**
	 * Returns the {@code GridJobProfile} for a given {@code GridJob}.
	 * 
	 * @param jobId
	 *            JobId of the {@code GridJob}
	 * @return {@code GridJobProfile} for the specified {@code GridJob}.
	 */
	public GridJobProfile getProfile(String jobId);
	
	/**
	 * Returns a {@code boolean} value indicating whether a given JobId refers
	 * to an active {@code GridJob} of this service instance.
	 * 
	 * @param jobId
	 *            JobId of the {@code GridJob}
	 * 
	 * @return {@code true} if the {@code GridJob} is active, {@code false}
	 *         otherwise.
	 */
	public boolean isActiveJob(String jobId);
	
	/**
	 * Returns the total number of GridJobs executed by
	 * this ClusterManager.
	 * 
	 * @return Finished Job Count
	 */
	public int getFinishedJobCount();
	
	/**
	 * Returns the total number of Active GridJobs in
	 * this ClusterManager.
	 * 
	 * @return Active Job Count
	 */
	public int getActiveJobCount();
}
