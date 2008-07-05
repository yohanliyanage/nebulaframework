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
package org.nebulaframework.core.grid.cluster.manager;

import java.util.UUID;

import javax.jms.ConnectionFactory;

import org.nebulaframework.core.grid.cluster.manager.services.jobs.ClusterJobService;
import org.nebulaframework.core.grid.cluster.manager.services.messaging.ServiceMessageSender;
import org.nebulaframework.core.grid.cluster.manager.services.registration.ClusterRegistrationService;
import org.nebulaframework.core.grid.cluster.node.GridNode;
import org.nebulaframework.core.support.ID;
import org.nebulaframework.deployment.classloading.service.ClassLoadingService;
import org.nebulaframework.deployment.classloading.service.support.ClassLoadingServiceSupport;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

/**
 * <p>ClusterManager manages a Cluster of {@link GridNode}s in Nebula Framework. 
 * This is a key class in Nebula Framework, which manages services with in a cluster such as
 * {@link ClusterRegistrationService}, {@link ClusterJobService}, etc.</p>
 * 
 * <p><b>Note :</b> This class is managed by Spring Container. If it is required to use 
 * this outside Spring Container, please ensure that the {@link ClusterManager#afterPropertiesSet()} 
 * method is invoked after setting dependencies of the class. If deployed with in Spring Container, 
 * this will be automatically invoked by Spring itself.</p>
 * 
 * <p><i>Spring Managed</i></p>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class ClusterManager implements InitializingBean {

	private UUID clusterId;
	private String brokerUrl;
	private ConnectionFactory connectionFactory;
	private ServiceMessageSender serviceMessageSender;
	private ClusterRegistrationService clusterRegistrationService;
	private ClusterJobService jobService;
	
	/**
	 * Instantiates ClusterManager, and assigns it a unique identifier, through
	 * {@link ID} class. For details about algorithm for generating, please refer
	 * to {@link ID#getId()} method.
	 * 
	 * @see ID#getId()
	 */
	public ClusterManager() {
		super();
		this.clusterId = ID.getId();
	}

	/**
	 * Returns the ID for the cluster managed by this ClusterManager instance.
	 * @return {@link UUID} Cluster ID
	 */
	public UUID getClusterId() {
		return clusterId;
	}

	/**
	 * <p>Returns the JMS Broker URL used by this cluster.</p>
	 * 
	 * <p>This Broker URL is used by nodes with in this cluster to communicate with the ClusterManager, 
	 * and to access its services.</p>
	 * 
	 * @return String Broker URL
	 */
	public String getBrokerUrl() {
		return brokerUrl;
	}

	/**
	 * <p>Sets the Broker URL used by Cluster. This Broker URL should point to a JMS Broker, 
	 * which is to be used by the ClusterManager to communicate with its nodes.</p>
	 * 
	 * <p>In default implementation, this URL refers to an embedded JMS Broker managed by the 
	 * Spring container.</p>
	 * 
	 * <p><b>Note :</b>This is a <b>required</b> dependency.</p>
	 * 
	 * <p><i>Spring Injected</i></p>
	 * 
	 * @param brokerUrl Broker URL
	 */
	@Required
	public void setBrokerUrl(String brokerUrl) {
		this.brokerUrl = brokerUrl;
	}

	/**
	 * <p>Returns the {@link ServiceMessageSender} used by this Cluster.</p> 
	 * 
	 * <p>The {@link ServiceMessageSender} allows the ClusterManager to send 
	 * messages to {@link GridNode}s managed by it.</p>
	 * 
	 * @return {@link ServiceMessageSender} of this Cluster
	 */
	public ServiceMessageSender getServiceMessageSender() {
		return serviceMessageSender;
	}

	/**
	 * <p>Sets the {@link ServiceMessageSender} used by this Cluster. The {@link ServiceMessageSender}
	 * allows the ClusterManager to send messages to {@link GridNode}s managed by it.</p>
	 * 
	 * <p><b>Note :</b>This is a <b>required</b> dependency.</p>
	 * 
	 * <p><i>Spring Injected</i></p>
	 * 
	 * @param serviceMessageSender {@link ServiceMessageSender} for this Cluster
	 */
	@Required
	public void setServiceMessageSender(
			ServiceMessageSender serviceMessageSender) {
		this.serviceMessageSender = serviceMessageSender;
	}

	/**
	 * <p>Returns a reference to the {@link ClusterRegistrationService} of this Cluster.</p> 
	 * 
	 * <p>The {@link ClusterRegistrationService} is responsible for allowing {@link GridNode}s to
	 * be registered in this Cluster.</p>
	 * 
	 * @return {@link ClusterRegistrationService} of this Cluster
	 */
	public ClusterRegistrationService getClusterRegistrationService() {
		return clusterRegistrationService;
	}

	/**
	 * <p>Sets the {@link ClusterRegistrationService} for this Cluster</p>
	 * 
	 * <p>The {@link ClusterRegistrationService} is responsible for allowing {@link GridNode}s to
	 * be registered in this Cluster.</p>
	 * 
	 * <p><b>Note :</b>This is a <b>required</b> dependency.</p>
	 * 
	 * <p><i>Spring Injected</i></p>
	 *  
	 * @param clusterRegistrationService {@link ClusterRegistrationService} for this Cluster
	 */
	@Required
	public void setClusterRegistrationService(
			ClusterRegistrationService clusterRegistrationService) {
		this.clusterRegistrationService = clusterRegistrationService;
	}

	/**
	 * <p>Returns the {@link ClusterJobService} of this Cluster</p>
	 * 
	 * <p>The {@link ClusterJobService} is responsible for allowing {@link GridNode}s to submit jobs,
	 * and also to register as workers for submitted jobs.</p>
	 * 
	 * @return {@link ClusterJobService} of this Cluster
	 */
	public ClusterJobService getJobService() {
		return jobService;
	}

	/**
	 * <p>Sets the {@link ClusterJobService} for this Cluster</p>
	 * 
	 * <p>The {@link ClusterJobService} is responsible for allowing {@link GridNode}s to submit jobs,
	 * and also to register as workers for submitted jobs.</p>
	 * 
	 * <p><b>Note :</b>This is a <b>required</b> dependency.</p>
	 * 
	 * <p><i>Spring Injected</i></p>
	 *  
	 * @param jobService {@link ClusterJobService} for this Cluster
	 */
	@Required
	public void setJobService(ClusterJobService jobService) {
		this.jobService = jobService;
	}

	/**
	 * <p>Sets the JMS {@link ConnectionFactory} used by the <tt>ClusterManager</tt> to 
	 * communicate with the JMS Broker of the cluster.</p>
	 * 
	 * <p>In default implementation, this <tt>ConnectionFactory</tt> is managed by Spring Container.</p>
	 * 
	 * <p><b>Note :</b>This is a <b>required</b> dependency.</p>
	 * 
	 * <p><i>Spring Injected</i></p>
	 * 
	 * @param connectionFactory JMS ConnectionFactory for <tt>ClusterManager</tt>
	 */
	@Required
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	/**
	 * <p>This method ensures that all dependencies of the {@link ClusterManager} is set. 
	 * Also, this method starts {@link ClassLoadingService} for the Cluster.</p>
	 * 
	 * <p><b>Note :</b>In default implementation, this method will be invoked automatically by Spring Container.
	 * If this class is used outside of Spring Container, this method should be invoked explicitly
	 * to initialize the <tt>ClusterManager</tt> properly.</p>
	 * 
	 * <p><i>Spring Invoked</i></p>
	 * 
	 * @throws Exception if dependencies are not set or ClassLoadingService start fails with Exceptions.
	 */
	public void afterPropertiesSet() throws Exception {
		
		// Assertions, to ensure that the class was properly initialized,
		// if used outside the container.
		Assert.notNull(brokerUrl);
		Assert.notNull(connectionFactory);
		Assert.notNull(jobService);
		Assert.notNull(clusterRegistrationService);
		
		// Start Remote Class Loading Service
		ClassLoadingServiceSupport.startClassLoadingService(this, connectionFactory);
	}
	
}
