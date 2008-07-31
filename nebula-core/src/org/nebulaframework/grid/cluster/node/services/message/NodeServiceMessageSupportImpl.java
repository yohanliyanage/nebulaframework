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

package org.nebulaframework.grid.cluster.node.services.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;

/**
 * Implementation of {@code ServiceMessagesSupport}. Handles
 * {@code ServiceMessage}s sent by the {@code ClusterManager}. The
 * responsibility of this service is to notify relevant services regarding
 * incoming service messages.
 * <p>
 * {@code ServiceMessage}s are communicated through a special JMS {@code Topic},
 * referred to as {@code ServiceTopic}.
 * <p>
 * <i>Spring Managed</i>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ServiceMessagesSupport
 * @see ServiceMessage
 */
public class NodeServiceMessageSupportImpl implements ServiceMessagesSupport {

	private static Log log = LogFactory.getLog(NodeServiceMessageSupportImpl.class);

	private ServiceMessage message; // Last Message


	/**
	 * Constructs a {@code ServiceMessageSupportImpl} instance for given
	 * {@code GridNode}.
	 * 
	 */
	public NodeServiceMessageSupportImpl() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public ServiceMessage getLastMessage() {
		return message;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method is invoked by Spring's {@code MessageListenerContainer},
	 * through {@code MessageListenerAdapter}, when a {@code ServiceMessage}
	 * arrives in the {@code ServiceTopic}.
	 * <p>
	 * <i>Spring Invoked</i>
	 */
	public void onServiceMessage(ServiceMessage message) {

		// Ignore HeartBeat Messages
		if (message.getType()==ServiceMessageType.HEARTBEAT_FAILED) {
			return;
		}
		
		log.debug("[Service] " + message);

		this.message = message;

		GridNode node = GridNode.getInstance();
		

		// Notify Relevant Parties
		if (message.isJobMessage()) {
			// If Job Message Notify Job Service
			if (node.getJobExecutionService()!=null) {
				node.getJobExecutionService().onServiceMessage(message);
			}
			else {
				log.debug("[ServiceMessage]Job Message ignored as no JobExecutionService is registered");
			}
		}
		else if (message.getType()==ServiceMessageType.NODE_BANNED) {
			
			// If Message is a Banned Message for this Node
			
			if (node.getId().toString().equals(message.getMessage().split("#")[0])) {
				
				try {
					// Create a Cancel Message and Send
					ServiceMessage msg = new ServiceMessage(message.getMessage().split("#")[1], ServiceMessageType.JOB_CANCEL);
					node.getJobExecutionService().onServiceMessage(msg);
					
				} catch (Exception e) {
					log.warn("[ServiceMessage] Exception while processing message", e);
				}
				
			}
			else {
				log.debug("Disregarding Node Banned for : " + message.getMessage() + ", Local : " + node.getId());
			}
		}
		
		// Notify Service Event
		ServiceEventsSupport.getInstance().onServiceMessage(message);

	}






}
