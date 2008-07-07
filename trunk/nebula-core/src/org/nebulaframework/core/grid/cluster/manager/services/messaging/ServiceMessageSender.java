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

package org.nebulaframework.core.grid.cluster.manager.services.messaging;

import org.nebulaframework.core.service.message.ServiceMessage;

/**
 * Allows {@code ClusterManager} to send messages to its {@code GridNode}s,
 * using the Service Topic. 
 * <p>
 * The service messages will be delivered to all registered nodes, through
 * a special JMS {@code Topic}, referred to as {@code ServiceTopic}. 
 * <p>
 * Messages will be delivered using a special Data Transfer Object, implemented
 * as {@link ServiceMessage}.
 * 
 * @author Yohan Liyanage
 * @version 1.0

 * @see ServiceMessage
 */
public interface ServiceMessageSender {
	
	/**
	 * Sends the given {@code ServiceMessage} to all {@code GridNode}s
	 * registered with the {@code ClusterManager}.
	 * 
	 * @param message ServiceMessage
	 */
	public void sendServiceMessage(ServiceMessage message);
}
