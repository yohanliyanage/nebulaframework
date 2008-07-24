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

import java.util.UUID;

import org.nebulaframework.grid.cluster.manager.services.facade.ClusterManagerServicesFacade;
import org.nebulaframework.grid.cluster.node.GridNodeProfile;
import org.nebulaframework.grid.cluster.registration.Registration;

/**
 * {@code ClusterRegistrationService} is responsible for handling {@code GridNode}
 * registration with in the cluster. 
 * <p>
 * Each {@code GridNode} which requires to be registered as a node of the 
 * cluster will invoke the {@code registerNode} method of the implementation 
 * of this service, and each node which requires to leave the cluster will invoke 
 * the {@code unregisterNode} method.
 * <p>
 * This service is to be exposed directly as a Remote Service, which can be accessed by
 * remote {@code GridNode}s. This is <b>not</b> exposed as a part of the 
 * {@link ClusterManagerServicesFacade}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public interface ClusterRegistrationService {

	/**
	 * Registers a {@code GridNode} with given {@code nodeId} 
	 * in {@code ClusterManager}
	 * 
	 * @param id {@code UUID} Identifier of Node
	 * @param nodeProfile Grid Node Profile
	 * 
	 * @return {@code Registration} Registration Details
	 */
	public Registration registerNode(UUID id, GridNodeProfile nodeProfile);
	
	/**
	 * Unregisters a {@code GridNode} with given {@code nodeId} 
	 * from {@code ClusterManager}
	 * 
	 * @param id {@code UUID} Identifier of Node
	 */
	public void unregisterNode(UUID id);
	
}
