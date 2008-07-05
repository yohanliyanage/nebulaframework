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

package org.nebulaframework.core.grid.cluster.manager.services.jobs.aggregator;

import org.nebulaframework.core.grid.cluster.manager.services.jobs.ClusterJobService;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.core.grid.cluster.node.GridNode;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.nebulaframework.core.task.GridTask;

/**
 * <p><tt>AggregatorService</tt> collects results of {@link GridTask}s from 
 * participating {@link GridNode}s and aggregates results to calculate
 * the final result for the {@link GridJob}.</p>
 * 
 * <p>This is a support service of {@link ClusterJobService}.</p>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClusterJobService
 */
public interface AggregatorService {
	
	/**
	 * Starts an AggregatorService for given {@link GridJob}, 
	 * denoted by {@link GridJobProfile}.
	 * 
	 * @param profile <tt>GridJobProfile</tt> of <tt>GridJob</tt>
	 */
	public void startAggregator(GridJobProfile profile);
	
	/**
	 * <p>Aggregates results for given {@link GridJob}, denoted by
	 * {@link GridJobProfile}.</p>
	 * 
	 * <p>The final result is updated in the {@link GridJobFuture},
	 * through {@link GridJobProfile}.</p>
	 * 
	 * @param profile <tt>GridJobProfile</tt> of <tt>GridJob</tt>
	 */
	public void aggregateResults(GridJobProfile profile);
}
