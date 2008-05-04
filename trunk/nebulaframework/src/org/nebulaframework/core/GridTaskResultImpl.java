package org.nebulaframework.core;

import java.io.Serializable;
import java.util.UUID;

/**
 * GridTaskResult Default Implementation.
 * @author Yohan Liyanage
 *
 */
public class GridTaskResultImpl implements GridTaskResult {

	private static final long serialVersionUID = 2844189489176795349L;

	private Serializable result;
	private Exception exception;
	private UUID jobId;
	private UUID taskId;
	private String worker;
	private boolean complete;
	
	
	public GridTaskResultImpl(UUID jobId, UUID taskId, String worker) {
		super();
		this.jobId = jobId;
		this.taskId = taskId;
		this.worker = worker;
	}

	
	public void setResult(Serializable result) {
		this.result = result;
		this.complete = true;
	}


	public void setException(Exception exception) {
		this.exception = exception;
		this.complete = false;
	}


	public void setComplete(boolean complete) {
		this.complete = complete;
	}


	@Override
	public Exception getException() {
		return this.exception;
	}

	@Override
	public UUID getJobId() {
		return this.jobId;
	}

	@Override
	public Serializable getResult() throws IllegalStateException {
		return this.result;
	}

	@Override
	public UUID getTaskId() {
		return this.taskId;
	}

	@Override
	public String getWorkerId() {
		return this.worker;
	}

	@Override
	public boolean isComplete() {
		return this.complete;
	}


	@Override
	public String toString() {
		if (this.complete) {
			return this.jobId + " | " + this.taskId + " @ " + this.worker + "Result : " + this.result;
		}
		else {
			return this.jobId + " | " + this.taskId + " @ " + this.worker + " Exception : " + this.exception;
		}
	}
	
	

}
