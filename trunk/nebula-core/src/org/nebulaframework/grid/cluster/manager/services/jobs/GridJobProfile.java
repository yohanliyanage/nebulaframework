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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.ResultCallback;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.future.GridJobFutureServerImpl;
import org.nebulaframework.core.job.unbounded.UnboundedGridJob;
import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.core.task.GridTaskResult;
import org.nebulaframework.grid.GridExecutionException;
import org.nebulaframework.grid.cluster.manager.services.jobs.tracking.GridJobTaskTracker;
import org.nebulaframework.grid.cluster.node.GridNodeProfile;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;

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
	private GridJobTaskTracker taskTracker;
	
	private boolean stopped;

	private ResultCallback callbackProxy; // Intermediate Results Callback
	private ExecutorService callbackExecutor; // Thread Pool Executor for callbacks
	
	// Tasks of GridJob, against TaskId (Sequence Number)
	private Map<Integer, GridTask<?>> taskMap = Collections
			.synchronizedMap(new HashMap<Integer, GridTask<?>>());

	// Results for GridTasks, against TaskId (Sequence Number)
	private Map<Integer, GridTaskResult> resultMap = Collections
			.synchronizedMap(new HashMap<Integer, GridTaskResult>());

	// Contains the set of banned nodes (not allowed to participate for this job)
	private Set<UUID> bannedNodes = Collections.synchronizedSet(new HashSet<UUID>());
	
	// Contains the set of nodes which are executing this job
	private Set<UUID> workerNodes = Collections.synchronizedSet(new HashSet<UUID>());
	
	// Failed Task Count
	private int failedCount = 0;
	
	// Total # of Tasks (for percentage calculation)
	private int totalTasks = -1;
	

	private long startTime;	// Creation Time
	
	/**
	 * Minimum Execution Time. (Default 4 seconds).
	 * This is used to avoid thread interaction issues. If a 
	 * job finishes before this threshold, the results will
	 * be withheld for at least this amount + 1 second has been
	 * elapsed.
	 */
	public static final int MINIMUM_EXEUCTION_TIME = 2000;
	
	/**
	 * No-args constructor.
	 */
	public GridJobProfile() {
		super();
		startTime = System.currentTimeMillis();
	}

	/**
	 * Processes a request from a GridNode to participate
	 * for this GridJob.
	 * 
	 * @param nodeProfile Node Profile for a GridNode
	 * 
	 * @return boolean value indicating permission
	 */
	public boolean processRequest(GridNodeProfile nodeProfile) {
		if (bannedNodes.contains(nodeProfile.getId())) {
			return false;
		}
		workerNodes.add(nodeProfile.getId());
		
		// Add Node Failure Hook to remove workers
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {

			@Override
			public void onServiceEvent(ServiceMessage message) {
				workerNodes.remove(UUID.fromString(message.getMessage()));
			}
			
		}, 
			nodeProfile.getId().toString(), 
			ServiceMessageType.NODE_UNREGISTERED, 
			ServiceMessageType.HEARTBEAT_FAILED);
		
		return true;
	}
	
	/**
	 * Adds a banned node ID for this Job.
	 * 
	 * @param nodeId Node Id
	 */
	public void addBannedNode(UUID nodeId) {
		bannedNodes.add(nodeId);
		workerNodes.remove(nodeId);
		
		// If all nodes are banned 
		if (workerNodes.size()==0) {
			// Fail the job
			future.fail(new GridExecutionException("All Worker Nodes Failed"));
		}
	}
	
	/**
	 * Removes a given node ID from this
	 * jobs banned node list.
	 * 
	 * @param nodeId NodeId to remove from list
	 */
	public void removeBannedNode(UUID nodeId) {
		bannedNodes.remove(nodeId);
	}
	
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
	 * Returns the start time of this Job.
	 * @return Start time, as long
	 */
	public long getStartTime() {
		return startTime;
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
	 * Increments the number of failed  tasks received.
	 */
	public synchronized void failedTaskReceived() {
		failedCount++;
	}
	
	/**
	 * Returns the number of tasks failed.
	 * 
	 * @return Failed Task Count
	 */
	public int getFailedCount() {
		return failedCount;
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
	public int getTaskCount() {
		return this.taskMap.size();
	}

	/**
	 * Returns the total of results collected.
	 * 
	 * @return number of results collected
	 */
	public int getResultCount() {
		return this.resultMap.size();
	}

	
	/**
	 * Returns the total task count.
	 * 
	 * @return Total Task Count
	 */
	public int getTotalTasks() {
		
		// If Total Task count not set before
		if (totalTasks < 0) {
			
			if (job instanceof UnboundedGridJob) {
				return taskMap.size() + resultMap.size();
			} 
			else {
				
				// Job is not deployed completely, return current value
				if ((!future.isJobFinished()) && (future.getState()!= GridJobState.EXECUTING)) {
					return taskMap.size() + resultMap.size();
				}
				
				// Job is enqueued, cache result for faster access
				synchronized (this) {
					totalTasks = taskMap.size() + resultMap.size();
				}
			}
		}
		
		return totalTasks;
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
	 * Sets the {@code JobCancellationCallback} of this {@code GridJob}.
	 * 
	 * @param cancelCallback JobCancellationCallback
	 */
	public void setExecutionManager(JobExecutionManager cancelCallback) {
		this.executionManager = cancelCallback;
	}

	/**
	 * Sets the {@code GridJobTaskTracker} instance for this
	 * {@code GridJob}.
	 * 
	 * @return GridJobTaskTracker
	 */
	public GridJobTaskTracker getTaskTracker() {
		return taskTracker;
	}

	/**
	 * Sets the {@code GridJobTaskTracker} instance for this
	 * {@code GridJob}.
	 * 
	 * @param taskTracker GridJobTaskTracker
	 */
	public void setTaskTracker(GridJobTaskTracker taskTracker) {
		this.taskTracker = taskTracker;
	}

	/**
	 * Cancels the {@code GridJob} represented by this {@code GridJobProfile}.
	 * 
	 * @return a {@code boolean} value indicating success ({@code true}) /
	 *         failure ({@code false}).
	 */
	public boolean cancel() {
		
		if (this.executionManager == null) {
			log.warn("[GridJobProfile] Cannot Stop Job as No Job Execution Manager exsits");
			return false;
		}
		
		stopped = this.executionManager.cancel(this.jobId);
		
		return stopped;
	}

	
	/**
	 * Returns a percentage value (double 0-1.0) which indicates the
	 * completion percentage of this GridJob, if the GridJob is in
	 * {@code EXECUTING} state. For a finished GridJob, this returns 1.0.
	 * In all other states, this method will throw {@link IllegalStateException}.
	 * 
	 * @return Percentage Complete
	 * @throws IllegalStateException if state is not finished state or EXECUTING.
	 */
	public double percentage() throws IllegalStateException {
		
		if (future.isJobFinished()) {
			return 1.0;
		}
		
		if (future.getState()!=GridJobState.EXECUTING) {
			throw new IllegalStateException("Job is not in Executing State");
		}

		// Deduct 0.01 to avoid reaching 100% before aggregation
		double val =  ((double) getResultCount()) / getTotalTasks() - 0.01;
		
		return (val>0) ? val : 0;
	}

	/**
	 * Returns the number of active worker nodes
	 * for this GridJob.
	 * 
	 * @return worker node count
	 */
	public int getWorkerCount() {
		return this.workerNodes.size();
	}




}
