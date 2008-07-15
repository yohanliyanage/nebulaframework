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

package org.nebulaframework.deployment.classloading.node.exporter;

import java.util.UUID;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.cluster.manager.support.CleanUpSupport;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.nebulaframework.util.jms.JMSRemotingSupport;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * Provides support methods which assists the {@code GridNode} to create and
 * start the {@code GridNodeClassExporter} service.
 * <p>
 * This class provides methods which allows to create and remote enable the
 * local {@code GridNodeClassExporter} service using JMS (Spring JMS API).
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see GridNodeClassExporter
 */
public class GridNodeClassExporterSupport {

	private static Log log = LogFactory
			.getLog(GridNodeClassExporterSupport.class);

	/**
	 * Creates and starts the {@code GridNodeClassExporter} service for the
	 * local {@code GridNode}. This method creates the service itself, and the
	 * necessary JMS resources, along with Spring JMS Remoting services, and
	 * starts the remote service.
	 * 
	 * @throws IllegalArgumentException
	 *             if any argument is {@code null}
	 */
	public static void startService()throws IllegalArgumentException {

		UUID nodeId = GridNode.getInstance().getId();
		ConnectionFactory cf =  GridNode.getInstance().getConnectionFactory();
		String queueName = getExporterQueueName(nodeId);
		
		// Create Service
		GridNodeClassExporterImpl service = new GridNodeClassExporterImpl();
		DefaultMessageListenerContainer container = JMSRemotingSupport.createService(cf, queueName, service, GridNodeClassExporter.class);
		
		// CleanUpHook
		CleanUpSupport.shutdownContainerWhenNodeLeft(nodeId.toString(), container);
		
		log.debug("[GridNodeClassExporter] Service Started");
	}

	/**
	 * Returns the JMS QueueName for the {@code GridNodeClassExporter} for this
	 * {@code GridNode}.
	 * 
	 * @param nodeId
	 *            Node Id of local {@code GridNode}
	 * 
	 * @return JMS QueueName for {@code GridNodeClassExporter}
	 */
	private static String getExporterQueueName(UUID nodeId) {
		return "nebula.node." + nodeId + ".classexport.queue";
	}

	/**
	 * Creates an returns a proxy object which can be used to access the
	 * {@code GridNodeClassExporter} service of a given {@code GridNode}.
	 * <p>
	 * This method relies on Spring JMS Remoting API's proxy support to create
	 * the proxy object.
	 * 
	 * @param nodeId
	 *            Node Id of target {@code GridNode}
	 * @param connectionFactory
	 *            JMS {@code ConnectionFactory}
	 * 
	 * @return The {@code GridNodeClassExporter} Service Proxy
	 */
	public static GridNodeClassExporter createServiceProxy(UUID nodeId, ConnectionFactory cf)
			throws IllegalArgumentException {
		
		// Check for nulls
		String queueName = getExporterQueueName(nodeId);
		
		// Return Proxy Object
		GridNodeClassExporter proxy = JMSRemotingSupport.createProxy(cf, queueName, GridNodeClassExporter.class);
		
		// CleanUp Hook
		CleanUpSupport.removeQueueWhenNodeLeft(nodeId.toString(), queueName);
		
		return proxy;
	}
}
