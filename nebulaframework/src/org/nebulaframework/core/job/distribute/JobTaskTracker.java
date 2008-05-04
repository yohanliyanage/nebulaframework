package org.nebulaframework.core.job.distribute;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
