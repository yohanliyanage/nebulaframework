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

package org.nebulaframework.grid.cluster.node;

import java.util.UUID;


/**
 * Holds information regarding a node of Grid.
 * 
 * @author Yohan Liyanage
 * 
 */
public interface GridNodeProfile {

	/**
	 * Returns the GridNodeID for this
	 * Node.
	 * 
	 * @return UUID NodeId
	 */
	public UUID getId();
	
	/**
	 * Returns the Cluster ID for this
	 * Node.
	 * 
	 * @return UUID Cluster ID
	 */
	public UUID getClusterId();
	
	/**
	 * Returns user-friendly name for Node
	 * 
	 * @return String name
	 */
	public String getName();

	/**
	 * Returns Processor Information
	 * 
	 * @return String Processor Information
	 */
	public String getArhitecture();

	/**
	 * Returns the Operating System Name
	 * 
	 * @return OS Name
	 */
	public String getOSName();

	/**
	 * Returns the IP Address of Node
	 * 
	 * @return IP Address
	 */
	public String getIpAddress();
	
	/**
	 * Returns the version of Java
	 * @return Java version
	 */
	public String getJavaVersion();
	
	/**
	 * Returns the name of Java Vendor
	 * @return Java Vendor Name
	 */ 
	public String getJavaVendor();
}