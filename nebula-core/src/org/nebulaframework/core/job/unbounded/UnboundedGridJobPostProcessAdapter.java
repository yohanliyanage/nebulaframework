package org.nebulaframework.core.job.unbounded;

import java.io.Serializable;

import org.nebulaframework.core.job.exceptions.InvalidResultException;

/**
 * Adapter for UnboundedGridJob, which provides a default implementation for
 * {@link #processResult(Serializable)} method. This provided implementation
 * directly returns the result without any transformation, which may be useful
 * for implementing jobs which does not require post-processing.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * @param <T> Type of Task Result
 * 
 * @see UnboundedGridJob
 */
public abstract class UnboundedGridJobPostProcessAdapter<T extends Serializable> implements UnboundedGridJob<T>{

	/**
	 * This implementation directly returns the result without
	 * post-processing it.
	 * 
	 * @param result result received
	 * @return result received
	 */
	@Override
	public Serializable processResult(Serializable result)
			throws InvalidResultException {
		return result;
	}

}
