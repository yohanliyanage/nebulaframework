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

package org.nebulaframework.grid.cluster.node.services.job.execution;

import org.nebulaframework.core.service.message.ServiceMessage;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.nebulaframework.grid.cluster.node.services.message.ServiceMessagesSupport;

/**
 * {@code JobExecutionService} is responsible of joining new jobs,
 * and executing tasks from joined projects, at a {@code GridNode}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see GridNode
 */
public interface JobExecutionService {
	
	/**
	 * Invoked by {@link ServiceMessagesSupport} of {@code GridNode},
	 * when a {@code GridJob} related {@code ServiceMessage} arrives.
	 * <p>
	 * {@code GridJob} related messages are identified by the 
	 * {@code ServiceMessageType}, specifically,
	 * <ul>
	 * 	<li> {@code ServiceMessageType.JOB_START} </li>
	 * 	<li> {@code ServiceMessageType.JOB_END} </li>
	 * 	<li> {@code ServiceMessageType.JOB_CANCEL} </li>
	 * </ul>
	 * 
	 * @param message {@link ServiceMessage} incoming message
	 */
	void onServiceMessage(ServiceMessage message);
}
