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

import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.grid.cluster.manager.services.registration.ClusterRegistrationService;
import org.nebulaframework.core.grid.cluster.registration.Registration;
import org.nebulaframework.core.grid.cluster.registration.RegistrationException;

/**
 * {@code NodeRegistrationService} is responsible for registering the owner 
 * {@code GridNode} with in a designated cluster, through {@code ClusterManager}'s 
 * {@code ClusterRegistrationService}.
 *  
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see GridNode
 * @see ClusterManager
 * @see ClusterRegistrationService
 */
public interface NodeRegistrationService {

	/**
	 * Returns the {@code Registration} instance with cluster 
	 * registration information.
	 * 
	 * @return Registration information 
	 */
	public Registration getRegistration();

	/**
	 * Returns {@code true} if this node is registered in a 
	 * cluster.
	 * 
	 * @return if registered, {@code true}, otherwise {@code false}.
	 */
	public boolean isRegistered();
	
	/**
	 * Registers this {@code GridNode} in the current cluster,
	 * as specified in the {@code Broker URL}.
	 * <p>
	 * Registration is done through {@code ClusterRegistrationService} proxy.
	 * 
	 * @throws RegistrationException if registration fails
	 * @throws IllegalStateException if already registered
	 */
	public void register() throws RegistrationException, IllegalStateException;

	/**
	 * Unregisters the {@code GridNode} from currently registered
	 * cluster. 
	 * 
	 * @throws IllegalStateException if not registered yet
	 */
	public void unregister() throws IllegalStateException;

}