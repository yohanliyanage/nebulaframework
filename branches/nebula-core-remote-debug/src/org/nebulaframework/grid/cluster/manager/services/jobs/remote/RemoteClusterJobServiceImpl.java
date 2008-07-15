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

package org.nebulaframework.grid.cluster.manager.services.jobs.remote;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.deploy.GridJobInfo;
import org.nebulaframework.core.job.exceptions.GridJobPermissionDeniedException;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.manager.services.jobs.JMSNamingSupport;
import org.nebulaframework.util.jms.JMSRemotingSupport;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * Implementation of {@code RemoteClusterJobService}, which allows a
 * {@code ClusterManager} to request a {@code GridJob} which is managed by a
 * remote cluster.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see RemoteClusterJobService
 */
public class RemoteClusterJobServiceImpl implements
		InternalRemoteClusterJobService, RemoteClusterJobService {

	private static Log log = LogFactory
			.getLog(RemoteClusterJobServiceImpl.class);

	private ClusterManager cluster;
	private ConnectionFactory connectionFactory;
	private DefaultMessageListenerContainer container;

	/**
	 * Constructs a {@code RemoteClusterJobServiceImpl} for the given
	 * {@code ClusterManager} and initializes the service.
	 * 
	 * @param cluster
	 *            owner {@code ClusterManager}
	 */
	public RemoteClusterJobServiceImpl(ClusterManager cluster) {
		super();
		this.cluster = cluster;
		this.connectionFactory = cluster.getConnectionFactory();
		initialize();
	}

	/**
	 * Initializes the {@code RemoteClusterJobServiceImpl}, by exposing the
	 * service as a remote service through JMS and starting the JMS Message
	 * Listener Container for service.
	 */
	protected void initialize() {
		// Create JMS Remoting Container and Expose service
		String queueName = JMSNamingSupport.getRemoteJobServiceQueueName();

		DefaultMessageListenerContainer container = JMSRemotingSupport
				.createService(connectionFactory, queueName, this,
								RemoteClusterJobService.class);

		// Set a Message Selector to accept only messages for this Cluster
		container.setMessageSelector("targetClusterId = '"
				+ cluster.getClusterId() + "'");

		log.debug("[RemoteJobService] Initialized");
	}

	/**
	 * {@inheritDoc}
	 */
	public GridJobInfo remoteJobRequest(String jobId)
			throws GridJobPermissionDeniedException, IllegalArgumentException {

		log.debug("[RemoteJobService] Received Request for {" + jobId + "}");
		return cluster.getJobService().requestJob(jobId);

	}

	/**
	 * {@inheritDoc}
	 */
	public void shutdown() {
		container.stop();
		container.shutdown();
	}

}
