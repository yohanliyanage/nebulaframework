package org.nebulaframework.core.job.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO FixDoc : Way to give custom processing settings for unbounded
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProcessingSettings {
	int maxTasksInQueue() default 100;
	int reductionFactor() default 50;
	boolean stopOnNullTask() default true;
	boolean mutuallyExclusiveTasks() default false;
}
