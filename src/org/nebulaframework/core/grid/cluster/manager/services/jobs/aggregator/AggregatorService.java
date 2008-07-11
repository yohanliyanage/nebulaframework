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

/**
 * {@code AggregatorService} collects results of {@code GridTask}s from 
 * participating {@code GridNode}s and aggregates results to calculate
 * the final result for the {@code GridJob}.
 * <p>
 * This is a support service of {@code ClusterJobService}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClusterJobService
 */
public interface AggregatorService {
	
	/**
	 * Starts an AggregatorService for given {@code GridJob}, 
	 * denoted by {@code GridJobProfile}.
	 * 
	 * @param profile {@code GridJobProfile} of {@code GridJob}
	 */
	public void startAggregator(GridJobProfile profile);
	
	/**
	 * Aggregates results for given {@code GridJob}, denoted by
	 * {@code GridJobProfile}.
	 * <p>
	 * The final result is updated in the {@code GridJobFuture},
	 * through {@code GridJobProfile}.
	 * 
	 * @param profile {@code GridJobProfile} of {@code GridJob}
	 */
	public void aggregateResults(GridJobProfile profile);
}
