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

public class JobExecutor {

	private static Log log = LogFactory.getLog(JobExecutor.class);

	private static TaskQueue taskQueue;
	private static ResultQueue<? extends Serializable> resultQueue;
	private static RemoteGridJob job;
	private static List<JobListener> jobListeners = new ArrayList<JobListener>();
	private static boolean executing = false;

	static {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {

					try {
						synchronized (JobExecutor.class) {
							if (executing) {
								continue;
							}
						}

						JobQueue queue = JobQueueImpl.getInstance();
						log.info("Requesting Next Job from JobQueue");
						JobExecutor.setJob(queue.nextJob());
						JobExecutor.execute();
					} finally {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}

		}).start();
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

	public static synchronized void setJob(RemoteGridJob job) {
		JobExecutor.job = job;
	}

	public static synchronized <T extends Serializable> void execute() {

		if (job == null) {
			log.debug("No Job to Execute [null]");
			return;
		}
		
		log.info("Starting Execution of Job " + job.getJobId());
		executing = true;
		log.debug("Updating Future...");
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

		log.debug("Notifying Job Start...");
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

	@SuppressWarnings("unchecked")
	private synchronized static void aggreagateJob(
			GridJob<? extends Serializable> job, ResultQueue resultQueue) {
		
		log.debug("Aggreagating Results for Job " + JobExecutor.getJob().getJobId());
		try {
			Serializable result = job.aggregate(resultQueue.getResults());
			JobExecutor.job.getFuture().setResult(result);
			JobExecutor.job.getFuture().setState(GridJobState.COMPLETE);
		} catch (Exception e) {
			JobExecutor.job.getFuture().setState(GridJobState.FAILED);
		}

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
		if (job == null) {
			return;
		}


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
		if (job == null) {
			return;
		}

		

		synchronized (jobListeners) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					for (JobListener l : jobListeners) {
						l.endJob(job.getJobId());
					}
					
					// Update Flag
					executing = false;
				}

			}).start();
		}
		
		
	}

}
