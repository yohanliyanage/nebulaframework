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
package org.nebulaframework.core.grid.cluster.manager.services.registration;

import java.util.UUID;

import org.nebulaframework.core.grid.cluster.registration.Registration;

/**
 * ClusterManager API Interface. A ClusterManager manages a 
 * local cluster and supports node addition and removal, 
 * and management of local cluster.
 * 
 * @author Yohan Liyanage
 *
 */
public interface NodeRegistrationService {

	/**
	 * Registers a GridNode with ClusterManager
	 * @param id Identifier of Node
	 * @return Registration Details
	 */
	public Registration registerNode(UUID id);
	
	/**
	 * Unregisters a GridNode with ClusterManager
	 * @param id Identifier of Node
	 */
	public void unregisterNode(UUID id);
	
}
