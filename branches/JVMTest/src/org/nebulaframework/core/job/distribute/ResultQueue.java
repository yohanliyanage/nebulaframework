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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.GridTaskResult;

/**
 * Result Queue which holds Task results for a Job.
 * @author Yohan Liyanage
 *
 * @param <T>
 */
public class ResultQueue<T extends Serializable> {

	private static Log log = LogFactory.getLog(ResultQueue.class);

	private UUID jobId;
	private Queue<GridTaskResult> results = new LinkedList<GridTaskResult>();
	private JobTaskTracker tracker;
	private List<TaskResultListener> resultListeners = new ArrayList<TaskResultListener>();

	public ResultQueue(UUID jobId, JobTaskTracker tracker) {
		super();
		this.jobId = jobId;
		this.tracker = tracker;
		this.tracker.setResultQueue(this);
	}
	

	public UUID getJobId() {
		return jobId;
	}


	public void submit(GridTaskResult result) {

		log.info("Result received by Result Queue [" + result.getJobId() + " | "+result.getTaskId() + "] from Remote Node " + result.getWorkerId());
		tracker.resultCollected(result.getTaskId(), result.getWorkerId());

		synchronized (results) {
			results.add(result);
		}

		notifyResult(result);
		
	}

	/**
	 * Notify Listeners
	 * 
	 * @param taskId
	 * @param result
	 * @param worker
	 */
	private void notifyResult(final GridTaskResult result) {
		synchronized (resultListeners) {
			for (TaskResultListener l : resultListeners) {
				l.resultCollected(result);
			}
		}		
	}
	
	public void addTaskResultListener(TaskResultListener listener) {
		synchronized (resultListeners) {
			this.resultListeners.add(listener);
		}
	}

	public void removeTaskResultListener(TaskResultListener listener) {
		synchronized (resultListeners) {
			this.resultListeners.remove(listener);
		}
	}

	public List<GridTaskResult> getResults() {
		return new ArrayList<GridTaskResult>(this.results);
	}
}
