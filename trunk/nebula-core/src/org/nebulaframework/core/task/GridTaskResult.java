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
 * Wrapper class interface for a result of {@link GridTask} 
 * execution, including related meta-data.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see GridTask
 */
public interface GridTaskResult extends Serializable {
	
	/**
	 * Returns the JobId of {@code GridJob}, to which this 
	 * {@link GridTaskResult} belongs.
	 * 
	 * @return String JobId
	 */
	public String getJobId();
	
	/**
	 * Returns the Task Id (Sequence Number) of the 
	 * {@code GridTask} which produced the result.
	 * 
	 * @return taskId
	 */
	public int getTaskId();
	
	/**
	 * Returns NodeId of the {@code GridNode} which
	 * executed the {@code GridTask}.
	 * 
	 * @return WorkerId (NodeID of  Worker)
	 */
	public UUID getWorkerId();
	
	/**
	 * If the {@code GridTask} has failed, returns the exception 
	 * thrown at execution, if available. Otherwise, returns
	 * {@code null}.
	 * 
	 * @return Exception thrown
	 * 
	 * @throws IllegalStateException if execution has succeeded
	 */
	public Exception getException() throws IllegalStateException;
	
	/**
	 * Returns the result of execution, if completed
	 * 
	 * @return Result of execution
	 *  
	 * @throws IllegalStateException if execution was failed
	 */
	public Serializable getResult() throws IllegalStateException;
	
	/**
	 * Returns {@code true} if the {@code GridTask} has completed successfully 
	 * without exceptions.
	 * 
	 * @return Value {@code true} if {@code GridTask} completed.
	 */
	public boolean isComplete();
	
	/**
	 * Returns the time taken to execute this GridTask,
	 * in milliseconds.
	 * 
	 * @return Execution time, as a {@code long}
	 */
	public long getExecutionTime();
	
}
