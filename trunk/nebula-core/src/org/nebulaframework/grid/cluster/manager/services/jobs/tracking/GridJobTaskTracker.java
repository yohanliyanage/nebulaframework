package org.nebulaframework.grid.cluster.manager.services.jobs.tracking;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.grid.cluster.manager.services.jobs.JobExecutionManager;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;

// TODO FixDoc
public class GridJobTaskTracker {

	private static final Log log = LogFactory.getLog(GridJobTaskTracker.class);
	
	private GridJobProfile profile;
	private JobExecutionManager executionManager;

	private ExecutorService executors = Executors.newCachedThreadPool();
	
	private boolean started;
	private boolean stopped;
	
	// Average Execution Time, in Seconds
	private long averageTaskDuration = -1;
	
	// Enqueued Tasks
	private Queue<Integer> enqueued = new LinkedList<Integer>();
	
	// Tasks which are potentially failed 
	// (results received for tasks enqueued after)
	private List<Integer> potential = new ArrayList<Integer>();
	
	// Tasks Marked as Failed and to be re-enqueued
	private List<Integer> marked = new ArrayList<Integer>();
	
	
	private GridJobTaskTracker(GridJobProfile profile,
			JobExecutionManager executionManager) {
		super();
		this.profile = profile;
		this.executionManager = executionManager;
	}
	
	
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
	
	protected void startTracking() {
		
		log.debug("[GridJobTaskTracker] Started Tracking for " + profile.getJobId());
		
		// Wait until average information is available
		while (averageTaskDuration < 0 && (profile.getFuture().getState()!=GridJobState.EXECUTING)) {
			
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
		
		// We have duration information now
		while (!stopped) {
			
			synchronized (this) {
				renqueueMarked();
				potentialToMarked();
			}

			// If this is the final stage of Job Execution (90%)
			if (this.profile.percentage() > 0.9) {
				// Duplicate Tasks (Increases throughput)
				enqueuedToPotential();
			}
			
			
			try {
				// Sleep for some duration, determined by average * getMultiple()
				Thread.sleep((long) Math.floor(averageTaskDuration * getMultipler()));
			} catch (InterruptedException e) {
				log.error("[GridJobTaskTracker] Interrupted", e);
			}
		}
		
		log.debug("[GridJobTaskTracker] Stopping for Job " + profile.getJobId());
		
	}



	private long getMultipler() {
		
		// 2 Times Delay for Durations Less Than 2 Seconds
		if (averageTaskDuration < 2) {
			return 2000;
		}
		// 1.25 Times Delay for Durations 2-4 Secs
		else if (averageTaskDuration < 4) {
			return 1250;
		}
		// 1 Time Delay for Durations > 5 Seconds
		else {
			return 1000;
		}
		
	}


	private synchronized void renqueueMarked() {
		
		if (marked.size() == 0) return;
		
		Integer[] tasks = marked.toArray(new Integer[marked.size()]);
		marked.clear();
		
		for (int task : tasks) {
			executionManager.reEnqueueTask(profile.getJobId(), task);
		}
		
	}


	private synchronized void potentialToMarked() {
		for (int i=0; i <potential.size(); i++) {
			marked.add(potential.remove(i));
		}
	}
	

	private synchronized void enqueuedToPotential() {
		for (int i=0; i <enqueued.size(); i++) {
			marked.add(enqueued.remove());
		}
	}


	public synchronized void taskEnqueued(int taskId) {
		
		if (stopped) return;
		
		enqueued.add(taskId);
	}
	
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
						while (! enqueued.peek().equals(taskId)) {
							potential.add(enqueued.remove());
						}
						
						enqueued.remove(taskId);
					}
					else if (potential.contains(taskId)) {
						potential.remove(taskId);
					}
					else if (marked.contains(taskId)) {
						marked.remove(taskId);
					}
					
					// Calculate Duration
					double seconds = Math.floor(((double)(executionTime/1000)));
					
					if (seconds==0) seconds = 1;

					if (averageTaskDuration <=0) {
						averageTaskDuration = (long) seconds;
					}
					else {
						averageTaskDuration = (long) Math.floor(((averageTaskDuration + seconds) / 2));
					}
				}
			}
		});
	}

	
	public synchronized void stop() {
		this.stopped = true;
		destory();
	}
	
	private void destory() {
		this.enqueued = null;
		this.marked = null;
	}
}
