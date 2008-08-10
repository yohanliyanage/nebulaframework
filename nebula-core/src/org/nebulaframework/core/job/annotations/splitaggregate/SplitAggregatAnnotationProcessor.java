package org.nebulaframework.core.job.annotations.splitaggregate;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.annotations.AbstractAnnotationProcessor;


// TODO FixDoc
public class SplitAggregatAnnotationProcessor extends AbstractAnnotationProcessor {

	private static final Log log = LogFactory
			.getLog(SplitAggregatAnnotationProcessor.class);
	
	public SplitAggregatAnnotationProcessor() {
		super();
	}

	@Override
	public GridJob<?, ?> adapt(Serializable obj) throws IllegalArgumentException {
		
		log.debug("[SA-Annotations] Adapting " + obj.getClass().getSimpleName());
		
		// Check for SplitAggregateJob Annotation
		if (!isSplitAggregate(obj.getClass())) {
			throw new IllegalArgumentException("SplitAggregateJob annotation not found on type " + obj.getClass().getName());
		}
		
		Method splitMethod = splitMethod(obj.getClass());
		Method aggregateMethod = aggregateMethod(obj.getClass());
		Method taskMethod = taskMethod(getTaskClass(splitMethod, Split.class, obj.getClass()));
		
		return new SplitAggregateJobAdapter<Serializable,Serializable> (obj,splitMethod, aggregateMethod, taskMethod);
	}



	@Override
	public boolean isValid(Serializable obj) {
		
		log.debug("[SA-Annotations] Checking " + obj.getClass().getSimpleName());
		
		try {
			
			if (!isSplitAggregate(obj.getClass())) {
				return false;
			}
			
			splitMethod(obj.getClass());
			aggregateMethod(obj.getClass());
		} catch (IllegalArgumentException e) {
			return false;
		}
		
		return true;
	}

	private boolean isSplitAggregate(Class<? extends Serializable> clazz) {
		return clazz.isAnnotationPresent(SplitAggregateJob.class);
	}
	
	private Method splitMethod(Class<?> clazz) throws IllegalArgumentException {
		
		for (Method m : clazz.getMethods()) {
			
			// If Split Annotation is Present
			if (m.isAnnotationPresent(Split.class)) {
				// If Method takes Arguments
				if (m.getParameterTypes().length!=0) {
					throw new IllegalArgumentException("Split annotation detected. But method expects arguments where it should not.");
				}
				
				// Check Return Type
				if (!hasInterface(m.getReturnType(), List.class))  {
					throw new IllegalArgumentException("Split annotation detected. But method does not return a type of List");
				}
				
				Class<?> taskClass = getTaskClass(m, Split.class, clazz);
				
				// Check Task Class
				try {
					taskMethod(taskClass);
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("Split annotation detected, but Task verification failed",e);
				}
				
				return m;
			}
		}
		
		throw new IllegalArgumentException("No Split Annotation Found in type " + clazz.getSimpleName());
	}
	
	private Method aggregateMethod(Class<?> clazz) throws IllegalArgumentException {
		for (Method m : clazz.getMethods()) {
			
			// Check for Aggregate Annotation
			if (m.isAnnotationPresent(Aggregate.class)) {
				
				// If it doesn't has a single argument of type List
				if (!(m.getParameterTypes().length==1 && hasInterface(m.getParameterTypes()[0],List.class))) {
					throw new IllegalArgumentException("Aggregate annotation detected. But method does not accept an argument of type List <Serializable>.");
				}
				// If the return type is Serializable
				if (!hasInterface(m.getReturnType(), Serializable.class))  {
					throw new IllegalArgumentException("Aggregate annotation detected. But return type is not of type Serializable");
				}

				return m;
			}
		}
		
		throw new IllegalArgumentException("No Aggregate Annotation Found in type " + clazz.getSimpleName());
	}
	
	
	
}
