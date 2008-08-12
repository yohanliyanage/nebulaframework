package org.nebulaframework.core.job.annotations.splitaggregate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This marker annotation is used to denote  a method which performs 
 * aggregate operation in a Split-Aggregate Grid Job.
 * <p>
 * Note that any method which uses this annotation is expected to accept 
 * an argument of type {@code List<Serializable>} and return a result
 * of type Serializable.
 *  
 * @author Yohan Liyanage
 * @version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Aggregate {
}

