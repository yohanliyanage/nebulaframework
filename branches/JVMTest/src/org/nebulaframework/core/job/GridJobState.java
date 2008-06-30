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

/**
 * Represents the various states a Grid Job can exist.
 * @author Yohan Liyanage
 *
 */
public enum GridJobState {
	
	/**
	 * GridJob is in waiting state (to be enqueued). 
	 */
	WAITING,
	
	/**
	 * GridJob is enqueued for execution, but has not started yet.
	 */
	ENQUEUED,
	
	/**
	 * GridJob has been rejected by System.
	 */
	REJECTED,
	
	/**
	 * GridJob is executing on the Grid.
	 */
	EXECUTING,
	
	/**
	 * GridJob has completed successfully.
	 */
	COMPLETE,
	
	/**
	 * GridJob has been canceled.
	 */
	CANCELED,
	
	/**
	 * GridJob has failed during execution.
	 */
	FAILED;
}
