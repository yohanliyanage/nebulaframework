package org.nebulaframework.core.job.annotations.splitaggregate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker Annotation which denotes a
 * Split Aggregate Grid Job.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SplitAggregateJob {
}
