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
import org.nebulaframework.core.grid.cluster.node.GridNode;
import org.nebulaframework.core.task.GridTask;

/**
 * <p><tt>SplitterService</tt> splits a given {@link GridJob} to {@link GridTask}s which can be
 * executed on remote {@link GridNode}s. Furthermore, it enqueues the {@link GridTask}s in the
 * <tt>TaskQueue</tt> for the <tt>GridJob</tt>. </p>
 * 
 * <p>This is a support service of {@link ClusterJobService}.</p>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 *
 * @see ClusterJobService
 */
public interface SplitterService {
	
	/**
	 * Starts Splitter for given <tt>GridJob</tt>, denoted by {@link GridJobProfile}.
	 * 
	 * @param profile {@link GridJobProfile} for the <tt>GridJob</tt>
	 */
	public void startSplitter(GridJobProfile profile);
	
	/**
	 * Re-enqueues a given GridTask in the <tt>TaskQueue</tt>. Usually this is used to re-enqueue
	 * failed tasks.
	 * 
	 * @param jobId JobId of GridJob
	 * @param taskId TaskId of Task to be re-enqueued
	 * @param task {@link GridTask} to be re-enqueued
	 */
	public void reEnqueueTask(final String jobId, final int taskId, GridTask<? extends Serializable> task);
}
