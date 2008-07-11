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

package org.nebulaframework.core.grid.cluster.node.services.registration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.grid.cluster.manager.services.registration.ClusterRegistrationService;
import org.nebulaframework.core.grid.cluster.node.GridNode;
import org.nebulaframework.core.grid.cluster.registration.Registration;
import org.nebulaframework.core.grid.cluster.registration.RegistrationException;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of {@code NodeRegistrationService}. Responsible for registering 
 * the owner {@code GridNode} with in a designated cluster, through {@code ClusterManager}'s 
 * {@code ClusterRegistrationService}. 
 * <p>
 * <i>Spring Managed</i>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see NodeRegistrationService
 * @see GridNode
 * @see ClusterManager
 * @see ClusterRegistrationService
 */
public class NodeRegistrationServiceImpl implements NodeRegistrationService {

	private static Log log = LogFactory.getLog(NodeRegistrationServiceImpl.class);
	
	private GridNode node;
	private Registration registration;								
	private ClusterRegistrationService clusterRegistrationService;	// Proxy
	
	/**
	 * Constructs a {@code NodeRegistrationServiceImpl} instance for the
	 * given {@code GridNode}. 
	 * @param node owner {@code GridNode}
	 */
	public NodeRegistrationServiceImpl(GridNode node) {
		super();
		this.node = node;
	}
	
	/**
	 * Sets the {@code ClusterRegistrationService} proxy, which is used by this class
	 * to communicate with the actual implementation in the {@code ClusterManager}.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param regService {@code ClusterRegistrationService} proxy
	 */
	@Required
	public void setClusterRegistrationService(ClusterRegistrationService regService) {
		this.clusterRegistrationService = regService;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Registration getRegistration() {
		return registration;
	}

	/**
	 * {@inheritDoc}
	 */
	public void register() throws RegistrationException, IllegalStateException {
		
		// Check if already registered
		if (registration != null) {
			throw new IllegalStateException("Unable to register. Already registered with a Cluster");
		}
		
		// Do registration
		this.registration = clusterRegistrationService.registerNode(node.getId());

		// Log
		log.debug("Node " + node.getId() + " registered in Cluster "
				+ registration.getClusterId());
		log.debug("Broker URL : " + registration.getBrokerUrl());
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregister() throws IllegalStateException {
		
		// Check if not registered
		if (registration == null) {
			throw new IllegalStateException("Unable to unregister. Not registered with any Cluster");
		}
		
		// Unregister
		this.clusterRegistrationService.unregisterNode(node.getId());
		
		log.info("Node " + node.getId() + " unregistered from Cluster");
	}	
}
