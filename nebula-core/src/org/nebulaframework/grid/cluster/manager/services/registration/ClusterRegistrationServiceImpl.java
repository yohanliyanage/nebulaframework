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
package org.nebulaframework.grid.cluster.manager.services.registration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.deployment.classloading.node.exporter.GridNodeClassExporterSupport;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.node.GridNodeProfile;
import org.nebulaframework.grid.cluster.node.delegate.GridNodeDelegate;
import org.nebulaframework.grid.cluster.registration.Registration;
import org.nebulaframework.grid.cluster.registration.RegistrationImpl;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of {@link ClusterRegistrationService}, which is responsible
 * for handling {@code GridNode} registration with in the cluster.
 * <p>
 * This class will be remote enabled through Spring JMS Remoting support, so
 * that it could be accessed by remote {@code GridNode}s.
 * <p>
 * This class holds references to the registered {@code GridNode}s, through a 
 * special delegate instance dedicated for each node, implemented by
 * {@code GridNodeDelegate}.
 * <p>
 * <i>Spring Managed</i>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClusterRegistrationService
 * @see GridNodeDelegate
 */
public class ClusterRegistrationServiceImpl implements
		ClusterRegistrationService, InternalClusterRegistrationService {

	private static Log log = LogFactory
			.getLog(ClusterRegistrationServiceImpl.class);

	private ClusterManager cluster;					// Owner Cluster
	private ConnectionFactory connectionFactory;	// JMS Connection Factory

	// Holds delegate references of GridNodes registered, against Node Id
	private Map<UUID, GridNodeDelegate> clusterNodes = new HashMap<UUID, GridNodeDelegate>();

	/**
	 * Constructs a {@code ClusterRegistrationServiceImpl} for 
	 * the given {@code ClusterManager}.
	 * 
	 * @param cluster {@code ClusterManager}
	 */
	public ClusterRegistrationServiceImpl(ClusterManager cluster) {
		super();
		this.cluster = cluster;
	}

	/**
	 * Sets the JMS ConnectionFactory used by this class to communicate
	 * with remote {@code GridNode}s.
	 * 
	 * @param connectionFactory JMS {@code ConnectionFactory}
	 */
	@Required
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	public Registration registerNode(final UUID nodeId, GridNodeProfile profile) {

		// Set the Registration Data
		RegistrationImpl reg = new RegistrationImpl();
		reg.setBrokerUrl(this.cluster.getClusterInfo().getServiceUrl());
		reg.setNodeId(nodeId);
		reg.setClusterId(this.cluster.getClusterId());

		GridNodeDelegate delegate = new GridNodeDelegate(nodeId, profile);
		delegate.setClassExporter(GridNodeClassExporterSupport
				.createServiceProxy(nodeId, connectionFactory));
		synchronized (this) {
			this.clusterNodes.put(nodeId, delegate);	
		}

		log.info("Node registered [ID:" + nodeId + "]");

		// Notify Service Event
		ServiceMessage message = new ServiceMessage(nodeId.toString(),ServiceMessageType.NODE_REGISTERED);
		ServiceEventsSupport.getInstance().onServiceMessage(message);

		// Create HeartBeat Failure Hook
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {
			public void onServiceEvent(ServiceMessage msg) {
				synchronized (this) {
					clusterNodes.remove(nodeId);
					
					// Notify Service Event
					ServiceMessage message = new ServiceMessage(nodeId.toString(),ServiceMessageType.NODE_UNREGISTERED);
					ServiceEventsSupport.getInstance().onServiceMessage(message);
				}		
			}
		}, nodeId.toString(), ServiceMessageType.HEARTBEAT_FAILED);
		
		// Start HeartBeat Tracking
		cluster.getHeartBeatService().addNode(nodeId);
		
		// Return Registration Data to Node
		return reg;
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterNode(UUID id) {
		synchronized (this) {
			this.clusterNodes.remove(id);
		}
		ServiceMessage message = new ServiceMessage(id.toString(), ServiceMessageType.NODE_UNREGISTERED);
		cluster.getServiceMessageSender().sendServiceMessage(message);
		log.info("Node unregistered [ID:" + id + "]");
	}

	/**
	 * Returns a reference to the {@code GridNodeDelegate} instance
	 * which represents a given {@code GridNode}, identified by the
	 * {@code nodeId}.
	 * 
	 * @param nodeId {@code UUID} Node Id
	 * 
	 * @return {@code GridNodeDelegate} delegate
	 * 
	 * @throws IllegalArgumentException if no such node exists
	 */
	public GridNodeDelegate getGridNodeDelegate(UUID nodeId) {
		
		if (this.clusterNodes.containsKey(nodeId)) {
			// If node exists, return
			return this.clusterNodes.get(nodeId);
		} else {
			throw new IllegalArgumentException(
					"No such Node registered with Id " + nodeId);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int getNodeCount() {
		return clusterNodes.size();
	}

}
