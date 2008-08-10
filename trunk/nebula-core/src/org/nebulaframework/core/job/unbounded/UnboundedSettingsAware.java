package org.nebulaframework.core.job.unbounded;

import org.nebulaframework.core.job.annotations.unbounded.UnboundedProcessingSettings;

/**
 * This interface defines the contract which allows UnboundedGridJobs to provide processing 
 * settings. These settings will be processed and used by the Nebula Framework when a 
 * {@code UnboundedGridJob} which implements this interface is submitted to the Grid.
 * <p>
 * Alternatively, consider using the {@link UnboundedProcessingSettings} annotation for 
 * the same purpose without implementing each and every method of this interface.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see UnboundedProcessingSettings
 */
public interface UnboundedSettingsAware {
	/**
	 * Maximum number of tasks in TaskQueue at a given time, without slowing
	 * task generation. Note that this does not define the maximum possible
	 * tasks in queue. However, once this amount is reached, future task 
	 * generation will be gradually slowed down, to avoid overloading the
	 * task queue.
	 * 
	 * @return maximum allowed tasks in queue without reduction
	 */
	int maxTasksInQueue();

	/**
	 * Factor (time) by which the task generation is slowed per task which is
	 * over {@code maxTasksInQueue} (in milliseconds). When the task queue exceeds
	 * {@code maxTasksInQueue}, the next task generation will be delayed by <i>n</i> 
	 * milliseconds where <i>n</i> is  the multiplication of this factor into 
	 * tasks above limit.
	 * <p>
	 * For example, if reductionFactor is 50, {@code maxTasksInQueue} is 100, 
	 * and if current number of tasks in queue are 105, the next task generation 
	 * will be slowed by (105-100) * 50 milliseconds.
	 * 
	 * @return reduction factor in milliseconds
	 */
	int reductionFactor();

	/**
	 * Indicates whether to stop task generation if a null task is returned
	 * after invoking task() method on UnboundedGridJob.
	 * <p>
	 * @return a boolean indicating whether the job should stop on a null task
	 */
	boolean stopOnNullTask();

	/**
	 * Indicates whether the tasks generated for the current UnboundedGridJob
	 * are mutually exclusive, which can be used to increase performance and
	 * to optimize resource utilization.
	 * 
	 * @return a boolean indicating whether the tasks of this job are mutually
	 *         exclusive
	 */
	boolean mutuallyExclusiveTasks();
}
