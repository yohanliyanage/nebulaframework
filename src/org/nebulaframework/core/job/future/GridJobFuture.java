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
package org.nebulaframework.core.job.future;

import java.io.Serializable;
import java.util.concurrent.Future;

import org.nebulaframework.core.GridExecutionException;
import org.nebulaframework.core.GridTimeoutException;
import org.nebulaframework.core.job.GridJobState;

/**
 * Defines the interface of {@ code GridJobFuture} implementations, which represents the 
 * result of a deployed {@code GridJob}.
 * <p>
 * {@code GridJobFuture} is returned after submitting the {@code GridJob} to the Grid. 
 * This class allows to check the status of a {@code GridJob}, to cancel 
 * execution of a {@code GridJob}, and to obtain the result of a {@code GridJob}, 
 * blocking until result is available. A {@code GridJob} can be requested to be 
 * canceled using the {@link #cancel()} method.  
 * <p>
 * The implementation of this interface resides at the {@code ClusterManager}'s JVM,
 * and is exposed as a remote service to the submitter node, using proxy classes.
 * <p>
 * <i>Note :</i> This class was modeled after the {@link Future} class of 
 * {@code java.util.concurrent} package.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see Future
 * @see SplitAggregateGridJob
 */
public interface GridJobFuture {
	
	/**
	 * Returns the Job State through {@link GridJobState} enumeration.
	 * @return JobState of the Grid Job
	 */
	public GridJobState getState();
	
	/**
	 * Returns the result of Job. This method blocks until the job execution is complete.
	 * @return Result of the job
	 * @throws GridExecutionException if execution fails
	 */
	public Serializable getResult() throws GridExecutionException;
	
	/**
	 * Returns the result of Job with in a given time. 
	 * This method blocks until the job execution is complete or 
	 * the timeout exceeds (which ever occurs first).
	 * 
	 * @param timeout Timeout in milliseconds
	 * @return Result of the job
	 * @throws GridExecutionException if execution fails
	 * @throws GridTimeoutException if timeout occurs
	 */
	public Serializable getResult(long timeout) throws GridExecutionException, GridTimeoutException;

	/**
	 * Returns the exception attached to the execution of this {@code GridJob},
	 * if applicable, or {@code null}.
	 * 
	 * @return Assigned {@code Exception), or {@code null}
	 */
	public Exception getException();
	
	/**
	 * Attempts to cancel execution of this job. Note that the cancellation 
	 * of the job is <b>not a guaranteed behavior</b>.
	 * 
	 * @return if cancellation succeeded , {@code true}.
	 */
	public boolean cancel();
	
}
