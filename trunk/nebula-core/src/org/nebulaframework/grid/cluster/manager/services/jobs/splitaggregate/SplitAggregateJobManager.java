package org.nebulaframework.grid.cluster.manager.services.jobs.splitaggregate;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.splitaggregate.SplitAggregateGridJob;
import org.nebulaframework.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.grid.cluster.manager.services.jobs.JobExecutionManager;
import org.nebulaframework.grid.cluster.manager.services.jobs.tracking.GridJobTaskTracker;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;
import org.springframework.beans.factory.annotation.Required;

// TODO FixDoc
public class SplitAggregateJobManager implements JobExecutionManager {
	
	private static final Log log = LogFactory.getLog(SplitAggregateJobManager.class);
	
	private SplitterService splitter;
	private AggregatorService aggregator;
	
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
			
			// Start Splitter & Aggregator for GridJob
			splitter.startSplitter(profile);
			aggregator.startAggregator(profile, this);
			profile.setTaskTracker(GridJobTaskTracker.startTracker(profile, this));
			
			
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
	
	// TODO FixDoc
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
				
			}, jobId, ServiceMessageType.JOB_CANCEL, ServiceMessageType.JOB_END);
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
			return collectors.get(jobId).cancel();
		}
		else {
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
