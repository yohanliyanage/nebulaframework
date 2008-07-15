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
 * Represents a Split-Aggregate model Job which is can be 
 * executed on Nebula Grid. 
 * <p>
 * A Job will be split into multiple {@link GridTask}s and 
 * executed on remote nodes, using the {@link #split()} method.
 * <p>
 * All intermediate results returned after the executions of
 * {@code GridTask}s will be then aggregated using the
 * {@link #aggregate(List)} method, and final result will be
 * returned.
 * <p>
 * If it is necessary to obtain the intermediate results of
 * each task execution, provide a {@code ResultCallback} when the job
 * is submitted. Refer to {@link ResultCallback} for more information.
 * 
 * @param <T> Type of intermediate results of {@code GridTask}s
 * @param <R> Type of Final Result of the {@code GridJob}
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see GridTask
 */
public interface SplitAggregateGridJob<T extends Serializable, R extends Serializable> extends GridJob<T, R>{
	
	/**
	 * Logic to split this {@code GridJob} into multiple 
	 * {@code GridTask}s.
	 * <p>
	 * Should be overridden by all {@code GridJob}s, as it states
	 * the {@code Split} operation of the {@code Split-Aggregate} model.
	 * 
	 * @return A Collection of {@link GridTask}s.
	 */
	public List<? extends GridTask<T>> split();
	
	/**
	 * Logic to aggregate results from tasks into final result of job.
	 * <p>
	 * Should be overridden by all {@code GridJob}s, as it states
	 * the {@code Aggregate} operation of the {@code Split-Aggregate} model.
	 * 
	 * @param results {@code List} of results by {@code GridTask} executions
	 * at remote nodes.
	 * 
	 * @return Final result of {@code GridJob}, after aggregation
	 */
	public R  aggregate(List<? extends Serializable> results);
	


}