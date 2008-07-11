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
import org.nebulaframework.core.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.InternalClusterJobService;
import org.nebulaframework.core.task.GridTaskResult;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * {@code ResultCollector} is responsible of collecting
 * {@code GridTaskResult}s from participating {@code GridNode}s, for a given
 * {@code GridJob}.
 * <p>
 * This class assists the {@code AggregatorService}, which in turn assists
 * {@code ClusterJobService}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class ResultCollector {

	private static Log log = LogFactory.getLog(ResultCollector.class);

	private GridJobProfile profile;
	private InternalClusterJobService jobService;
	private DefaultMessageListenerContainer container;

	/**
	 * Constructs a {@code ResultCollector} instance for given {@code GridJob}.
	 * 
	 * @param profile {@code GridJobProfile} for {@code GridJob}
	 * @param jobService {@code ClusterJobServiceImpl} JobService Implementation
	 * @param container Spring {@code DefaultMessageListenerContainer} for {@code ResultsQueue}
	 */
	public ResultCollector(GridJobProfile profile,
			InternalClusterJobService jobService,
			DefaultMessageListenerContainer container) {
		super();
		this.profile = profile;
		this.jobService = jobService;
		this.container = container;
	}

	/**
	 * Called by {@code DefaultMessageListenerContainer}, when a {@code GridTaskResult} arrives
	 * to the {@code ResultQueue} for the {@code GridJob}.
	 * 
	 * @param result {@code GridTaskResult}
	 */
	public void onResult(GridTaskResult result) {

		if (result.isComplete()) { // Result is Valid / Complete
			
			log.debug("[ResultCollector] Received : Task " + result.getTaskId());
			
			// Put result to ResultMap, and remove Task from TaskMap
			profile.getResultMap().put(result.getTaskId(), result);
			profile.getTaskMap().remove(result.getTaskId());
			
			log.debug("[ResultCollector] Remaining Tasks  (" + profile.getTaskMap().size() + ")");
			if (profile.getTaskMap().size() == 0) { // If all results collected
				
				// Aggregate result
				jobService.getAggregatorService().aggregateResults(profile);
				
				this.destroy(); // Destroy the Result Collector
			}

		} else { // Result Not Valid / Exception
			
			log.warn("[ResultCollector] Result Failed, ReEnqueueing - " + result.getException());
			
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
	 * Returns the {@code GridJobProfile} for the {@code GridJob}
	 * of the {@code ResultCollector}.
	 * 
	 * @return GridJobProfile for the GridJob
	 */
	public GridJobProfile getProfile() {
		return profile;
	}

}
