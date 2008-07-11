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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.node.GridNode;
import org.nebulaframework.core.service.message.ServiceMessage;
import org.nebulaframework.core.service.message.ServiceMessageType;

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
public class ServiceMessageSupportImpl implements ServiceMessagesSupport {

	private static Log log = LogFactory.getLog(ServiceMessageSupportImpl.class);

	private GridNode node; // Owner Node
	private ServiceMessage message; // Last Message

	private List<ServiceHookElement> hooks = new ArrayList<ServiceHookElement>();

	/**
	 * Constructs a {@code ServiceMessageSupportImpl} instance for given
	 * {@code GridNode}.
	 * 
	 * @param node
	 *            Owner {@code GridNode}
	 */
	public ServiceMessageSupportImpl(GridNode node) {
		super();
		this.node = node;
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

		log.debug("[Service] " + message);

		this.message = message;

		// If Job Message
		if (message.isJobMessage()) {
			// Notify Job Service
			node.getJobExecutionService().onServiceMessage(message);
		}
		if (message.isClusterMessage()) {
			processClusterMessage(message);
		}

		// TODO Write the rest of code to manage message and notify relevant
		// parties

		notifyHooks(message);

	}

	// TODO FixDoc
	private void notifyHooks(final ServiceMessage message) {
		new Thread(new Runnable() {
			public void run() {
				for (ServiceHookElement hook : hooks) {
					if (hook.getEvent().getType()== message.getType()) {
						if (hook.getEvent().getMessage()!=null) {
							if (hook.getEvent().getMessage().equals(message.getMessage())) {
								hook.getCallback().onServiceEvent();
							}
						}
						else {
							hook.getCallback().onServiceEvent();
						}
					}
				}
			}
		}).start();
	}

	// TODO Fix Doc
	protected void processClusterMessage(ServiceMessage message) {
		String clusterId = message.getMessage();

		// Process only messages from own Cluster
		if (!node.getClusterId().equals(UUID.fromString(clusterId))) {
			return;
		}

		// Cluster Shutdown, Shutdown Node
		if (message.getType() == ServiceMessageType.CLUSTER_SHUTDOWN) {
			// TODO Fail Over ?
			node.shutdown(true, true);
		}
	}

	// TODO FixDoc
	public void addServiceHook(ServiceEvent event, HookCallback callback) {
		// TODO Auto-generated method stub

	}

	// TODO FixDoc
	private class ServiceHookElement {
		private ServiceEvent event;
		private HookCallback callback;

		public ServiceHookElement(ServiceEvent event, HookCallback callback) {
			super();
			this.event = event;
			this.callback = callback;
		}

		public ServiceEvent getEvent() {
			return event;
		}

		public HookCallback getCallback() {
			return callback;
		}

	}

}
