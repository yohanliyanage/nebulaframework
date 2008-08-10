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
package org.nebulaframework.grid.cluster.manager.services.jobs.tracking;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.splitaggregate.SplitAggregateGridJob;
import org.nebulaframework.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.grid.cluster.manager.services.jobs.JobExecutionManager;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;

/**
 * TaskTracker which tracks the status of tasks for
 * a given GridJob.
 * <p>
 * Instance of this class monitors the task execution of
 * a GridJob, and re-enqueues potentially failed tasks.
 * The average execution duration for a task is constantly measured
 * by the 
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class GridJobTaskTracker {

	private static final Log log = LogFactory.getLog(GridJobTaskTracker.class);
	
	private GridJobProfile profile;
	private JobExecutionManager executionManager;

	private ExecutorService executors = Executors.newCachedThreadPool();
	
	private boolean started;
	private boolean stopped;
	
	// Average Execution Time, in Seconds
	private int averageTaskDuration = -1;
	
	// Enqueued Tasks
	private Queue<Integer> enqueued = new LinkedList<Integer>();
	
	// Tasks which are potentially failed 
	// (results received for tasks enqueued after)
	private Queue<Integer> potential = new LinkedList<Integer>();
	
	// Tasks Marked as Failed and to be re-enqueued
	private Queue<Integer> marked = new LinkedList<Integer>();
	
	// Active Worker Count (as of last update)
	private int workerCount = 0;
	
	private int renqueued = 0; // TODO Remove
	
	/**
	 * Constructs a Task Tracker for given job.
	 * <p>
	 * Note that this is a <b>private</b> constructor.
	 * 
	 * @param profile Job Profile
	 * @param executionManager JobExecutionManager for job
	 */
	private GridJobTaskTracker(GridJobProfile profile,
			JobExecutionManager executionManager) {
		super();
		this.profile = profile;
		this.executionManager = executionManager;
	}
	
	/**
	 * Starts a tracker instance for given Job.
	 * 
	 * @param profile Profile
	 * @param executionManager Job Execution Manager
	 * @return constructed instance
	 */
	public static GridJobTaskTracker startTracker(GridJobProfile profile,
			JobExecutionManager executionManager) {
		
		final GridJobTaskTracker instance = new GridJobTaskTracker(profile, executionManager);
		
		
		// Create Job-End Cleanup Hook
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {

			@Override
			public void onServiceEvent(ServiceMessage message) {
				instance.stop();
			}
			
		}, profile.getJobId(), ServiceMessageType.JOB_CANCEL, ServiceMessageType.JOB_END);
		
		return instance;
	}

	/**
	 * Starts the tracker, which then
	 * continues to monitor and re-enqueue possibly
	 * failed jobs.
	 */
	public void start() {
		
		// Ignore all start() calls if already started
		if (started) return;
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// Initial Startup Delay (wait for tasks to execute)
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					log.error("[GridJobTaskTracker] Interrupted", e);
				}	
				
				// Start Task Tracker
				GridJobTaskTracker.this.startTracking();
			}
		}).start();
		
	}
	
	/**
	 * Internal method to start the tracking process.
	 */
	protected void startTracking() {
		
		log.debug("[GridJobTaskTracker] Started Tracking for " + profile.getJobId());
		
		// Wait until average information is available
		while (averageTaskDuration < 0 || (profile.getFuture().getState()!=GridJobState.EXECUTING)) {
			
			// If Stopped
			if (stopped) {
				log.debug("[GridJobTaskTracker] Stopped Tracking for " + profile.getJobId());
				return;
			}
			
			try {
				// Wait for average to be set
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				log.error("[GridJobTaskTracker] Interrupted", e);
			}
		}
		
		// Retrieve Worker Count
		workerCount = profile.getWorkerCount();
		
		// We have duration information now
		while (!stopped) {
			
			synchronized (this) {
				renqueueMarked();
				potentialToMarked();
				enqueuedToPotential();
			}

			// If this is the final stage of Job Execution (90%)
			if (isFinalStage()) {
				// Duplicate Tasks (Increases throughput)
				log.trace("In Final Stage");
				enqueuedToMarked();
			}
			
			// Update Worker Count
			workerCount = profile.getWorkerCount();
			
			try {
				// Sleep for some duration, determined by average * getMultiple()
				Thread.sleep((long) Math.floor(averageTaskDuration * getMultipler()));
			} catch (InterruptedException e) {
				log.error("[GridJobTaskTracker] Interrupted", e);
			}
		}
		
		log.debug("[GridJobTaskTracker] Stopping for Job " + profile.getJobId());
		
	}

	private boolean isFinalStage() {
		return (this.profile.getWorkerCount() >= this.profile.getTaskCount());
	}

	/**
	 * Returns the sleep duration multiplier. The tracker
	 * thread sleeps for {@code average duration * multiplier} amount of 
	 * time (milliseconds). This multiplier is higher for tasks with lower
	 * duration, and lower for tasks with higher duration.
	 * 
	 * @return multiplier
	 */
	private long getMultipler() {
		
		// 2 Times Delay for Durations Less Than 2 Seconds
		if (averageTaskDuration < 2) {
			return 2000;
		}
		// 1.5 Times Delay for Durations 2-4 Secs
		else if (averageTaskDuration < 4) {
			return 1500;
		}
		// 1.25 Time Delay for Durations > 5 Seconds
		else if (averageTaskDuration < 10) {
			return 1250;
		}
		else {
			return 1100;
		}
		
	}

	/**
	 * Re-enqueues jobs which are marked as 
	 * possibly failed.
	 */
	private synchronized void renqueueMarked() {
		
		if (marked.size() == 0) return;
		
		Integer[] tasks = marked.toArray(new Integer[marked.size()]);
		
		log.trace("Reenqueuing " + tasks.length + "tasks");
		
		marked.clear();
		
		for (int task : tasks) {
			executionManager.reEnqueueTask(profile.getJobId(), task);
			log.trace("Re-enqueued " + task);
			log.trace("Re-enqueued " + (++renqueued) + " tasks so far");
		}
		
	}

	/**
	 * Moves the potentially failed tasks to
	 * marked task queue.
	 */
	private synchronized void potentialToMarked() {
		
		if (profile.getJob() instanceof SplitAggregateGridJob) {
			
			// For Split-Aggregate style, all tasks are enqueued in beginning.
			// But we should only consider about the tasks which will be
			// executed by workers.
			int activeRange = workerCount;
			activeRange = (potential.size() > activeRange) ? activeRange : potential.size();
			
			for (int i=0; i <activeRange; i++) {
				marked.add(potential.remove());
			}
			
			log.trace("Potential to Marked " + activeRange + "tasks");
			
		}
		else {
			for (int i=0; i <potential.size(); i++) {
				marked.add(potential.remove());
			}
			
			log.trace("Potential to Marked " + potential.size() + "tasks");
		}
		
		
		
	}
	
	/**
	 * Moves the existing enqueued tasks queue to
	 * marked queue.
	 */
	private synchronized void enqueuedToPotential() {
		
		if (profile.getJob() instanceof SplitAggregateGridJob) {
			
			// For Split-Aggregate style, all tasks are enqueued in beginning.
			// But we should only consider about the tasks which will be
			// executed by workers.
			int activeRange = workerCount;
			activeRange = (enqueued.size()-1 > activeRange) ? activeRange : enqueued.size() -1;
			
			for (int i=0; i <activeRange; i++) {
				potential.add(enqueued.remove());
			}
			log.trace("Enqueued to Potential " + activeRange + "tasks");
		}
		else {
			for (int i=0; i <enqueued.size(); i++) {
				potential.add(enqueued.remove());
				log.trace("Enqueued to Potential " + enqueued.size() + "tasks");
			}
		}
	}
	

	/**
	 * Moves the existing enqueued tasks queue to
	 * marked queue.
	 */
	private synchronized void enqueuedToMarked() {
		
		log.trace("Enqueued to Marked " + enqueued.size() + "tasks");
		
		for (int i=0; i <enqueued.size(); i++) {
			marked.add(enqueued.remove());
		}
	}

	/**
	 * Invoked to notify that a new task was enqueued.
	 * 
	 * @param taskId TaskId of enqueued task
	 */
	public synchronized void taskEnqueued(int taskId) {
		
		if (stopped) return;
		
		log.trace("Enqueued " + taskId);
		enqueued.add(taskId);
	}
	
	/**
	 * Invoked to notify that a result was received
	 * 
	 * @param taskId taskId of result
	 * @param executionTime duration taken to execute
	 */
	public void resultReceived(final Integer taskId, final long executionTime) {
		
		if (stopped) return;
		
		executors.execute(new Runnable() {

			@Override
			public void run() {
				
				
				synchronized (GridJobTaskTracker.this) {
				
					// Remove from any list (enqueued, marked)
					if (enqueued.contains(taskId)) {
						
						// This task is not the first item in queue
						// Remove all tasks ahead in queue
						// and put to potentially failed
						// list
//						while (! enqueued.peek().equals(taskId)) {
//							potential.add(enqueued.remove());
//						}
						
						// TODO Remove above block
						
						enqueued.remove(taskId);
					}
					if (potential.contains(taskId)) {
						potential.remove(taskId);
					}
					if (marked.contains(taskId)) {
						marked.remove(taskId);
					}
					
					// Calculate Task Average Duration
					double seconds = Math.floor(((double)(executionTime/1000)));
					
					if (seconds==0) seconds = 1;

					if (averageTaskDuration <=0) {
						averageTaskDuration = (int) seconds;
					}
					else {
						averageTaskDuration = (int) Math.floor(((averageTaskDuration + seconds) / 2));
					}
				}
			}
		});
	}

	/**
	 * Stops the TaskTracker
	 */
	public synchronized void stop() {
		this.stopped = true;
		destory();
	}
	
	/**
	 * Destroys the tracker instance.
	 */
	private void destory() {
		this.enqueued = null;
		this.marked = null;
	}
}
