package org.nebulaframework.core.grid.cluster.manager.services.jobs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.GridJobStateListener;
import org.nebulaframework.core.job.future.GridJobFutureImpl;
import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.core.task.GridTaskResult;

public class GridJobProfile {
	
	private static Log log = LogFactory.getLog(GridJobProfile.class);
	
	private String jobId;
	private UUID owner;
	private GridJob<? extends Serializable> job;
	private ActiveMQQueue taskQueueRef;
	private ActiveMQQueue resultQueueRef;
	private ActiveMQQueue futureQueueRef;
	private GridJobFutureImpl future;

	private Map<Integer, GridTask<? extends Serializable>> taskMap = new HashMap<Integer, GridTask<? extends Serializable>>();
	private Map<Integer, GridTaskResult> resultMap = new HashMap<Integer, GridTaskResult>();
	
	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public UUID getOwner() {
		return owner;
	}

	public void setOwner(UUID owner) {
		this.owner = owner;
	}

	public GridJob<? extends Serializable> getJob() {
		return job;
	}

	public void setJob(GridJob<? extends Serializable> job) {
		this.job = job;
	}

	public GridJobFutureImpl getFuture() {
		return future;
	}

	public void setFuture(GridJobFutureImpl future) {
		this.future = future;
	}

	public Map<Integer, GridTask<? extends Serializable>> getTaskMap() {
		return taskMap;
	}

	public Map<Integer, GridTaskResult> getResultMap() {
		return resultMap;
	}

	public ActiveMQQueue getTaskQueueRef() {
		return taskQueueRef;
	}

	public void setTaskQueueRef(ActiveMQQueue taskQueueRef) {
		this.taskQueueRef = taskQueueRef;
	}

	public ActiveMQQueue getResultQueueRef() {
		return resultQueueRef;
	}

	public void setResultQueueRef(ActiveMQQueue resultQueueRef) {
		this.resultQueueRef = resultQueueRef;
	}

	public ActiveMQQueue getFutureQueueRef() {
		return futureQueueRef;
	}

	public void setFutureQueueRef(ActiveMQQueue futureQueueRef) {
		this.futureQueueRef = futureQueueRef;
	}

	public void initCleanUpHandlers() {
		if (this.future==null) throw new IllegalStateException("Future not Set, cannot initalize clean up handlers");
		this.future.addGridJobStateListener(new JMSResourceCleanUpListener());
	}
	
	private class JMSResourceCleanUpListener implements GridJobStateListener {

		public void stateChanged(GridJobState newState) {
			
			log.debug("[DUMMY] GridJob State Changed " + newState);
			
/*			// If Job Finished / Terminated
			if (newState.equals(GridJobState.COMPLETE)||newState.equals(GridJobState.FAILED)||newState.equals(GridJobState.CANCELED)) {
				try {
					// TODO Remove if not deletable
					if (taskQueueRef!=null) taskQueueRef.delete();
					if (resultQueueRef!=null)resultQueueRef.delete();
					if (futureQueueRef!=null)futureQueueRef.delete();
					log.debug("Resources Cleaned Up for Job " + jobId);
				} catch (JMSException e) {
					log.warn("Unable to clean up JMS Resources for Job " + jobId, e);
				}
			}*/
		}
		
	}
	

}
