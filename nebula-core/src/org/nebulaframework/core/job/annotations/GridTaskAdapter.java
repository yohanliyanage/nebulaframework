package org.nebulaframework.core.job.annotations;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.grid.GridExecutionException;

// TODO FixDoc
public class GridTaskAdapter<T extends Serializable> implements GridTask<T>{

	private static final long serialVersionUID = -6295188093453101037L;
	private Serializable adaptee;
	private String executeMethod;
	
	
	public GridTaskAdapter(Serializable adaptee, String executeMethod) {
		super();
		this.adaptee = adaptee;
		this.executeMethod = executeMethod;
	}


	@SuppressWarnings("unchecked")
	@Override
	public T execute() throws GridExecutionException {
		try {
			Method m = adaptee.getClass().getMethod(executeMethod);
			return (T) m.invoke(adaptee);
		} catch (Exception e) {
			throw new GridExecutionException(e);
		}
	}
	
}
