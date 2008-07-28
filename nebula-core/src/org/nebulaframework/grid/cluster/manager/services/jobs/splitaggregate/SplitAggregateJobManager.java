package org.nebulaframework.grid.cluster.manager.services.jobs.splitaggregate;

import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.SplitAggregateGridJob;
import org.nebulaframework.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.grid.cluster.manager.services.jobs.JobExecutionManager;
import org.springframework.beans.factory.annotation.Required;

// TODO FixDoc
public class SplitAggregateJobManager implements JobExecutionManager {

	private SplitterService splitter;
	private AggregatorService aggregator;
	


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
	
	

}
