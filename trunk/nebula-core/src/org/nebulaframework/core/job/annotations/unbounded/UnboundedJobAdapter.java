package org.nebulaframework.core.job.annotations.unbounded;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.nebulaframework.core.job.annotations.GridTaskAdapter;
import org.nebulaframework.core.job.exceptions.InvalidResultException;
import org.nebulaframework.core.job.unbounded.UnboundedGridJob;
import org.nebulaframework.core.job.unbounded.UnboundedSettingsAware;
import org.nebulaframework.core.task.GridTask;

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
	
	public UnboundedJobAdapter(Serializable adaptee, String taskGenerator,
			String resultProcessor, String taskMethod) {
		super();
		this.adaptee = adaptee;
		this.taskGenerator = taskGenerator;
		this.resultProcessor = resultProcessor;
		this.taskMethod = taskMethod;
	}

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
	
	

	public void setMaxTasksInQueue(int maxTasksInQueue) {
		this.maxTasksInQueue = maxTasksInQueue;
	}

	public void setMutuallyExclusiveTasks(boolean mutuallyExclusiveTasks) {
		this.mutuallyExclusiveTasks = mutuallyExclusiveTasks;
	}

	public void setReductionFactor(int reductionFactor) {
		this.reductionFactor = reductionFactor;
	}

	public void setStopOnNull(boolean stopOnNull) {
		this.stopOnNull = stopOnNull;
	}

	@Override
	public int maxTasksInQueue() {
		return maxTasksInQueue;
	}

	@Override
	public boolean mutuallyExclusiveTasks() {
		return mutuallyExclusiveTasks;
	}

	@Override
	public int reductionFactor() {
		return reductionFactor;
	}

	@Override
	public boolean stopOnNullTask() {
		return stopOnNull;
	}
	
	
	
	
	
}
