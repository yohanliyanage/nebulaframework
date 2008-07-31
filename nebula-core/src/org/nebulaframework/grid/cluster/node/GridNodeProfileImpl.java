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

import java.io.Serializable;
import java.util.UUID;

/**
 * GridNodeInfo Implementation. Holds information regarding a node of Grid.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class GridNodeProfileImpl implements Serializable, GridNodeProfile {

	private static final long serialVersionUID = -7303863519997272578L;
	
	private UUID id;				// Node Id
	private UUID clusterId;			// Cluster Id
	private String ipAddress;		// IP Address
	private String name;			// System Name
	private String architecture;	// System Architecture
	private String OS;				// Operating System
	private String javaVersion;		// Java Version
	private String javaVendor;		// Java Vendor
	
	/**
	 * Constructor.
	 * 
	 */
	public GridNodeProfileImpl() {
		super();
	}

	
	/**
	 * Returns the GridNodeID for this
	 * Node.
	 * 
	 * @return UUID NodeId
	 */
	public UUID getId() {
		return id;
	}


	/**
	 * Sets the GridNodeID for this
	 * Node.
	 * 
	 * @param id Node ID
	 */
	public void setId(UUID id) {
		this.id = id;
	}

	// TODO Uncomment in interface
	/**
	 * Returns the ClusterID for this
	 * {@code GridNode}.
	 * 
	 * @return UUID ClusterID
	 */
	public UUID getClusterId() {
		return clusterId;
	}

	/**
	 * 	Sets the ClusterID for this
	 * {@code GridNode}.
	 * 
	 * @param clusterId ClusterID
	 */
	public void setClusterId(UUID clusterId) {
		this.clusterId = clusterId;
	}


	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets user-friendly name for Node
	 * 
	 * @param name
	 *            Name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getArhitecture() {
		return architecture;
	}

	/**
	 * Sets Processor Information String
	 * 
	 * @param processor
	 *            Processor Info
	 */
	public void setArchitecture(String archiecture) {
		this.architecture = archiecture;
	}



	/**
	 * {@inheritDoc}
	 */
	public String getOSName() {
		return OS;
	}

	/**
	 * Sets the Operating System Name
	 * 
	 * @param os
	 *            OS Name
	 */
	public void setOSName(String os) {
		OS = os;
	}

	/**
	 * {@inheritDoc}.
	 * <p>
	 * {@code String} representation format is as follows:
	 * <code>
	 * <i>Name</i>|<i>IPAddress</i>|<i>OS</i>|<i>Architecture</i>
	 * </code> 
	 */
	@Override
	public String toString() {
		return name + "|" + ipAddress + "|" + OS + "|" + architecture  ;
	}

	/**
	 * Returns the IP Address of this {@code GridNode}.
	 * 
	 * @return {@code String} IP Address
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * Sets the IP Address of this {@code GridNode}.
	 * 
	 * @param ipAddress IP Address
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}


	/**
	 * {@inheritDoc}
	 */
	public String getJavaVersion() {
		return javaVersion;
	}

	/**
	 * Sets the Java Version of this Node
	 * @param javaVersion Java Version
	 */
	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getJavaVendor() {
		return javaVendor;
	}

	/**
	 * Sets the vendor of Java
	 * @param vendor name
	 */
	public void setJavaVendor(String vendor) {
		javaVendor = vendor;
	}

}
