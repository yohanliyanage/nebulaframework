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
package org.nebulaframework.grid.cluster.manager.services.jobs;

import org.nebulaframework.core.job.GridJob;

/**
 * JobExecutionManager defines the common API for {@code GridJob}
 * executors. All {@code GridJob} executors are required to
 * implement this interface, which exposes necessary methods to
 * manage the life-cycle of a {@code GridJob}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public interface JobExecutionManager {
	
	/**
	 * This method should return the GridJob interface which
	 * determines the GridJob Type Supported by this
	 * {@code JobExecutionManager}.
	 * 
	 * @return Class of GridJob
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends GridJob> getInterface();
	
	/**
	 * Attempts to start the GridJob denoted by the specified
	 * {@code GridJobProfile} using this {@code JobExecutionManager}.
	 * If Success, it returns true.
	 * 
	 * @param profile {@code GridJobProfile} for Job
	 * @return boolean value indicating success / failure
	 */
	public boolean startExecution(GridJobProfile profile);
	

}
