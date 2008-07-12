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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.ResultCallback;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.future.GridJobFutureImpl;
import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.core.task.GridTaskResult;

/**
 * {@code GridJobProfile} is an internal representation of a submitted
 * {@code GridJob}, maintained by the {@code ClusterManager}. This class holds
 * the {@code GridJob}, and its meta data, as well as execution details such as
 * tasks and results.
 * <p>
 * This class is not a part of the Public API of the Nebula Framework, and it is
 * intended to be used internally with in the {@code ClusterManager} only.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class GridJobProfile {

	private static Log log = LogFactory.getLog(GridJobProfile.class);
	
	private String jobId; // JobId of GridJob
	private UUID owner; // Owner Node Id (Submitter)
	private GridJob<? extends Serializable, ? extends Serializable> job; // GridJob class Reference
	private GridJobFutureImpl future; // GridJobFuture for the Job
	private GridArchive archive; // If exists, the GridArchive of Job

	private ResultCallback callbackProxy;
	
	// Tasks of GridJob, against TaskId (Sequence Number)
	private Map<Integer, GridTask<?>> taskMap = new HashMap<Integer, GridTask<?>>();

	// Results for GridTasks, against TaskId (Sequence Number)
	private Map<Integer, GridTaskResult> resultMap = new HashMap<Integer, GridTaskResult>();

	
	/**
	 * Returns JobId of the Job
	 * 
	 * @return String JobId
	 */
	public String getJobId() {
		return jobId;
	}

	/**
	 * Sets the JobId for the Job
	 * 
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
	 * 
	 * @param owner
	 *            UUID NodeId
	 */
	public void setOwner(UUID owner) {
		this.owner = owner;
	}

	/**
	 * Returns the {@code GridJob} instance of the Job.
	 * 
	 * @return {@code GridJob} instance for the Job
	 */
	public GridJob<?,?> getJob() {
		return job;
	}

	/**
	 * Sets the {@code GridJob} instance for the Job.
	 * 
	 * @param job
	 *            {@code GridJob} instance
	 */
	public void setJob(GridJob<?,?> job) {
		this.job = job;
	}


	/**
	 * Returns the {@code GridJobFutureImpl} for the Job.
	 * 
	 * @return GridJobFuture Implementation
	 */
	public GridJobFutureImpl getFuture() {
		return future;
	}

	/**
	 * Sets the {@code GridJobFutureImpl} for the Job.
	 * 
	 * @param future
	 *            GridJobFuture Implementation
	 */
	public void setFuture(GridJobFutureImpl future) {
		this.future = future;
	}

	// TODO FixDoc
	public synchronized void addResult(int taskId, GridTaskResult result) {
		this.resultMap.put(taskId, result);
		fireCallback(result.getResult());
	}
	
	// TODO FixDoc
	public void fireCallback(final Serializable result) {
		
		if (callbackProxy == null) return;
		
		new Thread(new Runnable() {
			public void run() {
				try {
					callbackProxy.onResult(result);
				} catch (Exception e) {
					log.warn("[ResultCallback] Exception on Client Side", e);
				}
			}
		}).start();
	}

	// TODO FixDoc
	public synchronized void addTask(int taskId, GridTask<?> task) {
		this.taskMap.put(taskId, task);
	}
	
	// TODO FixDoc
	public synchronized GridTask<?> removeTask(int taskId) {
		return this.taskMap.remove(taskId);
	}
	
	// TODO FixDoc
	public GridTask<?> getTask(int taskId) {
		return this.taskMap.get(taskId);
	}
	
	// TODO FixDoc
	public synchronized int getTaskCount() {
		return this.taskMap.size();
	}
	
	// TODO FixDoc
	public synchronized int getResultCount() {
		return this.resultMap.size();
	}
	
	// TODO FixDoc
	public synchronized Collection<GridTaskResult> getResults() {
		return this.resultMap.values();
	}
	
//	
//	/**
//	 * Returns the Map of GridTasks for the Job.
//	 * 
//	 * @return GridTasks Map
//	 */
//	public Map<Integer, GridTask<?>> getTaskMap() {
//		return taskMap;
//	}
//
//	/**
//	 * Returns the Map of GridTaskResults for the Job.
//	 * 
//	 * @return GridTaskResults Map
//	 */
//	public Map<Integer, GridTaskResult> getResultMap() {
//		return resultMap;
//	}

	/**
	 * Returns the {@code GridArchive} of the Job, or {@code null} if not
	 * applicable.
	 * 
	 * @return {@code GridArchive} or {@code null}
	 */
	public GridArchive getArchive() {
		return archive;
	}

	/**
	 * Sets the {@code GridArchive} for the Job.
	 * 
	 * @param archive
	 *            {@code GridArchive}
	 */
	public void setArchive(GridArchive archive) {
		this.archive = archive;
	}

	/**
	 * Returns a {@code boolean} value indicating whether the 
	 * Job is an archived job.
	 * 
	 * @return {@code true} if the Job is Archived, {@code false}
	 *         otherwise.
	 */
	public boolean isArchived() {
		return this.archive != null;
	}

	// TODO FixDoc
	@Override
	public String toString() {
		return this.jobId + (isArchived()?" [Archived]" : " [Non-Archive]");
	}

	// TODO FixDoc
	public void setResultCallback(ResultCallback proxy) {
		this.callbackProxy = proxy;
	}

	
}
