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

import java.io.Externalizable;
import java.io.Serializable;
import java.util.UUID;

import org.springframework.util.Assert;

/**
 * Implementation of {@code GridTaskResult}, wrapper class for a result of
 * {@link GridTask} execution, including related meta-data.
 * <p>
 * This class implements {@link Externalizable} interface, instead of
 * {@link Serializable} to improve performance in communications, by reducing
 * the data transfer amount and serialization time [Grosso, W. 2001. "Java RMI",
 * Section 10.7.1].
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see GridTaskResult
 * @see Externalizable
 */
public class GridTaskResultImpl implements GridTaskResult {

	private static final long serialVersionUID = 4411158166154492107L;
	
	private Serializable result; // Result of execution
	private Exception exception; // Exceptions, if any
	private String jobId; // Parent Job Id
	private int taskId; // TaskId of task
	private UUID workerId; // Node ID of Worker
	private boolean complete; // Status flag, true if result available

	/**
	 * Constructs a {@link GridTaskResultImpl} instance for the given JobId,
	 * TaskId and WorkerId.
	 * 
	 * @param jobId
	 *            Parent {@code GridJob} Id
	 * @param taskId
	 *            Task Id of the {@code GridTask}
	 * @param workerId
	 *            Node Id of worker {@code GridNode}
	 */
	public GridTaskResultImpl(String jobId, int taskId, UUID workerId)
			throws IllegalArgumentException {
		
		super();
		
		//Check for null
		Assert.notNull(jobId);
		Assert.notNull(taskId);
		Assert.notNull(workerId);
		
		this.jobId = jobId;
		this.taskId = taskId;
		this.workerId = workerId;
	}

	/**
	 * Sets the result of execution of the {@code GridTask}.
	 * 
	 * @param result {@code Serializable} result of execution
	 */
	public void setResult(Serializable result) {
		this.result = result;
		this.complete = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Serializable getResult() throws IllegalStateException {
		if (!complete) {
			throw new IllegalStateException(
					"No result available, as Job is not complete", exception);
		}
		return this.result;
	}
	
	/**
	 * Sets the exception of the {@code GridTask} execution,
	 * if it has failed, and if applicable.
	 * 
	 * @param result {@code Exception} exception
	 */	
	public void setException(Exception exception) {
		this.exception = exception;
		this.complete = false;
	}

	/**
	 * {@inheritDoc}
	 */	
	public Exception getException() throws IllegalStateException {
		if (complete) {
			throw new IllegalStateException(
					"No exception available, as Job is complete");
		}		
		return this.exception;
	}

	public String getJobId() {
		return this.jobId;
	}

	/**
	 * {@inheritDoc}
	 */	
	public int getTaskId() {
		return this.taskId;
	}

	/**
	 * {@inheritDoc}
	 */	
	public UUID getWorkerId() {
		return this.workerId;
	}

	/**
	 * {@inheritDoc}
	 */	
	public boolean isComplete() {
		return this.complete;
	}

	/**
	 * Returns a String representation of this {@code GridTaskResultImpl}. The
	 * formats of String representation are
	 * <ul>
	 * <li><code><i>JobId</i>|<i>TaskId</i>@<i>WorkerId</i>&gt;
	 * Result:<i>result</i></code></li>
	 * <li><code><i>JobId</i>|<i>TaskId</i>@<i>WorkerId</i>&gt;
	 * Exception:<i>exception</i></code></li>
	 * </ul>
	 */
	@Override
	public String toString() {
		if (this.complete) {
			return this.jobId + "|" + this.taskId + "@" + this.workerId
					+ "> Result:" + this.result;
		} else {
			return this.jobId + "|" + this.taskId + "@" + this.workerId
					+ "> Exception:" + this.exception;
		}
	}

}
