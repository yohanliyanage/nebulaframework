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

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TaskQueue for a Job.
 * @author Yohan Liyanage
 *
 */
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
