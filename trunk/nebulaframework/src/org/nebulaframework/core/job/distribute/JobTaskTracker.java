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
package org.nebulaframework.core.job.distribute;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Tracks Task status for Jobs in execution.
 * @author Yohan Liyanage
 *
 */
public class JobTaskTracker {

	private Log log = LogFactory.getLog(JobTaskTracker.class);
	
	private UUID jobId;
	private TaskQueue taskQueue;
	private ResultQueue<? extends Serializable> resultQueue;
	private boolean jobComplete;
	
	private Map<UUID, String> pendingTasks = new HashMap<UUID, String> ();
	
	public JobTaskTracker(UUID jobId) {
		super();
		this.jobId = jobId;
		log.debug("Tracker created for Job " + jobId);
	}

	public UUID getJobId() {
		return jobId;
	}
	
	
	public TaskQueue getTaskQueue() {
		return taskQueue;
	}

	public void setTaskQueue(TaskQueue taskQueue) {
		log.warn("SETTING TASK QUEUE to " + taskQueue);
		this.taskQueue = taskQueue;
		this.taskQueue.toString();
	}

	public ResultQueue<? extends Serializable> getResultQueue() {
		return resultQueue;
	}

	public <T extends Serializable> void setResultQueue(ResultQueue<T> resultQueue) {
		this.resultQueue = resultQueue;
	}

	public synchronized void dispatchTask(UUID taskId, String worker) {
		log.debug("Task Dispatched : " + taskId + " to " + worker);
		this.pendingTasks.put(taskId, worker);
	}
	
	public synchronized boolean containsTask(UUID taskId) {
		return this.pendingTasks.containsKey(taskId);
	}
	
	public synchronized void resultCollected(UUID taskId, String worker) {
		
		if (! this.pendingTasks.containsKey(taskId)) {
			throw new IllegalArgumentException("UUID " + taskId + " is invalid !");
		}
		
		log.debug("Result Collected : " + taskId + " from " + worker);
		this.pendingTasks.remove(taskId);
		
		//If TaskQueue is Empty and there's no running job, then it is job done
		if (this.taskQueue.isEmpty() && this.pendingTasks.isEmpty()) {
			log.warn("JOB COMPLETE!");
			jobComplete = true;
		}
	}
	
	public synchronized boolean isJobComplete() {
		return this.jobComplete;
	}
}
