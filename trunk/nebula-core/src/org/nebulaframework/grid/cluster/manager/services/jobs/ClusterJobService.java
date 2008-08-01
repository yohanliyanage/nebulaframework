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

import java.util.UUID;

import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.ResultCallback;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.deploy.GridJobInfo;
import org.nebulaframework.core.job.exceptions.GridJobPermissionDeniedException;
import org.nebulaframework.core.job.exceptions.GridJobRejectionException;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.node.GridNodeProfile;
import org.nebulaframework.grid.cluster.node.services.job.submission.JobSubmissionService;

/**
 * {@code ClusterJobService} is responsible for the {@code GridJob} submission and
 * execution, with in a {@code ClusterManager}. This interface defines the API for 
 * implementations of this service.
 * <p>
 * Invocation of this API is done by the {@link JobSubmissionService} implementation
 * of a {@code GridNode}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClusterManager
 * @see GridJob
 *
 */
public interface ClusterJobService {

	/**
	 * Submits a {@code GridJob} to the {@code ClusterManager}, which 
	 * results in job enqueue and infrastructure allocation.
	 * 
	 * @param owner Owner of Job (Node Id)
	 * @param className GridJob Class Name
	 * @param classData Serialized GridJob Object Data
	 * 
	 * @return String JobId assigned for the submitted Job
	 * 
	 * @throws GridJobRejectionException if job is rejected
	 */
	public String submitJob(UUID owner, String className, byte[] classData) throws GridJobRejectionException;
	
	/**
	 * Submits a {@code GridJob} to the {@code ClusterManager}, which 
	 * results in job enqueue and infrastructure allocation, and  also
	 * allows to specify a {@code ResuleCallback}, by specifying the
	 * JMS {@code Queue Name} to be used for communication with callback.
	 * <p>
	 * For more information about callbacks, refer to {@link ResultCallback}, 
	 * {@link JobSubmissionService}.
	 * 
	 * @param owner Owner of Job (Node Id)
	 * @param className GridJob Class Name
	 * @param classData Serialized GridJob Object Data
	 * @param resultCallbackQueue JMS QueueName used for ResultCallback 
	 * communication
	 * 
	 * @return String JobId assigned for the submitted Job
	 * 
	 * @throws GridJobRejectionException if job is rejected
	 */
	public String submitJob(UUID owner,  String className, byte[] classData,  String resultCallbackQueue) throws GridJobRejectionException;
	
	/**
	 * Submits an <i>archived</i> {@code GridJob} to the {@code ClusterManager}, 
	 * which results in job enqueue and infrastructure allocation. 
	 * This overloaded version accepts a {@code GridArchive}, 
	 * which contains the submitted {@code GridJob}.
	 * 
	 * @param owner Owner of Job (Node Id)
	 * @param className GridJob Class Name
	 * @param classData Serialized GridJob Object Data
	 * @param archive GridArchive, if applicable. This may be {@code null}.
	 * 
	 * @return String JobId assigned for the submitted Job
	 * 
	 * @throws GridJobRejectionException if job is rejected
	 */	
	public String submitJob(UUID owner,
			 String className, byte[] classData, GridArchive archive) throws GridJobRejectionException;
	
	/**
	 * Submits an <i>archived</i> {@code GridJob} with the given
	 * result callback queue, to the {@code ClusterManager}, 
	 * which results in job enqueue and infrastructure allocation.
	 * <p>
	 * This overloaded version accepts a {@code GridArchive}, 
	 * which contains the submitted {@code GridJob} and  also
	 * allows to specify a {@code ResuleCallback}, by specifying the
	 * JMS {@code Queue Name} to be used for communication with callback.
	 * <p>
	 * For more information about callbacks, refer to {@link ResultCallback}, 
	 * {@link JobSubmissionService}.
	 * 
	 * @param owner Owner of Job (Node Id)
	 * @param job GridJob
	 * @param archive GridArchive, if applicable. This may be {@code null}.
	 * @param resultCallbackQueue JMS QueueName used for ResultCallback 
	 * communication
	 * 
	 * @return String JobId assigned for the submitted Job
	 * 
	 * @throws GridJobRejectionException if job is rejected
	 */
	public String submitJob(UUID owner,
			 String className, byte[] classData, GridArchive archive, String resultCallbackQueue) throws GridJobRejectionException;
	
	/**
	 * Requests permission for the invoking {@code GridNode} 
	 * to participate in specified {@code GridJob}.
	 * 
	 * @param jobId JobId of GridJob, which the invoking {@code GridNode} expects to join.
	 * @param nodeProfile {@code GridNode}s profile
	 * 
	 * @return GridJobInfo Grid Job Information, if request accepted.
	 * 
	 * @throws GridJobPermissionDeniedException if permission denied
	 */
	public GridJobInfo requestJob(String jobId, GridNodeProfile nodeProfile) throws GridJobPermissionDeniedException;
	
	/**
	 * Requests next available {@code GridJob}. If no {@code GridJob} is available,
	 * this method will return {@code null}.
	 * 
	 * @param nodeProfile {@code GridNode}s profile
	 * 
	 * @return If available, {@code GridJobInfo}, or {@code null} if no {@code GridJob}
	 * is available for which the given node can participate.
	 */
	public GridJobInfo requestNextJob(GridNodeProfile nodeProfile);
}