/*
 * Copyright (C) 2008 Yohan Liyanage. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.nebulaframework.core.job.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code UnboundedProcessingSettings} allows to provide processing settings for
 * {@code UnboundedGridJob}s. These settings will be processed and used by the
 * Nebula Framework when a {@code UnboundedGridJob} marked by this annotation is
 * submitted to the Grid.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface UnboundedProcessingSettings {

	/**
	 * Maximum number of tasks in TaskQueue at a given time, without slowing
	 * task generation. Note that this does not define the maximum possible
	 * tasks in queue. However, once this amount is reached, future task 
	 * generation will be gradually slowed down, to avoid overloading the
	 * task queue.
	 * <p>
	 * default value is 100.
	 * 
	 * @return maximum allowed tasks in queue without reduction
	 */
	int maxTasksInQueue() default 100;

	/**
	 * Factor (time) by which the task generation is slowed per task which is
	 * over {@code maxTasksInQueue} (in milliseconds). When the task queue exceeds
	 * {@code maxTasksInQueue}, the next task generation will be delayed by <i>n</i> 
	 * milliseconds where <i>n</i> is  the multiplication of this factor into 
	 * tasks above limit.
	 * <p>
	 * For example, if reductionFactor is 50, {@code maxTasksInQueue} is 100, 
	 * and if current number of tasks in queue are 105, the next task generation 
	 * will be slowed by (105-100) * 50 milliseconds.
	 * <p>
	 * default value is 50.
	 * 
	 * @return reduction factor in milliseconds
	 */
	int reductionFactor() default 50;

	/**
	 * Indicates whether to stop task generation if a null task is returned
	 * after invoking task() method on UnboundedGridJob.
	 * <p>
	 * default value is {@code true}.
	 * @return a boolean indicating whether the job should stop on a null task
	 */
	boolean stopOnNullTask() default true;

	/**
	 * Indicates whether the tasks generated for the current UnboundedGridJob
	 * are mutually exclusive, which can be used to increase performance and
	 * to optimize resource utilization.
	 * <p>
	 * default value is {@code false}.
	 * 
	 * @return a boolean indicating whether the tasks of this job are mutually
	 *         exclusive
	 */
	boolean mutuallyExclusiveTasks() default false;
}
