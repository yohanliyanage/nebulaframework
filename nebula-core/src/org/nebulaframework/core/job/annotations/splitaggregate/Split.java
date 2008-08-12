package org.nebulaframework.core.job.annotations.splitaggregate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation denotes the Split operation
 * of Split-Aggregate GridJob.
 * <p>
 * The method which uses this annotation should not
 * accept any argument, and should return a List of objects
 * which are valid GridTasks.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Split {
	
	/**
	 * Identifies the Class of the GridTask class
	 * for this GridJob. This value defaults to
	 * the Void.class, which is used to denote that
	 * the GridTask class is the class where the this
	 * annotation is present.
	 * 
	 * @return GridTask class
	 */
	Class<?> task() default Void.class;
}
