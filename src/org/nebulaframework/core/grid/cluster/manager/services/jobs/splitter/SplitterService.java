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

package org.nebulaframework.core.grid.cluster.manager.services.jobs.splitter;

import java.io.Serializable;

import org.nebulaframework.core.grid.cluster.manager.services.jobs.ClusterJobService;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.core.task.GridTask;

/**
 * {@code SplitterService} splits a given {@code GridJob} to {@code GridTask}s which can be
 * executed on remote {@code GridNode}s. Furthermore, it enqueues the {@code GridTask}s in the
 * {@code TaskQueue} for the {@code GridJob}. 
 * <p>
 * This is a support service of {@code ClusterJobService}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 *
 * @see ClusterJobService
 */
public interface SplitterService {
	
	/**
	 * Starts Splitter for given {@code GridJob}, denoted by {@code GridJobProfile}.
	 * 
	 * @param profile {@code GridJobProfile} for the {@code GridJob}
	 */
	public void startSplitter(GridJobProfile profile);
	
	/**
	 * Re-enqueues a given GridTask in the {@code TaskQueue}. Usually this is used to re-enqueue
	 * failed tasks.
	 * 
	 * @param jobId JobId of GridJob
	 * @param taskId TaskId of Task to be re-enqueued
	 * @param task {@code GridTask} to be re-enqueued
	 */
	public void reEnqueueTask(final String jobId, final int taskId, GridTask<? extends Serializable> task);
}
