package org.nebulaframework.core.job.annotations.unbounded;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.nebulaframework.core.job.annotations.GridTaskAdapter;
import org.nebulaframework.core.job.exceptions.InvalidResultException;
import org.nebulaframework.core.job.unbounded.UnboundedGridJob;
import org.nebulaframework.core.job.unbounded.UnboundedSettingsAware;
import org.nebulaframework.core.task.GridTask;

/**
 * The UnboundedJobAdapter is capable of relaying UnboundedGridJob
 * method invocations to a object which uses Unbounded annotations.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 *
 */
public class UnboundedJobAdapter implements UnboundedGridJob<Serializable>, UnboundedSettingsAware{

	private static final long serialVersionUID = 9126607493856956143L;
	private Serializable adaptee;
	private String taskGenerator;
	private String resultProcessor;
	private String taskMethod;
	private int maxTasksInQueue = 100;
	private boolean mutuallyExclusiveTasks = false;
	private int reductionFactor = 50;
	private boolean stopOnNull = true;
	
	/**
	 * Constructs an UnboundedJobAdapter for given adaptee with the specified
	 * task generator, result processor and task methods.
	 * 
	 * @param adaptee Adaptee
	 * @param taskGenerator task() method
	 * @param resultProcessor processResult() method
	 * @param taskMethod GridTask's execute() method
	 */
	public UnboundedJobAdapter(Serializable adaptee, String taskGenerator,
			String resultProcessor, String taskMethod) {
		super();
		this.adaptee = adaptee;
		this.taskGenerator = taskGenerator;
		this.resultProcessor = resultProcessor;
		this.taskMethod = taskMethod;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable processResult(Serializable result)
			throws InvalidResultException {
		
		// If no processor, no transformation
		if (resultProcessor==null)  {
			return result;
		}
		
		// If we have a processor
		try {
			Method m = adaptee.getClass().getMethod(resultProcessor);
			return (Serializable) m.invoke(adaptee, result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GridTask<Serializable> task() {
		
		try {
			
			Method m = adaptee.getClass().getMethod(taskGenerator);
			Serializable taskAdaptee = (Serializable) m.invoke(adaptee);
			
			if (taskAdaptee == null) return null;
			
			return new GridTaskAdapter<Serializable> (taskAdaptee, taskMethod);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	
	/**
	 * Sets the maximum number of allowed tasks in queue.
	 */
	public void setMaxTasksInQueue(int maxTasksInQueue) {
		this.maxTasksInQueue = maxTasksInQueue;
	}

	/**
	 * Sets whether tasks are mutually exclusive.
	 */
	public void setMutuallyExclusiveTasks(boolean mutuallyExclusiveTasks) {
		this.mutuallyExclusiveTasks = mutuallyExclusiveTasks;
	}

	/**
	 * Sets the reduction factor for task generation.
	 */
	public void setReductionFactor(int reductionFactor) {
		this.reductionFactor = reductionFactor;
	}

	/**
	 * Specifies whether to stop on a null task.
	 */
	public void setStopOnNull(boolean stopOnNull) {
		this.stopOnNull = stopOnNull;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int maxTasksInQueue() {
		return maxTasksInQueue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean mutuallyExclusiveTasks() {
		return mutuallyExclusiveTasks;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int reductionFactor() {
		return reductionFactor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean stopOnNullTask() {
		return stopOnNull;
	}
	
	
	
	
	
}
