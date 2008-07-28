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

package org.nebulaframework.core.job;

import java.io.Serializable;

import org.nebulaframework.core.job.splitaggregate.SplitAggregateGridJob;
import org.nebulaframework.core.job.unbounded.UnboundedGridJob;


/**
 * {@code GridJob} marker interface denotes  that any sub-interface of this to be
 * a GridJob which can be executed on Nebula Grid.
 * <p>
 * {@code GridJob} interface does not mandate the programming model to be used
 * by the users. For this, refer to sub-interfaces of this interface, which are
 * listed below.
 * <p>
 * <ul>
 * 	<li> {@link SplitAggregateGridJob} </li>
 * 	<li> {@link UnboundedGridJob} </li>
 * </ul>
 * 
 * @param <T> Type of intermediate results of {@code GridTask}s
 * @param <R> Type of Final Result of the {@code GridJob}
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see SplitAggregateGridJob
 * @see UnboundedGridJob
 */
public interface GridJob<T extends Serializable, R extends Serializable> extends Serializable {
	// Marker Interface
}
