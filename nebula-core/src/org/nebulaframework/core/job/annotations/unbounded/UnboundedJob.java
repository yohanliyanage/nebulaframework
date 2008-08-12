package org.nebulaframework.core.job.annotations.unbounded;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker Interface which denotes an
 * UnboundedGridJob.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UnboundedJob {
	
	/**
	 * Boolean indicating whether this GridJob
	 * requires post processing or not.
	 * @return
	 */
	boolean postProcess() default true;
}
