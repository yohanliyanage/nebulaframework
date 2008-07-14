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

package org.nebulaframework.grid.cluster.registration;

import java.util.UUID;

/**
 * Implementation of {@code Registration}. Holds information 
 * regarding a Cluster registration of a {@code GridNode}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see Registration
 */
public class RegistrationImpl implements Registration {

	private static final long serialVersionUID = -3286278119947135119L;
	
	private UUID nodeId;
	private UUID clusterId;
	private String brokerUrl;

	/**
	 * {@inheritDoc}
	 */
	public UUID getNodeId() {
		return nodeId;
	}

	/**
	 * Sets the UUID for Node
	 * @param uuid Node Id
	 */
	public void setNodeId(UUID uuid) {
		this.nodeId = uuid;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public UUID getClusterId() {
		return clusterId;
	}

	/**
	 * Sets the ClusterManager UUID
	 * @param clusterId ClusterManager UUID
	 */
	public void setClusterId(UUID clusterId) {
		this.clusterId = clusterId;
	}

	/**
	 * {@inheritDoc}
	 */	
	public String getBrokerUrl() {
		return brokerUrl;
	}

	/**
	 * Sets the Broker URL of ClusterManager
	 * @param brokerUrl Broker URL
	 */
	public void setBrokerUrl(String brokerUrl) {
		this.brokerUrl = brokerUrl;
	}


}
