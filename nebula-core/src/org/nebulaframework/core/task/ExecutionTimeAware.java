package org.nebulaframework.core.task;

/**
 * This interface is used to indicate that a result of a GridTask 
 * is aware of the execution time taken to execute the task
 * in the remote node.
 * <p>
 * If the result type of a GridTask implements this interface, 
 * the framework automatically injects the execution time into 
 * the result object.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public interface ExecutionTimeAware {

	/**
	 * Sets the time taken to execute the task,
	 * in milliseconds.
	 * 
	 * @param time execution time
	 */
	public void setExecutionTime(long time);
}
