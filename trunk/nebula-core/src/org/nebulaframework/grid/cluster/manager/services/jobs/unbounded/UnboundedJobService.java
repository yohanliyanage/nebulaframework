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
package org.nebulaframework.grid.cluster.manager.services.jobs.unbounded;

import org.nebulaframework.grid.cluster.manager.services.jobs.GridJobProfile;

/**
 * {@code  UnboundedJobService} is responsible for
 * starting and managing execution of {@code UnboundedGridJob}s.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public interface UnboundedJobService {

	/**
	 * Starts execution of the given {@code UnboundedGridJob}
	 * denoted by the {@code GridJobProfile}.
	 * 
	 * @param profile {@code UnboundedGridJob} profile
	 */
	void startJobProcessing(GridJobProfile profile);
}
