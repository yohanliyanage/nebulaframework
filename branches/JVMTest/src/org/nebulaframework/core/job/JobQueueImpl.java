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
package org.nebulaframework.core.job;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Concrete Implementation of {@link JobQueue} interface.
 * This uses a {@link PriorityBlockingQueue} as the internal queue.
 * This class is a singleton. The instance can be obtained through {@link JobQueueImpl#getInstance()} method.
 * 
 * @author Yohan Liyanage
 */
public class JobQueueImpl implements JobQueue {

	private static Log log = LogFactory.getLog(JobQueueImpl.class);
	
	// Singleton Instance
	private static JobQueueImpl instance = new JobQueueImpl();
	
	// Internal Priority Blocking Queue
	private BlockingQueue<RemoteGridJob> queue = new PriorityBlockingQueue<RemoteGridJob>();

	/**
	 * Returns the instance of {@link JobQueueImpl}.
	 * @return {@link JobQueueImpl} instance.
	 */
	public static JobQueueImpl getInstance() {
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(RemoteGridJob job) {
		synchronized (queue) {
			if (!queue.contains(job)) {
				queue.add(job);
				job.getFuture().setState(GridJobState.ENQUEUED);
				log.info("Job " + job.getJobId() + " is enqueued");
			} else {
				//Job already in Queue
				throw new IllegalArgumentException(
						"A Job with given UUID already in Queue");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RemoteGridJob nextJob(){
		RemoteGridJob nextElement = null;

		try {
			//Get next Job : Block if not available
			nextElement = this.queue.take();
		} catch (InterruptedException e) {
			throw new RuntimeException("Intterupted while waiting for a Job", e);
		}

		log.debug("Returning next Job : " + nextElement.getJobId());	
		return nextElement;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(RemoteGridJob job) {
		synchronized (queue) {
			return queue.remove(job);
		}
	}



}
