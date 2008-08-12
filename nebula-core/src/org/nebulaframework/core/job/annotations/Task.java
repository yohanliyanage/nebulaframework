package org.nebulaframework.core.job.annotations;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to denote a method
 * which contains the code to be executed in a remote
 * node, for a Grid Job. That is, this is the annotation
 * counterpart for GridTask interface.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Task {
	
	/**
	 * The type of return value of the subjective method.
	 * @return Class of the return value type.
	 */
	Class<?> returns() default Serializable.class;
}
