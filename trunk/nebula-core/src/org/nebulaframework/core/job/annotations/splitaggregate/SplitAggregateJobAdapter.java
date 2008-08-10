package org.nebulaframework.core.job.annotations.splitaggregate;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.nebulaframework.core.job.annotations.GridTaskAdapter;
import org.nebulaframework.core.job.splitaggregate.SplitAggregateGridJob;
import org.nebulaframework.core.task.GridTask;

// TODO FixDoc
public class SplitAggregateJobAdapter<T extends Serializable, R extends Serializable> implements SplitAggregateGridJob<T,R>{

	private static final long serialVersionUID = 1378671135368561704L;
	
	private Serializable adaptee;
	private String splitMethod;
	private String aggregateMethod;
	private String taskMethod;
	
	
	public SplitAggregateJobAdapter(Serializable adaptee, Method splitMethod,
			Method aggregateMethod, Method taskMethod) {
		super();
		this.adaptee = adaptee;
		this.splitMethod = splitMethod.getName();
		this.aggregateMethod = aggregateMethod.getName();
		this.taskMethod = taskMethod.getName();
	}

	@SuppressWarnings("unchecked")
	public R aggregate(List<? extends Serializable> results) {
		try {
			Method m = adaptee.getClass().getMethod(aggregateMethod, List.class);
			return (R) m.invoke(adaptee, results);
		} catch (Exception e) {
			throw new RuntimeException("Exception in aggregate",e);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<? extends GridTask<T>> split() {
		
		List<GridTask<T>> taskList = new ArrayList<GridTask<T>>();
		
		List<?> list = null;
		
		try {
			Method m = adaptee.getClass().getMethod(splitMethod);
			list = (List<?>) m.invoke(adaptee);
		} catch (Exception e) {
			throw new RuntimeException("Exception in split",e);
		}
		
		if (list == null) {
			throw new NullPointerException("Task list was null");
		}

		for (Object obj : list) {
			if (!(obj instanceof Serializable)) {
				throw new IllegalArgumentException("Returned List from Split method was not Serializable");
			}
			
			// If not a GridTask, adapt
			taskList.add(new GridTaskAdapter((Serializable) obj, taskMethod));
			
		}

		return taskList;
	}
	
}
