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
import java.util.List;

import org.nebulaframework.core.task.GridTask;

/**
 * Represents a Job which is to be executed on Grid. 
 * A Job will be split into multiple {@link GridTask}s and 
 * executed on remote nodes.
 * 
 * @author Yohan Liyanage
 *
 * @param <R> Type of Result of execution of the Job
 */
public interface GridJob<R extends Serializable> extends Serializable{
	
	/**
	 * Logic to split the Job into multiple tasks.
	 * @return A Collection of {@link GridTask}s.
	 */
	public List<? extends GridTask<R>> split();
	
	/**
	 * Logic to aggregate results from tasks into final result of job.
	 * @param results A Collection of {@link Serializable}s 
	 * which are the outcome of {@link GridTask} execution.
	 * @return Final result of Job
	 */
	public R aggregate(List<Serializable> results);
	


}