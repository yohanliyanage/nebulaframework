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

package org.nebulaframework.core.grid.cluster.manager.services.jobs;

import java.io.Serializable;
import java.util.UUID;

import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.grid.cluster.node.GridNode;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.deploy.GridJobInfo;
import org.nebulaframework.core.job.exceptions.GridJobPermissionDeniedException;
import org.nebulaframework.core.job.exceptions.GridJobRejectionException;

/**
 * <tt>ClusterJobService</tt> is responsible for the {@link GridJob} submission and
 * execution, with in a {@link ClusterManager}. This interface defines the API for 
 * implementations of this service.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 *
 */
public interface ClusterJobService {

	/**
	 * Submits a {@link GridJob} to the {@link ClusterManager}, which results in job enqueue and
	 * infrastructure allocation.
	 * 
	 * @param owner Owner of Job (Node Id)
	 * @param job GridJob
	 * 
	 * @return String <tt>JobId</tt> assigned for the submitted Job
	 * 
	 * @throws GridJobRejectionException if job is rejected
	 */
	public String submitJob(UUID owner,
			GridJob<? extends Serializable> job) throws GridJobRejectionException;
	
	/**
	 * Submits a {@link GridJob} to the {@link ClusterManager}, which results in job enqueue and
	 * infrastructure allocation. This overloaded version accepts a {@link GridArchive}, 
	 * which contains the submitted {@link GridJob}.
	 * 
	 * @param owner Owner of Job (Node Id)
	 * @param job GridJob
	 * @param archive GridArchive, if applicable. This may be <tt>null</tt>.
	 * 
	 * @return String <tt>JobId</tt> assigned for the submitted Job
	 * 
	 * @throws GridJobRejectionException if job is rejected
	 */	
	public String submitJob(UUID owner,
			GridJob<? extends Serializable> job, GridArchive archive) throws GridJobRejectionException;
	
	/**
	 * Requests permission for the invoking {@link GridNode} 
	 * to participate in specified {@link GridJob}.
	 * 
	 * @param jobId JobId of GridJob, which the invoking {@link GridNode} expects to join.
	 * 
	 * @return {@link GridJobInfo} Grid Job Information, if request accepted.
	 * 
	 * @throws GridJobPermissionDeniedException if permission denied
	 */
	// TODO Pass GridNode profile ?
	public GridJobInfo requestJob(String jobId) throws GridJobPermissionDeniedException;
	
}