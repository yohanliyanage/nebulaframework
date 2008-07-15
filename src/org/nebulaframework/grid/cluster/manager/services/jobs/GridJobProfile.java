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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.ResultCallback;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.future.GridJobFutureServerImpl;
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
	private GridJob<? extends Serializable, ? extends Serializable> job; 
	
	private GridJobFutureServerImpl future; // GridJobFuture for the Job
	private GridArchive archive; // If exists, the GridArchive of Job

	private JobExecutionManager executionManager; 
	private boolean stopped;

	private ResultCallback callbackProxy; // Intermediate Results Callback
	private ExecutorService callbackExecutor; // Thread Pool Executor for callbacks
	
	// Tasks of GridJob, against TaskId (Sequence Number)
	private Map<Integer, GridTask<?>> taskMap = Collections
			.synchronizedMap(new HashMap<Integer, GridTask<?>>());

	// Results for GridTasks, against TaskId (Sequence Number)
	private Map<Integer, GridTaskResult> resultMap = Collections
			.synchronizedMap(new HashMap<Integer, GridTaskResult>());

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
	public GridJob<?, ?> getJob() {
		return job;
	}

	/**
	 * Sets the {@code GridJob} instance for the Job.
	 * 
	 * @param job
	 *            {@code GridJob} instance
	 */
	public void setJob(GridJob<?, ?> job) {
		this.job = job;
	}

	/**
	 * Returns the {@code GridJobFutureImpl} for the Job.
	 * 
	 * @return GridJobFuture Implementation
	 */
	public GridJobFutureServerImpl getFuture() {
		return future;
	}

	/**
	 * Sets the {@code GridJobFutureImpl} for the Job.
	 * 
	 * @param future
	 *            GridJobFuture Implementation
	 */
	public void setFuture(GridJobFutureServerImpl future) {
		this.future = future;
	}

	/**
	 * Adds the {@code GridTaskResult} for the given {@code taskId}
	 * to the Results collection and removes the {@code GridTask} from
	 * active tasks collection.
	 * <p>
	 * Returns the outstanding task count.
	 * 
	 * @param taskId taskId
	 * @param result {@code GridTaskResult}
	 * 
	 * @return Outstanding task count
	 */
	public synchronized int addResultAndRemoveTask(int taskId,
			GridTaskResult result) {

		// If no such taskId in taskMap
		if (!this.taskMap.containsKey(taskId)) {
			throw new IllegalArgumentException(
					"No such Task in TaskMap for Task Id :" + taskId);
		}
		
		// Remove Task from Task Map
		this.taskMap.remove(taskId);
		
		// Add result to Result Map
		this.resultMap.put(taskId, result);

		// Fire intermediate result callback
		fireCallback(result.getResult());
		
		// return outstanding task count
		return taskMap.size();
	}

	/**
	 * Fires the intermediate result callback (proxy), with
	 * the result value, if it is available.
	 * 
	 * @param result new result
	 */
	public void fireCallback(final Serializable result) {

		// If no callback registered, return
		if (callbackProxy == null)
			return;

		// Invoke onResult on callback with result
		callbackExecutor.submit(new Runnable() {
			public void run() {
				try {
					callbackProxy.onResult(result);
					
				} catch (Exception e) {
					// Ignore if Job has been stopped
					if (!stopped) {
						log.warn("[ResultCallback] Exception on Client Side", e);
					}
				}
			}
		});
		
	}

	/**
	 * Adds a {@code GridTask} to the outstanding Tasks collection.
	 * 
	 * @param taskId taskId
	 * @param task {@code GridTask}
	 */
	public synchronized void addTask(int taskId, GridTask<?> task) {
		this.taskMap.put(taskId, task);
	}

	/**
	 * Removes a {@code GridTask} from the outstanding Tasks collection.
	 * In order to remove a task due to successful execution,
	 * use {@link #addResultAndRemoveTask(int, GridTaskResult)} instead.
	 * 
	 * @param taskId taskId
	 * @return removed {@code GridTask} 
	 */
	public synchronized GridTask<?> removeTask(int taskId) {
		return this.taskMap.remove(taskId);
	}

	/**
	 * Returns the {@code GridTask} for given {@code taskId}.
	 * 
	 * @param taskId taskId
	 * @return GridTask for given taskId
	 */
	public GridTask<?> getTask(int taskId) {
		return this.taskMap.get(taskId);
	}

	/**
	 * Returns the outstanding Task Count.
	 * That is, the remaining tasks in the Tasks collection,
	 * without results.
	 * 
	 * @return outstanding task count
	 */
	public synchronized int getTaskCount() {
		return this.taskMap.size();
	}

	/**
	 * Returns the total of results collected.
	 * 
	 * @return number of results collected
	 */
	public synchronized int getResultCount() {
		return this.resultMap.size();
	}

	/**
	 * Returns the collected results as a collection.
	 * 
	 * @return results
	 */
	public synchronized Collection<GridTaskResult> getResults() {
		return this.resultMap.values();
	}

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
	 * Returns a {@code boolean} value indicating whether the Job is an archived
	 * job.
	 * 
	 * @return {@code true} if the Job is Archived, {@code false} otherwise.
	 */
	public boolean isArchived() {
		return this.archive != null;
	}

	/**
	 * Returns {@code true} if this {@code GridJob} has been stopped.
	 * 
	 * @return value {@code true} if stopped, {@code false} otherwise
	 */
	public boolean isStopped() {
		return stopped;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.jobId + (isArchived() ? " [Archived]" : " [Non-Archive]");
	}

	/**
	 * Sets the {@code ResultCallback} which is to be invoked
	 * for each intermediate result received. This method 
	 * accepts the proxy object which can be used to pass the
	 * result to client.
	 * <p>
	 * This method also creates the {@code SingleThreadExecutor}
	 * which is used to execute the notification process.
	 * 
	 * @param proxy {@code ResultCallback} proxy
	 * 
	 * @see ResultCallback
	 */
	public void setResultCallback(ResultCallback proxy) {
		this.callbackProxy = proxy;
		this.callbackExecutor = Executors.newSingleThreadExecutor();
	}

	/**
	 * Sets the {@code JobExecutionManager} of this {@code GridJob}.
	 * 
	 * @param executionManager
	 *            Execution Manager
	 */
	public void setExecutionManager(JobExecutionManager executionManager) {
		this.executionManager = executionManager;
	}

	/**
	 * Cancels the {@code GridJob} represented by this {@code GridJobProfile}.
	 * 
	 * @return a {@code boolean} value indicating success ({@code true}) /
	 *         failure ({@code false}).
	 */
	public boolean cancel() {
		if (this.executionManager == null) {
			log.warn("Cannot Stop Job as No Execution Manager exsits");
			return false;
		}
		this.stopped = true;
		return this.executionManager.cancel();
	}

}
