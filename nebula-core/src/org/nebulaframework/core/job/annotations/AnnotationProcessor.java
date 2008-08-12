package org.nebulaframework.core.job.annotations;

import java.io.Serializable;

import org.nebulaframework.core.job.GridJob;

/**
 * Defines the API definition for AnnotationProcessors,
 * which are responsible of handling a given set of
 * annotations which defines the contract for a GridJob type.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public interface AnnotationProcessor {

	/**
	 * Returns true if the given object is a supported
	 * GridJob type of this Annotation Processor.
	 * 
	 * @param obj Object to check
	 * @return true if valid
	 */
	boolean isValid(Serializable obj);
	
	/**
	 * Attempts to adapt the given object as a GridJob,
	 * by processing the annotations present.
	 * 
	 * @param obj Object to adapt
	 * @return GridJob instance
	 * @throws IllegalArgumentException if given object is not a valid grid job
	 */
	GridJob<?,?> adapt(Serializable obj) throws IllegalArgumentException;
}
