package org.nebulaframework.core.job.annotations.unbounded;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.annotations.AbstractAnnotationProcessor;

/**
 * The Unbounded Annotation Processor is responsible for
 * handling annotations of Unbounded Jobs.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class UnboundedAnnotationProcessor extends AbstractAnnotationProcessor {

	private static final Log log = LogFactory
			.getLog(UnboundedAnnotationProcessor.class);
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public GridJob<?, ?> adapt(Serializable obj)
			throws IllegalArgumentException {
		
		log.debug("Adapting Unbounded");
		
		if (!isUnbounded(obj.getClass())) {
			throw new IllegalArgumentException("UnboundedJob annotation not found on type " + obj.getClass().getName());
		}
		
		UnboundedJob jobAnnotation = obj.getClass().getAnnotation(UnboundedJob.class);
		
		Method taskGenerator = taskGeneratorMethod(obj.getClass());
		String processResult = null;
		
		try {
			Method m = processResultMethod(obj.getClass());
			processResult = m.getName();
		} catch (IllegalArgumentException e) {
			if (jobAnnotation.postProcess()) {
				// If post processing enabled
				throw e;
			}
		}

		Method taskMethod = taskMethod(getTaskClass(taskGenerator, TaskGenerator.class, obj.getClass()));
		
		// Create Job Adapter
		UnboundedJobAdapter job = new UnboundedJobAdapter(obj, 
		                                                  taskGenerator.getName(), 
		                                                  processResult, 
		                                                  taskMethod.getName());
		
		// Extract settings if specified in original Job
		if (obj.getClass().isAnnotationPresent(UnboundedProcessingSettings.class)) {
			UnboundedProcessingSettings settings = obj.getClass().getAnnotation(UnboundedProcessingSettings.class);
			job.setMaxTasksInQueue(settings.maxTasksInQueue());
			job.setMutuallyExclusiveTasks(settings.mutuallyExclusiveTasks());
			job.setReductionFactor(settings.reductionFactor());
			job.setStopOnNull(settings.stopOnNullTask());
			
		}
		
		return job;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValid(Serializable obj) {
		
		log.debug("Checking Unbounded");
		
		if (!isUnbounded(obj.getClass())) {
			return false;
		}

		UnboundedJob job = obj.getClass().getAnnotation(UnboundedJob.class);
		
		try {
			taskGeneratorMethod(obj.getClass());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		}
		
		try {
			processResultMethod(obj.getClass());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			// If post processing is enabled
			if (job.postProcess()) {
				return false;
			}
		}
		
		
		return true;
	}

	/**
	 * Returns true if the given class contains the UnboundedJob annotation.
	 * 
	 * @param clazz class to test
	 * @return true if has UnboundedJob annotation
	 */
	private boolean isUnbounded(Class<? extends Serializable> clazz) {
		return clazz.isAnnotationPresent(UnboundedJob.class);
	}
	
	/**
	 * Returns a reference to the method which is annotated
	 * with TaskGeneator annotation.
	 * 
	 * @param clazz class to process
	 * @return Method reference to the method with TaskGeneator annotation
	 * @throws IllegalArgumentException if class does not contain proper TaskGeneator method
	 */
	private Method taskGeneratorMethod(Class<?> clazz) throws IllegalArgumentException {

		for (Method m : clazz.getMethods()) {

			// If TaskGenerator Annotation is Present
			if (m.isAnnotationPresent(TaskGenerator.class)) {
				// If Method takes Arguments
				if (m.getParameterTypes().length != 0) {
					throw new IllegalArgumentException(
							"TaskGenerator annotation detected. But method expects arguments where it should not.");
				}

				Class<?> taskClass = getTaskClass(m, TaskGenerator.class, clazz);
				
				// Check Return Type
				if (!hasInterface(m.getReturnType(), taskClass)) {
					throw new IllegalArgumentException(
							"TaskGenerator annotation detected. But method does not return a type of " + taskClass.getName());
				}

				// Check Task Class
				try {
					taskMethod(taskClass);
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException(
							"TaskGenerator annotation detected, but Task verification failed", e);
				}

				return m;
			}
		}

		throw new IllegalArgumentException("No TaskGenerator Annotation Found in type "
				+ clazz.getSimpleName());
	}
	
	/**
	 * Returns a reference to the method which is annotated
	 * with ResultPostProcessor annotation.
	 * 
	 * @param clazz class to process
	 * @return Method reference to the method with ResultPostProcessor annotation
	 * @throws IllegalArgumentException if class does not contain proper ResultPostProcessor method
	 */
	private Method processResultMethod(Class<?> clazz) throws IllegalArgumentException {
		for (Method m : clazz.getMethods()) {

			// If TaskGenerator Annotation is Present
			if (m.isAnnotationPresent(ResultPostProcessor.class)) {
				// If Method takes Arguments
				if (m.getParameterTypes().length != 1 || (!hasInterface(m.getParameterTypes()[0], Serializable.class))) {
					throw new IllegalArgumentException(
							"ResultPostProcessor annotation detected. But arguments are not matching");
				}

				// Check Return Type
				if (!hasInterface(m.getReturnType(), Serializable.class)) {
					throw new IllegalArgumentException(
							"ResultPostProcessor annotation detected. But method does not return a type of Serializable");
				}

				return m;
			}
		}

		throw new IllegalArgumentException("No ResultPostProcessor Annotation Found in type "
				+ clazz.getSimpleName());
	}

}
