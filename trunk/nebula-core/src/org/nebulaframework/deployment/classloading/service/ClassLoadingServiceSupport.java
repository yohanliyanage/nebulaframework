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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.manager.services.jobs.InternalClusterJobService;
import org.nebulaframework.grid.cluster.manager.services.registration.InternalClusterRegistrationService;
import org.nebulaframework.util.jms.JMSRemotingSupport;

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
	 * @return {@code ClassLoadingService} instance
	 * 
	 * @throws IllegalArgumentException if any argument is null
	 */
	public static ClassLoadingService startClassLoadingService() throws IllegalArgumentException {

		ClusterManager manager = ClusterManager.getInstance();
		
		// Create Service Implementation Instance
		ClassLoadingServiceImpl service = new ClassLoadingServiceImpl(
				(InternalClusterJobService) manager.getJobService(),
				(InternalClusterRegistrationService) manager.getClusterRegistrationService());

		// ActiveMQ Queue used for communication
		String queueName = getQueueName(manager.getClusterId());
		
		ConnectionFactory cf = manager.getConnectionFactory();
		JMSRemotingSupport.createService(cf, queueName, service, ClassLoadingService.class);
		
		log.debug("[ClassLoadingService] Started");
		
		return service;
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
	public static ClassLoadingService createProxy(UUID clusterId, ConnectionFactory cf) throws IllegalArgumentException {
		
		String queueName = getQueueName(clusterId);
		return JMSRemotingSupport.createProxy(cf, queueName, ClassLoadingService.class);
	}
}
