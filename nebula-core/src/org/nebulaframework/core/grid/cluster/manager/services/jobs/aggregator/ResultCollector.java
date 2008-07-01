package org.nebulaframework.core.grid.cluster.manager.services.jobs.aggregator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.ClusterJobServiceImpl;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.core.task.GridTaskResult;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class ResultCollector {
	
	private static Log log = LogFactory.getLog(ResultCollector.class);
	
	private GridJobProfile profile;
	private ClusterJobServiceImpl jobService;
	private DefaultMessageListenerContainer container;
	
	public ResultCollector(GridJobProfile profile, ClusterJobServiceImpl jobService) {
		super();
		this.profile = profile;
		this.jobService = jobService;
	}

	public GridJobProfile getProfile() {
		return profile;
	}
	
	public void onResult(GridTaskResult result) {
		// Result
		log.debug("Result Received " + result);
		
		
		if (result.isComplete()) { // Result is Valid / Complete
			profile.getResultMap().put(result.getTaskId(), result);
			profile.getTaskMap().remove(result.getTaskId());
			
			log.debug("Result Success " + result.getResult());
			
			// Check if all results are collected
			if (profile.getTaskMap().size() == 0) {
				jobService.getAggregatorService().aggregateResults(profile);
				this.destroy();	// Destory the Result Collector
			}
			
		}
		else { // Result Not Valid / Exception
			log.debug("Result Failed, ReEnqueueing - " + result.getException());
			jobService.getSplitterService().reEnqueueTask(profile.getJobId(), result.getTaskId(), profile.getTaskMap().get(result.getTaskId()));
		}
	}

	/**
	 * Called once all results are collected for the Job, to destroy the ResultCollector. 
	 * This method shutdowns the MessageListnerContainer for result queue.
	 */
	protected void destroy() {
		if (container != null) container.shutdown();
	}
	
	public void setContainer(DefaultMessageListenerContainer container) {
		this.container = container;
	}
	
}
