package org.nebulaframework.core.job.annotations;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.nebulaframework.core.job.annotations.splitaggregate.Split;
import org.nebulaframework.core.job.annotations.unbounded.TaskGenerator;

/**
 * Abstract Annotation Processor provides common routines for
 * AnnotationProcessors.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public abstract class AbstractAnnotationProcessor implements
		AnnotationProcessor {

	/**
	 * Returns true if the given class implements a given interface either
	 * directly, or through a super type.
	 * 
	 * @param clazz Class to check
	 * @param interfaceClass Interface to check
	 * @return true if implements
	 */
	protected boolean hasInterface(Class<?> clazz, Class<?> interfaceClass) {

		if (clazz == null)
			return false;

		if (clazz.equals(interfaceClass)) {
			return true;
		}

		// Recursively Process All Interfaces
		for (Class<?> c : clazz.getInterfaces()) {
			if (hasInterface(c, interfaceClass)) {
				return true;
			}
		}

		// Recursively Process Super Class
		if (hasInterface(clazz.getSuperclass(), interfaceClass)) {
			return true;
		}

		return false;
	}
	
	/**
	 * Returns the Task Method for a given annotated class, if exists.
	 * 
	 * @param clazz class to process
	 * @return Task method
	 * @throws IllegalArgumentException if class does not contain a task method
	 */
	protected Method taskMethod(Class<?> clazz) throws IllegalArgumentException {
		for (Method m : clazz.getMethods()) {
			if (m.isAnnotationPresent(Task.class)) {
				
				// If Method takes Arguments
				if (m.getParameterTypes().length!=0) {
					throw new IllegalArgumentException("Task annotation detected. But method expects arguments where it should not.");
				}
				
				// If the return type is Serializable
				if (!hasInterface(m.getReturnType(), Serializable.class))  {
					throw new IllegalArgumentException("Task annotation detected. But return type is not of type Serializable");
				}
				return m;
			}
		}
		
		throw new IllegalArgumentException("No Task Annotation Found in type " + clazz.getSimpleName());
	}
	
	/**
	 * Extracts and returns the Class for the GridTask, from another annotation.
	 * 
	 * @param method Method which contains the class information.
	 * @param annotationClass class of the other annotation
	 * @param jobClass class which contains the grid job
	 * @return Class for GridTask
	 */
	protected Class<?> getTaskClass(Method method, Class<? extends Annotation> annotationClass, Class<?> jobClass) {
		
		Class<?> taskClass = null;
		
		if (annotationClass.equals(Split.class)) {
			taskClass = method.getAnnotation(Split.class).task();
		}
		else if (annotationClass.equals(TaskGenerator.class)) {
			taskClass = method.getAnnotation(TaskGenerator.class).task();
		}
		
		// If no taskClass was specified, default to method's class
		if (taskClass.equals(Void.class)) {
			taskClass = jobClass;
		}
		
		return taskClass;
		
	}

}
