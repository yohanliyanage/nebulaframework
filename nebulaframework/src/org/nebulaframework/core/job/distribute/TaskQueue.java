package org.nebulaframework.core.job.distribute;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TaskQueue {

	private static Log log = LogFactory.getLog(TaskQueue.class);
	private UUID jobId;
	private Queue<RemoteGridTask> tasks = new LinkedList<RemoteGridTask>();
	private JobTaskTracker tracker;

	public TaskQueue(UUID jobId, JobTaskTracker tracker) {
		super();
		this.jobId = jobId;
		this.tracker = tracker;
		this.tracker.setTaskQueue(this);
	}

	public UUID getJobId() {
		return jobId;
	}

	public synchronized void addTask(RemoteGridTask task) {
		log.debug("Task added " + task.getJobId() + "|" + task.getTaskId());
		this.tasks.add(task);
	}

	public synchronized RemoteGridTask getTask(String worker) {
		log.debug("Task Requested for Job " + this.getJobId() + " by Node "
				+ worker);
		RemoteGridTask task = null;

		task = tasks.poll();
		if (task != null) {
			tracker.dispatchTask(task.getTaskId(), worker);
		}

		return task;

	}

	public boolean isEmpty() {
		return this.tasks.isEmpty();
	}
}
