package org.nebulaframework.core.grid.cluster.manager.services.jobs;

import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.aggregator.AggregatorService;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.splitter.SplitterService;
import org.nebulaframework.core.job.GridJob;

/**
 * Internal interface definition for {@code ClusterJobService}, which
 * is responsible for the {@code GridJob} submission and execution, with 
 * in a {@code ClusterManager}. Internal interface extends the public interface,
 * but allows to access members which are not exposed by the public API.
 * <p>
 * This is to be used by the internal system only, and is not a part of the
 * public API.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClusterManager
 * @see GridJob
 *
 */
public interface InternalClusterJobService extends ClusterJobService {

	/**
	 * Notifies to GridNodes that a particular GridJob has finished execution.
	 * 
	 * @param jobId JobId of the finished GridJob
	 */
	public void notifyJobEnd(String jobId);

	/**
	 * Notifies to GridNodes that a particular GridJob has been canceled.
	 * 
	 * @param jobId JobId of the canceled GridJob.
	 */
	public void notifyJobCancel(String jobId);

	/**
	 * Returns the {@code SplitterService} used by the {@code ClusterJobServiceImpl}.
	 * <p>
	 * {@code SplitterService} is responsible for splitting a given {@code GridJob} into
	 * {@code GridTask}s which are to be executed remotely.
	 * 
	 * @return {@code SplitterService} reference.
	 */
	public SplitterService getSplitterService();

	/**
	 * Returns the {@code AggregatorService} used by the {@code ClusterJobServiceImpl}.
	 * <p>
	 * {@code AggregatorService} is responsible for collecting results returned by each 
	 * {@code GridTask} which was executed on a remote node, and to aggregate the results
	 * to provide the final result for the {@code GridJob}.
	 * 
	 * @return {@code AggregatorService} reference.
	 */
	public AggregatorService getAggregatorService();

}
