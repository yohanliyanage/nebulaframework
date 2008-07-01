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

/**
 * Wraps a result of {@link GridTask} execution with meta data.
 * @author Yohan Liyanage
 *
 * @param <T> Type of Result of execution
 */
public interface GridTaskResult extends Serializable {
	
	/**
	 * Returns the Job Id
	 * @return String JobId
	 */
	public String getJobId();
	
	/**
	 * Returns the Task Id
	 * @return int TaskId
	 */
	public int getTaskId();
	
	/**
	 * Returns Worker Id
	 * @return Worker Id
	 */
	public String getWorkerId();
	
	/**
	 * Returns the exception thrown at execution, if any
	 * @return Exception thrown, or <tt>null</tt>
	 */
	public Exception getException();
	
	/**
	 * Returns the result of execution, if completed
	 * @return Result of execution, or IllegalStateException if execution was failed
	 */
	public Serializable getResult() throws IllegalStateException;
	
	/**
	 * Returns true if the Task has completed successfully without exceptions.
	 * @return true if Task completed
	 */
	public boolean isComplete();
	
}
