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
package org.nebulaframework.grid.cluster.manager;

import java.util.UUID;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.service.message.ServiceMessage;
import org.nebulaframework.core.service.message.ServiceMessageType;
import org.nebulaframework.deployment.classloading.service.ClassLoadingServiceSupport;
import org.nebulaframework.grid.ID;
import org.nebulaframework.grid.cluster.manager.services.facade.ClusterManagerServicesFacade;
import org.nebulaframework.grid.cluster.manager.services.jobs.ClusterJobService;
import org.nebulaframework.grid.cluster.manager.services.jobs.InternalClusterJobService;
import org.nebulaframework.grid.cluster.manager.services.jobs.remote.InternalRemoteClusterJobService;
import org.nebulaframework.grid.cluster.manager.services.jobs.remote.RemoteClusterJobServiceImpl;
import org.nebulaframework.grid.cluster.manager.services.messaging.ServiceMessageSender;
import org.nebulaframework.grid.cluster.manager.services.registration.ClusterRegistrationService;
import org.nebulaframework.grid.cluster.manager.services.registration.InternalClusterRegistrationService;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

/**
 * {@code ClusterManager} manages a Cluster of {@code GridNode}s in Nebula Framework. 
 * This is key server-side class in Nebula Framework, which provides server-side
 * facilities with in the cluster.
 * <p>
 * Each core functionality provided by the {@code ClusterManager} is provided through
 * a service, and the services are as follows:
 * 	<ul>
 * 		<li> {@link ClusterRegistrationService} </li>
 * 		<li> {@link ClusterJobService} </li>
 * 		<li> {@link ServiceMessageSender} </li>
 * 	</ul>
 * <p>{@code ClusterRegistrationService} is exposed directly as a remote service and
 * {@code ClusterJobService} is exposed through {@link ClusterManagerServicesFacade},
 * which will also be used for other services in the future implementation.
 * <p>
 * <b>Note : </b> This class is managed by Spring Container. If it is required to use 
 * this outside Spring Container, please ensure that the 
 * {@link ClusterManager#afterPropertiesSet()} method is invoked after setting 
 * dependencies of the class. If deployed with in Spring Container, this will be 
 * automatically invoked by Spring itself.
 * <p>
 * <i>Spring Managed</i>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * @see GridNode
 * @see ClusterJobService
 * @see ClusterRegistrationService
 */
public class ClusterManager implements InitializingBean {

	private static Log log = LogFactory.getLog(ClusterManager.class);
	
	private UUID clusterId;
	private String brokerUrl;
	private ConnectionFactory connectionFactory;
	private ServiceMessageSender serviceMessageSender;
	private InternalClusterRegistrationService clusterRegistrationService;
	private InternalClusterJobService jobService;
	private InternalRemoteClusterJobService remoteJobService;
	
	/**
	 * Instantiates ClusterManager, and assigns it a unique identifier.
	 * The identifier is obtained using {@code ID} class. For details 
	 * about algorithm for generating, please refer to {@link ID#getId()} 
	 * method.
	 */
	public ClusterManager() {
		super();
		this.clusterId = ID.getId();
	}

	/**
	 * Returns the ID for the cluster managed by this ClusterManager instance.
	 * @return {@code UUID} Cluster ID
	 */
	public UUID getClusterId() {
		return clusterId;
	}

	/**
	 * Returns the JMS Broker URL used by this cluster.
	 * <p>
	 * This Broker URL is used by nodes with in this cluster to communicate with 
	 * the ClusterManager, and to access its services.
	 * 
	 * @return {@code String} Broker URL
	 */
	public String getBrokerUrl() {
		return brokerUrl;
	}

	/**
	 * Sets the Broker URL used by Cluster. This Broker URL should point to a JMS Broker, 
	 * which is to be used by the {@code ClusterManager} to communicate with its nodes.
	 * <p>
	 * In default implementation, this URL refers to an embedded JMS Broker managed by the 
	 * Spring container.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param brokerUrl Broker URL
	 */
	@Required
	public void setBrokerUrl(String brokerUrl) {
		this.brokerUrl = brokerUrl;
	}

	/**
	 * Returns the {@code ServiceMessageSender} used by this Cluster. 
	 * <p>
	 * The {@code ServiceMessageSender} allows the {@code ClusterManager} to send 
	 * messages to {@code GridNode}s managed by it.
	 * 
	 * @return {@link ServiceMessageSender} of this Cluster
	 * 
	 * @see ServiceMessageSender
	 */
	public ServiceMessageSender getServiceMessageSender() {
		return serviceMessageSender;
	}

	/**
	 * Sets the {@code ServiceMessageSender} used by this Cluster.
	 * <p>The {@code ServiceMessageSender} allows the ClusterManager to 
	 * send messages to {@code GridNode}s managed by it.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param serviceMessageSender {@link ServiceMessageSender} for this Cluster
	 */
	@Required
	public void setServiceMessageSender(
			ServiceMessageSender serviceMessageSender) {
		this.serviceMessageSender = serviceMessageSender;
	}

	/**
	 * Returns a reference to the {@code ClusterRegistrationService} of this Cluster. 
	 * <p>
	 * The {@code ClusterRegistrationService} is responsible for allowing {@code GridNode}s to
	 * be registered in this Cluster.
	 * 
	 * @return {@code ClusterRegistrationService} of this Cluster
	 */
	public InternalClusterRegistrationService getClusterRegistrationService() {
		return clusterRegistrationService;
	}

	/**
	 * Sets the {@code ClusterRegistrationService} for this Cluster.
	 * <p>
	 * The {@code ClusterRegistrationService} is responsible for allowing {@code GridNode}s to
	 * be registered in this Cluster.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 *  
	 * @param clusterRegistrationService {@code ClusterRegistrationService} for this Cluster
	 */
	@Required
	public void setClusterRegistrationService(
			InternalClusterRegistrationService clusterRegistrationService) {
		this.clusterRegistrationService = clusterRegistrationService;
	}

	/**
	 * Returns the {@code ClusterJobService} of this Cluster.
	 * <p>
	 * The {@code ClusterJobService} is responsible for allowing {@code GridNode}s 
	 * to submit jobs, and also to register as workers for submitted jobs.
	 * 
	 * @return {@code ClusterJobService} of this Cluster
	 */
	public InternalClusterJobService getJobService() {
		return jobService;
	}

	/**
	 * Returns the {@code RemoteClusterJobService} of this Cluster.
	 * <p>
	 * The {@code RemoteClusterJobService} is responsible for allowing {@code GridNode}s 
	 * from remote {@code Cluster}s to participate in {@code GridJob}s of this {@code Cluster}.
	 * 
	 * @return {@code RemoteClusterJobService} of this Cluster
	 */
	public InternalRemoteClusterJobService getRemoteJobService() {
		return remoteJobService;
	}

	/**
	 * Sets the {@code ClusterJobService} for this Cluster.
	 * <p>
	 * The {@code ClusterJobService} is responsible for allowing {@code GridNode}s 
	 * to submit jobs and also to register as workers for submitted jobs.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 *  
	 * @param jobService {@code ClusterJobService} for this Cluster
	 */
	@Required
	public void setJobService(InternalClusterJobService jobService) {
		this.jobService = jobService;
	}

	/**
	 * Sets the JMS {@code ConnectionFactory} used by the {@code ClusterManager} to 
	 * communicate with the JMS Broker of the cluster.
	 * <p>
	 * In default implementation, this <tt>ConnectionFactory} is managed by Spring Container.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param connectionFactory JMS ConnectionFactory for <tt>ClusterManager}
	 */
	@Required
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	/**
	 * This method ensures that all dependencies of the {@code ClusterManager} is set. 
	 * Also, this method starts {@code ClassLoadingService} for the Cluster.
	 * <p>
	 * <b>Note : </b>In default implementation, this method will be invoked automatically by Spring Container.
	 * If this class is used outside of Spring Container, this method should be invoked explicitly
	 * to initialize the {@code ClusterManager} properly.
	 * <p>
	 * <i>Spring Invoked</i>
	 * 
	 * @throws Exception if dependencies are not set or ClassLoadingService  fails with Exceptions.
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
		
		// Start Remote Cluster Job Service
		remoteJobService = new RemoteClusterJobServiceImpl(this,connectionFactory);
	}
	
	// TODO Fix Doc
	public void shutdown() {
		// Soft Shutdown
		shutdown(false);
	}
	
	// TODO Fix Doc
	public void shutdown(boolean force) {
		
		// TODO Implement Rest
		if (!force) {
			// Soft Shutdown
			// If fails, log end return
		}
		else {
			// Forced Shutdown
			// If fails, log but ignore
		}
		
		ServiceMessage message = new ServiceMessage(this.clusterId.toString(), 
		                                            ServiceMessageType.CLUSTER_SHUTDOWN);
		
		serviceMessageSender.sendServiceMessage(message);
		
		// Other clean up operations
		
		new Thread(new Runnable() {

			public void run() {
				try {
					// Wait 2 secs for messages to go ? // TODO Revise
					Thread.sleep(2000);
					System.exit(0);
				} catch(InterruptedException ex) {
					log.error(ex);
				}
			}
			
		}).start();
	}
}
