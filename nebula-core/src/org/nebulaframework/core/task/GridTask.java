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

import org.nebulaframework.core.job.SplitAggregateGridJob;
import org.nebulaframework.core.job.unbounded.UnboundedGridJob;
import org.nebulaframework.grid.GridExecutionException;

/**
 * This interface defines the contract for a task which can be executed on a remote node of the grid.
 * The remote code should be given under {@link GridTask#execute()} method.
 * <p>
 * {@link SplitAggregateGridJob}s will be split into {@code GridTask}s at the time of {@code split} operation.
 * <p>
 * {@link UnboundedGridJob}s will generate {@code GridTask} at each invocation of {@link UnboundedGridJob#task()}
 * method.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @param <T> Type of the result of {@code GridTask} execution
 */
public interface GridTask<T extends Serializable> extends Serializable{
	
	/**
	 * Executes the GridTask on a remote node. Logic of each Task should be
	 * specified in the implementation of this method.
	 * 
	 * @return Result of execution.
	 * 
	 * @throws GridExecutionException If an exception is to be thrown at the 
	 * time of execution, it should be wrapped by a {@link GridExecutionException}.
	 */
	public T execute() throws GridExecutionException;
}
