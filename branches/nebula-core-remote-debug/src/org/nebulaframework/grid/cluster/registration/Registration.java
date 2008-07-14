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

import java.io.Serializable;
import java.util.UUID;

/**
 * {@code Registration} holds information regarding
 * a Cluster registration of a {@code GridNode}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 *
 */
public interface Registration extends Serializable {

	/**
	 * Returns the assigned UUID for the registered Node
	 * @return UUID id
	 */
	public abstract UUID getNodeId();

	/**
	 * Returns the Cluster Manager UUID 
	 * @return UUID Cluster Manager Id
	 */
	public abstract UUID getClusterId();
	
	/**
	 * Returns the ClusterManager Broker URL
	 * @return String BrokerURL
	 */
	public abstract String getBrokerUrl();


}