package org.nebulaframework.core.job;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.GridJob;
import org.nebulaframework.core.GridJobFuture;
import org.nebulaframework.core.GridJobFutureImpl;

/**
 * JobManager manages Jobs deployed from a particular JVM. There exists only one
 * JobManager per JVM (Singleton).
 * 
 * @author Yohan Liyanage
 * 
 */
public class JobManager {

	private static Log log = LogFactory.getLog(JobManager.class);
	private static JobManager instance = new JobManager();
	private JobQueue queue = JobQueueImpl.getInstance();
	private Map<UUID, RemoteGridJob> jobs = new HashMap<UUID, RemoteGridJob>();
	private Object mutex = new Object();
	/**
	 * No external instantiation of JobManager. Use
	 * {@link JobManager#getInstance()} to obtain the instance.
	 */
	private JobManager() {
		super();
	}

	/**
	 * Returns a reference to the JobManager instance for the current JVM.
	 * 
	 * @return JobManager instance
	 */
	public static JobManager getInstance() {
		return instance;
	}

	/**
	 * Starts execution of the {@link GridJob}. The priority will be set to Normal in this overloaded version of {@link JobManager#start(GridJob, JobPriority)}
	 * @param job GridJob to start
	 * @return A {@link GridJobFuture} indicating status of Job
	 */
	public <R extends Serializable> GridJobFuture start(GridJob<R> job) {
		return start(job, JobPriority.NORMAL);
	}
	
	/**
	 * Starts execution of the {@link GridJob} with a given priority. 
	 *  
	 *  @param job GridJob to Start
	 *  @param priority Priority for Grid Job
	 * @return A {@link GridJobFuture} indicating status of Job
	 */
	
	public <R extends Serializable> GridJobFuture start(GridJob<R> job, JobPriority priority) {
		
		log.info("JobManager starting a new Job...");
		synchronized (mutex) {

			//Create UUID
			UUID jobId = UUID.randomUUID();
			
			//Create Future
			GridJobFutureImpl future = new GridJobFutureImpl(jobId);
			
			//Create RemoteGridJob wrapper
			RemoteGridJob remoteJob = new RemoteGridJob(jobId, job, priority,
					future);
			log.debug("Assigened UUID " + jobId);
			//Enqueue
			this.queue.add(remoteJob);
			this.jobs.put(jobId, remoteJob);
			
			//Return Future
			return future;
		}
	}
	
	/**
	 * Cancel the given Job in the Grid.
	 * 
	 * @param jobid
	 * @return
	 */
	public boolean cancel(UUID jobid) {
		// TODO Implement
		return false;
	}

	
}
