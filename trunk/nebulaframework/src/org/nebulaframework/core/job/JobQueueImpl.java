package org.nebulaframework.core.job;

import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JobQueueImpl implements JobQueue {

	private static Log log = LogFactory.getLog(JobQueueImpl.class);
	
	private static JobQueueImpl instance = new JobQueueImpl();
	private Queue<RemoteGridJob> queue = new PriorityQueue<RemoteGridJob>();

	public static JobQueueImpl getInstance() {
		return instance;
	}

	@Override
	public void add(RemoteGridJob job) {
		synchronized (queue) {
			if (!queue.contains(job)) {
				queue.add(job);
				job.getFuture().setState(GridJobState.ENQUEUED);
				log.info("Job " + job.getJobId() + " is enqueued");
			} else {
				throw new IllegalArgumentException(
						"A Job with given UUID already in Queue");
			}
		}
	}

	@Override
	public RemoteGridJob nextJob() {
		RemoteGridJob nextElement = null;

		synchronized (queue) {
			nextElement = this.queue.poll();
		}

		if (nextElement != null) {
				log.debug("Returning next Job : " + nextElement.getJobId());	
		}
		
		return nextElement;
	}

	@Override
	public boolean remove(RemoteGridJob job) {
		synchronized (queue) {
			return queue.remove(job);
		}
	}



}
