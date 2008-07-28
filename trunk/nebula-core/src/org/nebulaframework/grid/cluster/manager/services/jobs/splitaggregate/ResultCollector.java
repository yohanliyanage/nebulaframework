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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.GridJobStateListener;
import org.nebulaframework.core.task.GridTaskResult;
import org.nebulaframework.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.grid.cluster.manager.support.CleanUpSupport;
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
public class ResultCollector implements GridJobStateListener {

	private static Log log = LogFactory.getLog(ResultCollector.class);

	private boolean aggregated = false;
	
	private GridJobProfile profile;
	private SplitAggregateJobManager jobManager;
	private DefaultMessageListenerContainer container;

	/**
	 * Constructs a {@code ResultCollector} instance for given {@code GridJob}.
	 * 
	 * @param profile {@code GridJobProfile} for {@code GridJob}
	 * @param jobManager {@code SplitAggreateJobManager} Job Manager
	 * @param container Spring {@code DefaultMessageListenerContainer} for {@code ResultsQueue}
	 */
	public ResultCollector(GridJobProfile profile,
			SplitAggregateJobManager jobManager,
			DefaultMessageListenerContainer container) {
		super();
		this.profile = profile;
		this.jobManager = jobManager;
		this.container = container;
		
		// Register as a GridJobState Listener
		profile.getFuture().addGridJobStateListener(this);
		
		// Create Job End Service Hook for Clean Up
		CleanUpSupport.shutdownContainerWhenFinished(profile.getJobId(), container);
		
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
			
			boolean finished = false;
			int taskCount = -1;
				
			// Put result to ResultMap, and remove Task from TaskMap
			taskCount = profile.addResultAndRemoveTask(result.getTaskId(), result);
			
			// Update Task Tracker
			profile.getTaskTracker().resultReceived(result.getTaskId(), result.getExecutionTime());
			
			// Check if Job has finished
			finished = (taskCount == 0)&&(profile.getFuture().getState()== GridJobState.EXECUTING);

			// If Job Finished
			if (finished) { 
				// Aggregate result
				doAggregate();
			}

		} else { // Result Not Valid / Exception
			
			log.warn("[ResultCollector] Result Failed, ReEnqueueing - " + result.getException());
			
			// Update Profile
			profile.failedTaskReceived();
			
			//Request re-enqueue of Task
			jobManager.getSplitter().reEnqueueTask(profile.getJobId(),
					result.getTaskId());
		}
	}

	/**
	 * Invokes the aggregation process by calling
	 * the {@code aggregateResults} method of the
	 * {@code AggregatorService}.
	 */
	private synchronized void doAggregate() {
		if (!aggregated) {
			aggregated = true;
			jobManager.getAggregator().aggregateResults(profile);
			this.destroy(); // Destroy the Result Collector
		}
	}
	/**
	 * Called once all results are collected for the Job, to destroy the
	 * ResultCollector. This method shutdowns the MessageListnerContainer for
	 * result queue.
	 */
	protected void destroy() {
		if (container != null) {
			container.shutdown();
		}
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

	/**
	 * {@inheritDoc}
	 */
	public boolean cancel() {
		try {
			this.destroy();
			return true;
		}
		catch (Exception e) {
			log.error("Exception while cancelling job",e);
			return false;
		}
	}

	/**
	 * Invoked when {@code GridJob}'s state has changed.
	 * <p>
	 * If the new state is executing and remaining task count 
	 * is zero, this will invoke {@link #doAggregate()}.
	 * <p>
	 * The reason for this is to guard against situations where the
	 * result collection completes before the {@link SplitterService}
	 * updates the {@code GridJob}'s state to {@link GridJobState#EXECUTING}.
	 * In such situations, {@link #doAggregate()} will not be invoked
	 * by the {@link #onResult(GridTaskResult)} method, as no new result
	 * will arrive.
	 * <p>
	 * In such situations, this method will be invoked and it will in turn
	 * invoke the {@link #doAggregate()} method.
	 * 
	 * @param newState New State of the {@code GridJob}
	 */
	public synchronized void stateChanged(GridJobState newState) {
		if ((newState==GridJobState.EXECUTING) && (profile.getTaskCount()==0)) {
			doAggregate();
		}
	}

}
