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

import org.nebulaframework.core.GridExecutionException;
import org.nebulaframework.core.job.GridJob;

/**
 * <p>This interface defines the contract for a task which can be executed on a remote node of the grid.
 * The remote code should be given under {@link GridTask#execute(Serializable...)} method.</p>
 * <p>{@link GridJob}s will be split into <tt>GridTask</tt>s at the time of <tt>split</tt> operation.</p>
 * 
 * @author Yohan Liyanage
 *
 * @param <T> Type of the result of execution
 */
public interface GridTask<T extends Serializable> extends Serializable{
	
	/**
	 * Executes the GridTask on a remote node.
	 * 
	 * @return Result of execution.
	 * @throws GridExecutionException If an exception was thrown at the time of execution.
	 */
	public T execute() throws GridExecutionException;
	
	/**
	 * Cancels the execution of this Task.
	 * @return <tt>true</tt> if task was successfully canceled.
	 */
	public boolean cancel();
}
