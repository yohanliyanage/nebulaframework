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
package org.nebulaframework.core.task;

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
	private String jobId;
	private int taskId;
	private UUID workerId;
	private boolean complete;
	
	
	public GridTaskResultImpl(String jobId, int taskId, UUID workerId) {
		super();
		this.jobId = jobId;
		this.taskId = taskId;
		this.workerId = workerId;
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


	public Exception getException() {
		return this.exception;
	}

	public String getJobId() {
		return this.jobId;
	}

	public Serializable getResult() throws IllegalStateException {
		return this.result;
	}

	public int getTaskId() {
		return this.taskId;
	}

	public UUID getWorkerId() {
		return this.workerId;
	}

	public boolean isComplete() {
		return this.complete;
	}


	@Override
	public String toString() {
		if (this.complete) {
			return this.jobId + " | " + this.taskId + " @ " + this.workerId + "Result : " + this.result;
		}
		else {
			return this.jobId + " | " + this.taskId + " @ " + this.workerId + " Exception : " + this.exception;
		}
	}
	
	

}
