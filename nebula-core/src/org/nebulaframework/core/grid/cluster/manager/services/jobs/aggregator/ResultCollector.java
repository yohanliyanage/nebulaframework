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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.ClusterJobService;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.ClusterJobServiceImpl;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.core.grid.cluster.node.GridNode;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.task.GridTaskResult;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * <p>
 * <tt>ResultCollector</tt> is responsible of collecting
 * {@link GridTaskResult}s from participating {@link GridNode}s, for a given
 * {@link GridJob}.
 * </p>
 * 
 * <p>
 * This class assists the {@link AggregatorService}, which in turn assists
 * {@link ClusterJobService}.
 * </p>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class ResultCollector {

	private static Log log = LogFactory.getLog(ResultCollector.class);

	private GridJobProfile profile;
	private ClusterJobServiceImpl jobService;
	private DefaultMessageListenerContainer container;

	/**
	 * Constructs a {@link ResultCollector} instance for given <tt>GridJob</tt>.
	 * 
	 * @param profile <tt>GridJobProfile</tt> for <tt>GridJob</tt>
	 * @param jobService {@link ClusterJobServiceImpl} JobService Implementation
	 * @param container Spring {@link DefaultMessageListenerContainer} for <tt>ResultsQueue</tt>
	 */
	public ResultCollector(GridJobProfile profile,
			ClusterJobServiceImpl jobService,
			DefaultMessageListenerContainer container) {
		super();
		this.profile = profile;
		this.jobService = jobService;
		this.container = container;
	}

	/**
	 * Called by {@link DefaultMessageListenerContainer}, when a {@link GridTaskResult} arrives
	 * to the <tt>ResultQueue</tt> for the <tt>GridJob</tt>.
	 * 
	 * @param result {@link GridTaskResult}
	 */
	public void onResult(GridTaskResult result) {

		if (result.isComplete()) { // Result is Valid / Complete
			
			// Put result to ResultMap, and remove Task from TaskMap
			profile.getResultMap().put(result.getTaskId(), result);
			profile.getTaskMap().remove(result.getTaskId());

			if (profile.getTaskMap().size() == 0) { // If all results collected
				
				// Aggregate result
				jobService.getAggregatorService().aggregateResults(profile);
				
				this.destroy(); // Destroy the Result Collector
			}

		} else { // Result Not Valid / Exception
			
			log.debug("Result Failed, ReEnqueueing - " + result.getException());
			
			//Request re-enqueue of Task
			jobService.getSplitterService().reEnqueueTask(profile.getJobId(),
					result.getTaskId(),
					profile.getTaskMap().get(result.getTaskId()));
		}
	}

	/**
	 * Called once all results are collected for the Job, to destroy the
	 * ResultCollector. This method shutdowns the MessageListnerContainer for
	 * result queue.
	 */
	protected void destroy() {
		if (container != null)
			container.shutdown();
	}
	
	/**
	 * Returns the {@link GridJobProfile} for the <tt>GridJob</tt>
	 * of the <tt>ResultCollector</tt>.
	 * 
	 * @return {@link GridJobProfile} for the <tt>GridJob</tt>
	 */
	public GridJobProfile getProfile() {
		return profile;
	}

}
