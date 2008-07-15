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

import org.nebulaframework.core.job.future.GridJobFuture;
import org.nebulaframework.core.task.GridTask;

/**
 * {@code ResultCallback} allows to specify a code segment to be
 * invoked when a result of a {@link GridTask} or the final
 * result of a {@link GridJob} is available.
 * <p>
 * At the time of a GridJob submission, it is possible to
 * provide a {@link ResultCallback}, which will be invoked for
 * each result of {@link GridTask}s of the given {@code GridJob}.
 * This use is referred to as <b>Intermediate Result Callback</b>.
 * This can be utilized to retrieve the results as they are collected,
 * for requirements of the client application, for example, updating
 * the UI.
 * <p>
 * Alternatively, it is possible to attach a {@link ResultCallback} to
 * a {@code GridJob}, to be invoked when the final result of the {@code GridJob}
 * is available, using the {@link GridJobFuture#addFinalResultCallback(ResultCallback)}
 * method. This use is referred to as <b>Final Result Callback</b>.
 * This is useful as the {@link GridJobFuture#getResult()} and related 
 * methods block execution. If a non-blocking result notification is required,
 * consider using a Final ResultCallback.
 *  
 * @author Yohan Liyanage
 * @version 1.0
 */
public interface ResultCallback {
	
	/**
	 * Invoked when a result is available
	 * 
	 * @param result result
	 */
	void onResult(Serializable result);
}
