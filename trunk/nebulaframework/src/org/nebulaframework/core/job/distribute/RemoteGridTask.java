package org.nebulaframework.core.job.distribute;

import java.io.Serializable;
import java.util.UUID;

import org.nebulaframework.core.GridTask;

public class RemoteGridTask implements Serializable{
	
	
	private static final long serialVersionUID = -7652685199169554306L;
	
	private GridTask<? extends Serializable> task;
	private UUID taskId;
	private UUID jobId;
	
	
	public RemoteGridTask(UUID jobId, GridTask<? extends Serializable> task,
			UUID taskId) {
		super();
		this.jobId = jobId;
		this.task = task;
		this.taskId = taskId;
	}

	public GridTask<? extends Serializable> getTask() {
		return task;
	}
	
	public UUID getTaskId() {
		return taskId;
	}
	
	public UUID getJobId() {
		return jobId;
	}

	@Override
	public String toString() {
		return "Job [" + this.jobId + "] Task [" + this.taskId + "] : " + this.task.toString();
	}
	
	
	
	
}
