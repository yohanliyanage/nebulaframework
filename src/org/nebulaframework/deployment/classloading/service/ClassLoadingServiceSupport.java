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

package org.nebulaframework.deployment.classloading.service;

import java.util.UUID;

import javax.jms.ConnectionFactory;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.ClusterJobServiceImpl;
import org.nebulaframework.core.grid.cluster.manager.services.registration.ClusterRegistrationServiceImpl;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.remoting.JmsInvokerProxyFactoryBean;
import org.springframework.jms.remoting.JmsInvokerServiceExporter;
import org.springframework.util.Assert;

/**
 * Provide support functionality for {@code ClassLoadingService},
 * which manages the Spring JMS Remoting API classes, and also
 * JMS Resources.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClassLoadingService
 */
public class ClassLoadingServiceSupport {

	private static Log log = LogFactory.getLog(ClassLoadingServiceSupport.class);
	
	/**
	 * Starts the {@code ClassLoadingService} for the cluster, by
	 * creating and starting the JMS Remoting support classes and the
	 * service implementation itself.
	 * 
	 * @param manager {@code ClusterManager} cluster
	 * @param connectionFactory JMS {@code ConnectionFactory}
	 * 
	 * @throws IllegalArgumentException if any argument is null
	 */
	public static void startClassLoadingService(ClusterManager manager,
			ConnectionFactory connectionFactory) throws IllegalArgumentException {

		// Check for null values
		Assert.notNull(manager);
		Assert.notNull(connectionFactory);
		
		// FIXME This type casting may cause troubles in future versions, if
		// real implementation differs
		// Create Service Implementation Instance
		ClassLoadingServiceImpl service = new ClassLoadingServiceImpl(
				(ClusterJobServiceImpl) manager.getJobService(),
				(ClusterRegistrationServiceImpl) manager.getClusterRegistrationService());

		// Spring JMS Remoting Service Exporter
		JmsInvokerServiceExporter exporter = new JmsInvokerServiceExporter();
		exporter.setService(service);
		exporter.setServiceInterface(ClassLoadingService.class);
		exporter.afterPropertiesSet();

		// ActiveMQ Queue used for communication
		ActiveMQQueue queue = new ActiveMQQueue(getQueueName(manager
				.getClusterId()));

		// Spring JMS MessageListenerContainer to receive messages
		DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		container.setDestination(queue);
		container.setMessageListener(exporter);
		container.setConnectionFactory(connectionFactory);
		container.afterPropertiesSet();
		
		log.debug("[ClassLoadingService] Started");
	}

	/**
	 * Returns the name of the JMS Queue used for communication
	 * of remote service.
	 * 
	 * @param clusterId ClusterManager ID
	 * 
	 * @return JMS Queue Name
	 */
	private static String getQueueName(UUID clusterId) {
		return "nebula.cluster." + clusterId + ".classloading.queue";
	}

	/**
	 * Creates the Client Proxy for the Remote JMS {@code ClassLoadingService}.
	 * Uses Spring JMS Remoting API.
	 * 
	 * @param clusterId ClusterID
	 * @param connectionFactory JMS {@code ConnectionFactory}
	 * 
	 * @return {@code ClassLoadingService} proxy
	 * 
	 * @throws IllegalArgumentException if any argument is null
	 */
	public static ClassLoadingService createProxy(UUID clusterId,
			ConnectionFactory connectionFactory) throws IllegalArgumentException {
		
		// Check for null values
		Assert.notNull(clusterId);
		Assert.notNull(connectionFactory);
		
		JmsInvokerProxyFactoryBean proxyFactory = new JmsInvokerProxyFactoryBean();
		proxyFactory.setConnectionFactory(connectionFactory);
		proxyFactory.setServiceInterface(ClassLoadingService.class);
		proxyFactory.setQueueName(getQueueName(clusterId));
		proxyFactory.afterPropertiesSet();
		return (ClassLoadingService) proxyFactory.getObject();
	}
}
