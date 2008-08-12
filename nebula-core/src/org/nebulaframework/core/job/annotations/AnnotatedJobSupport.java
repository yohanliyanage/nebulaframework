package org.nebulaframework.core.job.annotations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.nebulaframework.core.job.GridJob;
import org.springframework.util.Assert;

/**
 * Provides support methods which assists in managing GridJobs which
 * are annotated using framework annotations, instead of interface
 * implementation.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class AnnotatedJobSupport {

	// Singleton Instance
	private static final AnnotatedJobSupport instance = new AnnotatedJobSupport();
	
	private List<AnnotationProcessor> annotationProcessors = new ArrayList<AnnotationProcessor>();
	
	private AnnotatedJobSupport() {
		// No-external-instantiation
	}

	/**
	 * Returns the Singleton instance of AnnotatedJobSupport Class.
	 * 
	 * @return instance
	 */
	public static AnnotatedJobSupport getInstance() {
		return instance;
	}

	/**
	 * Sets the {@link AnnotationProcessor}s supported
	 * by the framework.
	 * 
	 * @param annotationProcessors
	 */
	public void setAnnotationProcessors(
			List<AnnotationProcessor> annotationProcessors) {
		this.annotationProcessors = annotationProcessors;
	}

	/**
	 * Adds an AnntationProcessor to the set of annotation processors
	 * supported by the framework.
	 * 
	 * @param processor Annotation Processor
	 */
	public void addAnnotationProcessor(AnnotationProcessor processor) {
		annotationProcessors.add(processor);
	}
	
	/**
	 * Attempts to adapt a given object as a GridJob, if possible. This
	 * method invokes the {@code isValid} method on each registered
	 * AnnotationProcessor to check whether the object is a GridJob.
	 * If found, the job will be handed over to the subjective annotation
	 * processor to process.
	 * 
	 * @param obj GridJob instance for given annotated job
	 * @return GridJob
	 * @throws IllegalArgumentException if the given object is not a supported
	 * job type.
	 */
	private GridJob<?,?> adapt(Serializable obj) throws IllegalArgumentException {

		Assert.notNull(obj);
		
		// Check with registered processors
		for (AnnotationProcessor processor : annotationProcessors) {
			if (processor.isValid(obj)) {
				return processor.adapt(obj);
			}
		}
		
		throw new IllegalArgumentException("Unable to detect Grid Job");
	}
	
	/**
	 * Attempts to adapt a given annotated grid job as a GridJob instance.
	 * 
	 * @param obj annotated grid job
	 * @return GridJob instance
	 * @throws IllegalArgumentException  the given object is not a supported
	 * job type.
	 */
	public static GridJob<?,?> adaptAsGridJob(Serializable obj) throws IllegalArgumentException{
		return instance.adapt(obj);
	}
}
