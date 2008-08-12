package org.nebulaframework.core.job.annotations;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.grid.GridExecutionException;

/**
 * The GridTask Adapter, used to adapt Task implementations
 * in annotation based GridJobs.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 *
 * @param <T> Type of intermediate result
 */
public class GridTaskAdapter<T extends Serializable> implements GridTask<T>{

	private static final long serialVersionUID = -6295188093453101037L;
	private Serializable adaptee;
	private String executeMethod;
	
	
	/**
	 * Constructs a {@link GridTaskAdapter} instance for the given
	 * adaptee and the method name.
	 * 
	 * @param adaptee the object which contains execute method
	 * @param executeMethod name of the execute method
	 */
	public GridTaskAdapter(Serializable adaptee, String executeMethod) {
		super();
		this.adaptee = adaptee;
		this.executeMethod = executeMethod;
	}


	/**
	 * This method delegates to the given execute method
	 * of the adaptee, and returns any value returned
	 * by that method.
	 */
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
