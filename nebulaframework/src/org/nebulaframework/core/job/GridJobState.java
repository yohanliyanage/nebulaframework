package org.nebulaframework.core.job;

/**
 * Represents the various states a Grid Job can exist.
 * @author Yohan Liyanage
 *
 */
public enum GridJobState {
	
	/**
	 * GridJob is in waiting state (to be enqueued). 
	 */
	WAITING,
	
	/**
	 * GridJob is enqueued for execution, but has not started yet.
	 */
	ENQUEUED,
	
	/**
	 * GridJob has been rejected by System.
	 */
	REJECTED,
	
	/**
	 * GridJob is executing on the Grid.
	 */
	EXECUTING,
	
	/**
	 * GridJob has completed successfully.
	 */
	COMPLETE,
	
	/**
	 * GridJob has been canceled.
	 */
	CANCELED,
	
	/**
	 * GridJob has failed during execution.
	 */
	FAILED;
}
