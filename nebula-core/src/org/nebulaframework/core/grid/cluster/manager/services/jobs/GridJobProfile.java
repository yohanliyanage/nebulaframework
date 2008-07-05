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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.future.GridJobFutureImpl;
import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.core.task.GridTaskResult;

/**
 * <p><tt>GridJobProfile</tt> is an internal representation of a submitted {@link GridJob}, maintained by
 * the {@link ClusterManager}. This class holds the {@link GridJob}, and its meta data, as well as
 * execution details such as tasks and results.</p>
 * 
 * <p>This class is not a part of the Public API of the Nebula Framework, and it is intended to be used
 * internally with in the {@link ClusterManager} only.</p>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class GridJobProfile {
	
	private String jobId;							// JobId of GridJob
	private UUID owner;								// Owner Node Id (Submitter)
	private GridJob<? extends Serializable> job;	// GridJob class Reference
	private GridJobFutureImpl future;				// GridJobFuture for the Job
	private GridArchive archive;					// If exists, the GridArchive of Job
	
	// Tasks of GridJob, against TaskId (Sequence Number)
	private Map<Integer, GridTask<?>> taskMap = new HashMap<Integer, GridTask<?>>();
	
	// Results for GridTasks, against TaskId (Sequence Number)
	private Map<Integer, GridTaskResult> resultMap = new HashMap<Integer, GridTaskResult>();
	
	/**
	 * Returns JobId of the Job
	 * @return String JobId
	 */
	public String getJobId() {
		return jobId;
	}

	/**
	 * Sets the JobId for the Job
	 * @param jobId
	 */
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	/**
	 * Returns the NodeId of the owner node (submitter).
	 * 
	 * @return UUID NodeId
	 */
	public UUID getOwner() {
		return owner;
	}

	/**
	 * Sets the NodeId of the owner node (submitter).
	 * @param owner UUID NodeId
	 */
	public void setOwner(UUID owner) {
		this.owner = owner;
	}

	/**
	 * Returns the {@link GridJob} instance of the Job.
	 * 
	 * @return {@link GridJob} instance for the Job
	 */
	public GridJob<? extends Serializable> getJob() {
		return job;
	}

	/**
	 * Sets the {@link GridJob} instance for the Job.
	 * @param job {@link GridJob} instance
	 */
	public void setJob(GridJob<? extends Serializable> job) {
		this.job = job;
	}

	/**
	 * Returns the {@link GridJobFutureImpl} for the Job.
	 * 
	 * @return GridJobFuture Implementation
	 */
	public GridJobFutureImpl getFuture() {
		return future;
	}

	/**
	 * Sets the {@link GridJobFutureImpl} for the Job.
	 * @param future GridJobFuture Implementation
	 */
	public void setFuture(GridJobFutureImpl future) {
		this.future = future;
	}

	/**
	 * Returns the Map of GridTasks for the Job.
	 *  
	 * @return GridTasks Map
	 */
	public Map<Integer, GridTask<?>> getTaskMap() {
		return taskMap;
	}

	/**
	 * Returns the Map of GridTaskResults for the Job.
	 *  
	 * @return GridTaskResults Map
	 */
	public Map<Integer, GridTaskResult> getResultMap() {
		return resultMap;
	}

	/**
	 * Returns the {@link GridArchive} of the Job, or <tt>null</tt> if not applicable.
	 * 
	 * @return {@link GridArchive} or <tt>null</tt>
	 */
	public GridArchive getArchive() {
		return archive;
	}

	/**
	 * Sets the {@link GridArchive} for the Job.
	 * 
	 * @param archive {@link GridArchive}
	 */
	public void setArchive(GridArchive archive) {
		this.archive = archive;
	}

	/**
	 * Returns a <tt>boolean</tt> value indicating whether the Job is an archived job.
	 * @return <tt>true</tt> if the Job is Archived, <tt>false</tt> otherwise.
	 */
	public boolean isArchived() {
		return this.archive!=null;
	}
	
	

}
