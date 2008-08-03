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
package org.nebulaframework.grid.cluster.manager.services.jobs.splitaggregate;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.splitaggregate.SplitAggregateGridJob;
import org.nebulaframework.grid.cluster.manager.services.jobs.AbstractJobExecutionManager;
import org.nebulaframework.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.grid.cluster.manager.services.jobs.tracking.GridJobTaskTracker;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;
import org.springframework.beans.factory.annotation.Required;

/**
 * Split-Aggregate JobManager, which manages execution of
 * Split-Aggregate Model GridJobs. This class holds references
 * to {@code SplitterService} and {@code AggregatorService}, which handles the
 * split and aggregate processes, and also {@code ResultCollector}s,
 * which are responsible for collecting individual results.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class SplitAggregateJobManager extends AbstractJobExecutionManager {
	
	private static final Log log = LogFactory.getLog(SplitAggregateJobManager.class);
	
	private SplitterService splitter;
	private AggregatorService aggregator;
	
	// Result Collectors
	private Map<String, ResultCollector> collectors = new HashMap<String, ResultCollector>();


	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns the Class for {@code SplitAggregateGridJob}.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends GridJob> getInterface() {
		// Return class for SplitAggregateGridJob
		return SplitAggregateGridJob.class;
	}

	/**
	 * Starts given {@code SplitAggregateJob} on the Grid.
	 * 
	 * @param profile GridJobProfile
	 */
	@Override
	public boolean startExecution(GridJobProfile profile) {
		
		// If valid GridJob Type
		if (profile.getJob() instanceof SplitAggregateGridJob) {
			
			// Allow Final Results
			profile.getFuture().setFinalResultSupported(true);
			
			// Create Task Tracker
			profile.setTaskTracker(GridJobTaskTracker.startTracker(profile, this));
			
			// Start Splitter & Aggregator for GridJob
			splitter.startSplitter(profile);
			aggregator.startAggregator(profile, this);
			return true;
		}
		else {
			return false;
		}
		
	}
	
	/**
	 * Sets the {@code SplitterService} used by the
	 * {@code JobExecutionManager}.
	 * <p>
	 * {@code SplitterService} is responsible for splitting a given
	 * {@code GridJob} into {@code GridTask}s which are to be executed
	 * remotely.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param splitterService
	 *            SplitterService for the {@code ClusterJobServiceImpl}
	 */
	@Required
	public void setSplitter(SplitterService splitter) {
		this.splitter = splitter;
	}

	/**
	 * Returns the {@code AggregatorService} used by the
	 * {@code ClusterJobServiceImpl}.
	 * <p>
	 * {@code AggregatorService} is responsible for collecting results returned
	 * by each {@code GridTask} which was executed on a remote node, and to
	 * aggregate the results to provide the final result for the {@code GridJob}.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param aggregatorService
	 *            {@code AggregatorService} for the service
	 */
	@Required
	public void setAggregator(AggregatorService aggregator) {
		this.aggregator = aggregator;
	}
	
	/**
	 * Returns the {@code SplitterService} used by the
	 * {@code ClusterJobServiceImpl}.
	 * <p>
	 * {@code SplitterService} is responsible for splitting a given
	 * {@code GridJob} into {@code GridTask}s which are to be executed
	 * remotely.
	 * 
	 * @return {@code SplitterService} reference.
	 */
	public SplitterService getSplitter() {
		return splitter;
	}

	/**
	 * Returns the {@code AggregatorService} used by the
	 * {@code ClusterJobServiceImpl}.
	 * <p>
	 * {@code AggregatorService} is responsible for collecting results returned
	 * by each {@code GridTask} which was executed on a remote node, and to
	 * aggregate the results to provide the final result for the {@code GridJob}.
	 * 
	 * @return {@code AggregatorService} reference.
	 */
	public AggregatorService getAggregator() {
		return aggregator;
	}
	
	/**
	 * Adds a {@code ResultCollector} for the given Job, denoted by
	 * the jobId.
	 * 
	 * @param jobId GridJob Id
	 * @param collector ResultCollector
	 * @throws IllegalArgumentException if the specified GridJob is already
	 * bound to a ResultCollector
	 */
	public void addResultCollector(final String jobId, ResultCollector collector) 
		throws IllegalArgumentException {
		
		if (!this.collectors.containsKey(jobId)) {
			
			// Add Collector
			collectors.put(jobId, collector);
			
			// Create Removal Hook
			ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {

				@Override
				public void onServiceEvent(ServiceMessage message) {
					collectors.remove(jobId);
				}
				
			}, jobId, ServiceMessageType.JOB_END);
		}
		else {
			throw new IllegalArgumentException("[Split-Aggregate] Result Collector Registered for Job " 
			                                   + jobId);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean cancel(String jobId) {
		
		if (collectors.containsKey(jobId)){
			boolean result = collectors.get(jobId).cancel();
			
			// Record Cancellation
			if (result) {
				markCanceled(jobId);
			}
			
			return result;
		}
		else {
			
			// If this job was canceled already
			if (isRecentlyCancelled(jobId)) return true;
			log.warn("[SplitAggregateJobService] Unable to Cancel Job " 
			         + jobId + " : No Processor Reference");
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reEnqueueTask(String jobId, int taskId) {
		try {
			log.debug("[SplitAggregateJobService] Re-enqueing  Task"
			          + jobId + "|" + taskId);
			splitter.reEnqueueTask(jobId, taskId);
		} catch (RuntimeException e) {
			log.debug("[SplitAggregateJobService] Unable to Re-enqueue Task " 
	         + jobId + "|" + taskId, e);
		}
	}
	

}
