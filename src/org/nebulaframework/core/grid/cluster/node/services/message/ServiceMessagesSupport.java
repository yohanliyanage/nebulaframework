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

package org.nebulaframework.core.grid.cluster.node.services.message;

import org.nebulaframework.core.service.message.ServiceMessage;

/**
 * Service to handle {@code ServiceMessage}s sent by the {@code ClusterManager}.
 * The responsibility of this service is to notify relevant services regarding
 * incoming service messages.
 * <p>
 * {@code ServiceMessage}s are communicated through a special JMS {@code Topic}, 
 * referred to as {@code ServiceTopic}. 
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ServiceMessage
 */
public interface ServiceMessagesSupport {
	
	/**
	 * Invoked when a new {@link ServiceMessage} arrives at
	 * the {@code ServiceTopic}.
	 * 
	 * @param message {@code ServiceMessage} incoming
	 */
	public void onServiceMessage(ServiceMessage message);
	
	/**
	 * Returns the last {@code ServiceMessage} received by
	 * this class, or {@code null} if not available.
	 * 
	 * @return Last {@code ServiceMessage} or {@code null}
	 */
	public ServiceMessage getLastMessage();
	
	public void addServiceHook(ServiceEvent event, HookCallback callback);
}
