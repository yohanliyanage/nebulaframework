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
package org.nebulaframework.core.job;

import java.io.Serializable;

import org.nebulaframework.core.GridExecutionException;
import org.nebulaframework.core.GridTimeoutException;
import org.nebulaframework.core.task.GridTask;

/**
 * <p>TaskFuture is returned by {@link GridJob#start()} method. This class allows to 
 * check the status of a {@link GridJob}, to cancel execution of a {@link GridJob}, 
 * and to obtain the result of a {@link GridJob}. </p> 
 * 
 * @author Yohan Liyanage
 *
 * @param <T> Type of result of execution of GridTask.
 */
public interface GridJobFuture extends Serializable{
	
	/**
	 * Returns the Job State through {@link GridJobState} enum.
	 * @return JobState of the Grid Job
	 */
	public GridJobState getState();
	
	/**
	 * Attempts to cancel execution of this job. The cancellation of the job is not a guaranteed behavior.
	 * This method attempts to cancel each GridTask by calling {@link GridTask#cancel()} method.
	 * 
	 * @return true if cancellation succeeded.
	 */
	public boolean cancel();
	
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
	 * Returns the current {@link GridJobStateListener} assigned to the GridJobFuture.
	 * @return {@link GridJobStateListener}
	 */
	public GridJobStateListener getListener();

	/**
	 * Sets the {@link GridJobStateListener} for this GridJobFuture.
	 * @param listener {@link GridJobStateListener}
	 */
	public void setListener(GridJobStateListener listener);
}
