package org.nebulaframework.core.job.annotations.unbounded;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker Annotation which denotes the
 * Result Post Processor (processResult)
 * method for an UnboundedGridJob.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResultPostProcessor {
}
