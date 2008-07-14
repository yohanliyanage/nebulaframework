package org.nebulaframework.core.job.future;

import org.nebulaframework.core.job.GridJobStateListener;
import org.nebulaframework.core.job.SplitAggregateGridJob;
import org.nebulaframework.core.job.unbounded.UnboundedGridJob;

// TODO FixDoc
public interface InternalGridJobFuture extends GridJobFuture {
	
	// TODO FixDoc
	public void addFinalResultCallback(String queueName);
	
	/**
	 * Adds the given {@code GridJobStateListener} as a listener to the
	 * {@code GridJob} represented by this {@code GridJobFuture}.
	 * 
	 * @param listener
	 *            {@code GridJobStateListener} to add
	 */
	public void addGridJobStateListener(GridJobStateListener listener);

	/**
	 * Removes the given {@code GridJobStateListener} from the collection of
	 * {@code GridJobStateListener}s of this {@code GridJobFuture}.
	 * 
	 * @param listener
	 *            {@code GridJobStateListener} to remove
	 * @return a {@code boolean} value, {@code true} if success, {@code false}
	 *         otherwise.
	 */
	public boolean removeGridJobStateListener(GridJobStateListener listener);
	
	/**
	 * Returns {@code true} if this {@code GridJob} supports
	 * final result. Currently {@link SplitAggregateGridJob}
	 * supports final result, where as {@link UnboundedGridJob}
	 * does not.
	 * 
	 * @return value @code true} final result supported
	 */
	public boolean isFinalResultSupported();
}
