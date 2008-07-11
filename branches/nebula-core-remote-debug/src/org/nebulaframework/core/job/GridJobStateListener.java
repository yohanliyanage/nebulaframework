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

import org.nebulaframework.core.job.future.GridJobFuture;


/**
 * Allows to track GridJobState changes. Classes which requires to track changes
 * of a JobState should implement this listener interface. To register as a 
 * listener, {@link GridJobFuture#addGridJobStateListener(GridJobStateListener)}
 * should be used.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see GridJobFuture#addGridJobStateListener(GridJobStateListener)
 * @see GridJobFuture#removeGridJobStateListener(GridJobStateListener)
 */
// TODO Remove if not needed
public interface GridJobStateListener {
	
	/**
	 * Invoked when {@code GridJob} state has changed.
	 * 
	 * @param newState updated state
	 */
	public void stateChanged(GridJobState newState);
}
