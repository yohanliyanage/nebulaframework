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
package org.nebulaframework.core.job.future;

import org.nebulaframework.core.job.GridJobStateListener;
import org.nebulaframework.core.job.ResultCallback;
import org.nebulaframework.core.job.SplitAggregateGridJob;
import org.nebulaframework.core.job.UnboundedGridJob;
import org.nebulaframework.grid.cluster.manager.services.jobs.ClusterJobService;

/**
 * Internal interface definition for GridJobFuture, which represents the 
 * result of a deployed {@code GridJob}.
 * <p>
 * Internal interface extends the public interface,
 * but allows to access operations which are not exposed by the public API.
 * <p>
 * <b>Warning : </b>This is to be used by the internal system only, and is not a 
 * part of the public API. Use of this API is strongly discouraged. For the 
 * public API of this service, refer to {@link ClusterJobService}.

 * @author Yohan Liyanage
 * @version 1.0
 */
public interface InternalGridJobFuture extends GridJobFuture {
	
	/**
	 * Creates a {@link ResultCallback} proxy using the specified JMS QueueName,
	 * which will be invoked when the final result of the {@code GridJob} is
	 * available.
	 * 
	 * @param queueName JMS QueueName
	 */
	public void addFinalResultCallback(String queueName);
	
	/**
	 * Adds the given {@code GridJobStateListener} as a listener to the
	 * {@code GridJob} represented by this {@code GridJobFuture}.
	 * 
	 * @param listener
	 *            {@code GridJobStateListener} to add
	 */
	public void addGridJobStateListener(GridJobStateListener listener);

	/**
	 * Removes the given {@code GridJobStateListener} from the collection of
	 * {@code GridJobStateListener}s of this {@code GridJobFuture}.
	 * 
	 * @param listener
	 *            {@code GridJobStateListener} to remove
	 * @return a {@code boolean} value, {@code true} if success, {@code false}
	 *         otherwise.
	 */
	public boolean removeGridJobStateListener(GridJobStateListener listener);
	
	/**
	 * Returns {@code true} if this {@code GridJob} supports
	 * final result. Currently {@link SplitAggregateGridJob}
	 * supports final result, where as {@link UnboundedGridJob}
	 * does not.
	 * 
	 * @return value @code true} final result supported
	 */
	public boolean isFinalResultSupported();
}
