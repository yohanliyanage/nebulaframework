package org.nebulaframework.core.job.annotations;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.nebulaframework.core.job.annotations.splitaggregate.Split;
import org.nebulaframework.core.job.annotations.unbounded.TaskGenerator;

// TODO FixDOc
public abstract class AbstractAnnotationProcessor implements
		AnnotationProcessor {

	protected boolean hasInterface(Class<?> clazz, Class<?> interfaceClass) {

		if (clazz == null)
			return false;

		if (clazz.equals(interfaceClass)) {
			return true;
		}

		for (Class<?> c : clazz.getInterfaces()) {
			if (hasInterface(c, interfaceClass)) {
				return true;
			}
		}

		if (hasInterface(clazz.getSuperclass(), interfaceClass)) {
			return true;
		}

		return false;
	}
	
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
