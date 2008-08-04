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
package org.nebulaframework.benchmark.scimark.grid.tasks;

import org.nebulaframework.benchmark.scimark.grid.ExecutionResult;
import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.grid.GridExecutionException;

/**
 * Abstract Benchmark Task for SciMark benchmark tasks.
 * <p>
 * Provides common functionality for SciMark test
 * tasks.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public abstract class AbstractBenchmarkTask implements GridTask<ExecutionResult> {

	
	/**
	 * This method provides the templated execution of SciMark
	 * test tasks. This method wraps any checked exceptions thrown
	 * by benchmark tasks with in GridExecutionExceptions.
	 * 
	 * @return benchmark results
	 * @throws GridExecutionException if an exception was thrown
	 */
	@Override
	public ExecutionResult execute() throws GridExecutionException {
		try {
			return doExecute();
		} catch (Exception e) {
			throw new GridExecutionException(e);
		}
	}

	/**
	 * Template method. Actual task execution
	 * should occur with in this method.
	 * 
	 * @return result
	 * @throws Exception if thrown
	 */
	public abstract ExecutionResult doExecute() throws Exception;

}
