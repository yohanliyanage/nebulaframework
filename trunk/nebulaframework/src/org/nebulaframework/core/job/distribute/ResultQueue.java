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
