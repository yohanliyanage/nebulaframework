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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.exceptions.AggregateException;
import org.nebulaframework.core.job.splitaggregate.SplitAggregateGridJob;
import org.nebulaframework.core.task.GridTaskResult;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.manager.services.jobs.ClusterJobService;
import org.nebulaframework.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.grid.cluster.manager.services.jobs.InternalClusterJobService;
import org.nebulaframework.util.jms.JMSNamingSupport;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

/**
 * Implementation of {@code AggregatorService}.
 * <p>
 * {@code AggregatorService} collects results of {@code GridTask}s from 
 * participating {@code GridNode}s and aggregates results to calculate
 * the final result for the {@code GridJob}.
 * <p>
 * <i>Spring Managed</i>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see AggregatorService
 * @see ClusterJobService
 */
public class AggregatorServiceImpl implements AggregatorService {

	private static Log log = LogFactory.getLog(AggregatorServiceImpl.class);
	
	private ConnectionFactory connectionFactory;
	private InternalClusterJobService jobService;
	private boolean initialized = false;
	
	/**
	 * Constructs an AggregatorServiceImpl instance.
	 */
	public AggregatorServiceImpl() {
		super();

	}
	
	private synchronized void initialize() {
		if (!initialized) {
			ClusterManager manager = ClusterManager.getInstance();
			this.jobService = manager.getJobService();			
			this.connectionFactory = ClusterManager.getInstance().getConnectionFactory();
			initialized = true;
		}
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This method simply delegates to {@code #doStartAggregator(GridJobProfile)} method,
	 * invoked on a separate {@code Thread}.
	 */
	public synchronized void startAggregator(final GridJobProfile profile, final SplitAggregateJobManager manager) {
		
		initialize();
		
		//Start Aggregator, on a new Thread
		new Thread(new Runnable() {
			public void run() {
				doStartAggregator(profile, manager);
			}
		}, "Aggregator[ResultCollector]-" + profile.getJobId()).start();
	}

	/**
	 * Internal method which handles the aggregator start-up for 
	 * a given {@code GridJobProfile}.
	 * <p>
	 * Creates and starts a {@code ResultCollector} instance for
	 * the given {@code GridJob}, and also creates necessary JMS message
	 * handling infrastructure.
	 * 
	 * @param profile {@code GridJobProfile} jobProfile
	 */
	protected void doStartAggregator(GridJobProfile profile, SplitAggregateJobManager manager) {
		
		// Create Message Listener Adapter and Result Collector for Job
		MessageListenerAdapter adapter = new MessageListenerAdapter();
		adapter.setDefaultListenerMethod("onResult");

		// Create JMS Message Listener Container
		DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setDestinationName(JMSNamingSupport
				.getResultQueueName(profile.getJobId()));
		container.setMessageListener(adapter);
		
		//Create results collector and set it as Execution Manager
		ResultCollector collector = new ResultCollector(profile, manager, container);
		profile.setExecutionManager(manager);
		manager.addResultCollector(profile.getJobId(), collector);
		
		// Initialize Adapter and Container
		adapter.setDelegate(collector);
		container.afterPropertiesSet();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method simply delegates to {@code #doAggregateResults(GridJobProfile)} method,
	 * invoked on a separate {@code Thread}.
	 */
	public void aggregateResults(final GridJobProfile profile) {

		// Aggregate results, on a separate Thread
		new Thread(new Runnable() {
			public void run() {
				doAggregateResults(profile);
			}
		}, "Aggregator[Aggregate]-" + profile.getJobId()).start();

	}

	/**
	 * Internal method which does the aggregation of {@code GridTaskResult}s for a 
	 * given {@code GridJob}, denoted by corresponding {@code GridJobProfile}.
	 * <p>
	 * Furthermore, this method invokes necessary methods of {@code ClusterJobServiceImpl}
	 * to notify workers about events such as End of Job Execution.<p>
	 * 
	 * @param profile {@code GridJobProfile} of {@code GridJob}
	 */
	protected void doAggregateResults(GridJobProfile profile) {
		
		Serializable jobResult = null;
		
		// Get GridTaskResults
		Collection<GridTaskResult> taskResults = profile.getResults();
		List<Serializable> results = new ArrayList<Serializable>();
		
		try {
			log.info("[Aggregator] Aggregating Results for Job : {" + profile.getJobId() + "}");
			
			// Fetch the result from GridTaskResults
			for (GridTaskResult result : taskResults) {
				results.add(result.getResult());
			}
			
			// Do Aggregation
			jobResult = ((SplitAggregateGridJob<?, ?>)profile.getJob()).aggregate(results);
			
		} catch (SecurityException e) {
			
			log.error("[Aggregator] Security Violation while aggregating final result of "
					+ profile.getJobId(), e);
			
			// Update Future
			profile.getFuture().setException(e);
			profile.getFuture().setState(GridJobState.FAILED);
			
			// Notify Workers that Job is canceled
			jobService.notifyJobCancel(profile.getJobId());
			
			return;
		} catch (Exception e) {
			
			log.warn("[Aggregator] Exception while aggregating final result of "
					+ profile.getJobId(), e);
			
			// Update Future
			profile.getFuture().setException(new AggregateException(e));
			profile.getFuture().setState(GridJobState.FAILED);
			
			// Notify Workers that Job is canceled
			jobService.notifyJobCancel(profile.getJobId());
			
			return;
		}

		/*  -- Aggregation Complete : Job Complete -- */
		
		// Notify Workers
		jobService.notifyJobEnd(profile.getJobId());
		
		// Update Future and return Result
		profile.getFuture().setResult(jobResult);
		profile.getFuture().setState(GridJobState.COMPLETE);
	}


}
