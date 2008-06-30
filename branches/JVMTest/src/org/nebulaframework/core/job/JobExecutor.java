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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.GridJob;
import org.nebulaframework.core.GridTask;
import org.nebulaframework.core.GridTaskResult;
import org.nebulaframework.core.job.distribute.JobTaskTracker;
import org.nebulaframework.core.job.distribute.RemoteGridTask;
import org.nebulaframework.core.job.distribute.ResultQueue;
import org.nebulaframework.core.job.distribute.TaskQueue;
import org.nebulaframework.core.job.distribute.TaskResultListener;

/**
 * Job Executor manages Job Execution.
 * @author Yohan Liyanage
 *
 */
public class JobExecutor {

	private static Log log = LogFactory.getLog(JobExecutor.class);

	private static TaskQueue taskQueue;
	private static ResultQueue<? extends Serializable> resultQueue;
	private static RemoteGridJob job;
	private static List<JobListener> jobListeners = new ArrayList<JobListener>();
	private static boolean executing = false;
	private static Object mutex = new Object();
	
	/**
	 * Static Initialization Block
	 * creates and starts JobExecutorDaemon Thread.
	 */
	static {
		
		/**
		 * JobExecutorDaemon Thread takes a Job from 
		 */
		Thread jobExecutor = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					//Infinite Loop : Until Interrupted
					while (true) {
						
							//If a Job is in execution
							while (JobExecutor.isExecuting()) {
								synchronized (mutex) {
									// Wait till current Job finishes
									mutex.wait();
								}
							}
							
							// Check for Next Job
							log.info("Requesting Next Job from JobQueue...");
							JobExecutor.setJob(JobQueueImpl.getInstance().nextJob());
							JobExecutor.execute();
					}
				}
				catch (InterruptedException e) {
					log.warn("Interrupted Exception at JobExecutorDaemon Thread", e);
					log.debug("Stopping JobExecutorDaemon Thread");
				}
			}

		}, "JobExecutorDaemon");
		
		jobExecutor.setDaemon(true);	//Daemon Thread : Runs continuously
		jobExecutor.start();
	}

	/**
	 * No instantiation
	 */
	private JobExecutor() {
		super();
	}

	public static synchronized RemoteGridJob getJob() {
		return job;
	}

	public static synchronized void setJob(RemoteGridJob job) throws IllegalArgumentException {
		if (job==null) throw new IllegalArgumentException("No Job Specified");
		JobExecutor.job = job;
	}

	public static synchronized <T extends Serializable> void execute() throws IllegalStateException {

		if (job == null) {
			throw new IllegalStateException("JobExecutor.execute() invoked with a 'null' Job");
		}

		log.info("Starting Execution of Job " + job.getJobId());
		executing = true;
		job.getFuture().setState(GridJobState.EXECUTING);
		
		log.debug("Creating Task Tracker...");
		
		// Create Task Q / result Q / tracker
		final JobTaskTracker tracker = new JobTaskTracker(job.getJobId());
		taskQueue = new TaskQueue(job.getJobId(), tracker);
		resultQueue = new ResultQueue<T>(job.getJobId(), tracker);

		log.debug("Assigning Handler for TaskResultListener");
		
		// Assign Handler to dispatch Job End Notification
		resultQueue.addTaskResultListener(new TaskResultListener() {

			@Override
			public void resultCollected(GridTaskResult result) {
				if (tracker.isJobComplete()) {
					aggreagateJob(job.getJob(), resultQueue);
					notifyJobEnd();
					log.info("Finished Execution of Job " + job.getJobId());
				}
			}

		});

		
		// Notify Workers that a new Job is available
		notifyJobStart();

		// Send the Tasks to Job's Task Queue
		new Thread(new Runnable() {

			@Override
			public void run() {
				splitJob(job.getJob(), taskQueue);
			}

		}).start();
	}
	
	private synchronized static void splitJob(
			GridJob<? extends Serializable> job, TaskQueue taskQueue) {

		log.debug("Splitting Tasks for Job " + JobExecutor.getJob().getJobId());
		List<? extends GridTask<? extends Serializable>> tasks = job.split();

		for (GridTask<? extends Serializable> task : tasks) {
			RemoteGridTask remoteTask = new RemoteGridTask(JobExecutor.job
					.getJobId(), task, UUID.randomUUID());
			taskQueue.addTask(remoteTask);
		}

	}
	
	@SuppressWarnings("unchecked")
	private static void aggreagateJob(
			GridJob<? extends Serializable> job, ResultQueue resultQueue) {
		
		log.debug("Aggreagating Results for Job " + JobExecutor.getJob().getJobId());
		
		synchronized (JobExecutor.class) {
			try {
				//Aggregate
				Serializable result = job.aggregate(resultQueue.getResults());
				
				//Update Future
				JobExecutor.job.getFuture().setResult(result);
				JobExecutor.job.getFuture().setState(GridJobState.COMPLETE);
				
				//Update Flag
				setExecuting(false);
			} catch (Exception e) {
				JobExecutor.job.getFuture().setState(GridJobState.FAILED);
			}
		}
		
		log.debug("Aggregation Finished");
		
		synchronized (mutex) {
			//Notify to JobExecutorDaemon about Job Finished Event
			mutex.notifyAll();
		}
	}



	public static void addJobListener(JobListener listener) {
		synchronized (jobListeners) {
			jobListeners.add(listener);
		}
		synchronized (JobExecutor.class) {
			if (JobExecutor.executing) {
				listener.startJob(job.getJobId(), job.getPriority(),
						taskQueue, resultQueue);
			}
		}
	}

	public static void removeJobListener(JobListener listener) {
		synchronized (jobListeners) {
			jobListeners.remove(listener);
		}
	}

	private synchronized static void notifyJobStart() {
		
		log.debug("Notifying Job Start...");
		
		synchronized (jobListeners) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					for (JobListener l : jobListeners) {
						l.startJob(job.getJobId(), job.getPriority(),
								taskQueue, resultQueue);
					}
				}

			}).start();
		}
	}

	private synchronized static void notifyJobEnd() {
		synchronized (jobListeners) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					for (JobListener l : jobListeners) {
						l.endJob(job.getJobId());
					}
					
					
				}

			}).start();
		}
	}

	private synchronized static boolean isExecuting() {
		return executing;
	}
	
	private synchronized static void setExecuting(boolean executing) {
		JobExecutor.executing = executing;
		
	}
}
