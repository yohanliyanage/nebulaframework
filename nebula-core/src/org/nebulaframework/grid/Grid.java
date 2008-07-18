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
package org.nebulaframework.grid;

import java.util.Properties;

import org.nebulaframework.configuration.ConfigurationSupport;
import org.nebulaframework.discovery.ClusterDiscoverySupport;
import org.nebulaframework.discovery.GridNodeDiscoverySupport;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.nebulaframework.util.spring.NebulaApplicationContext;

/**
 * The general access point to Nebula Grid. Allows to configure
 * and start {@link ClusterManager} and {@link GridNode} instances.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class Grid {
	
	// FIXME 61616 is hard coded in Cluster + Node Spring XML
	/** Cluster Service Port (JMS Broker) */
	public static final int SERVICE_PORT = 61616;
	
	/** Cluster Configuration Properties File */
	public static final String CLUSTER_PROPERTY_CONFIGURATION = "nebula-cluster.properties";
	
	/** Cluster XML Properties File */
	public static final String CLUSTER_XML_CONFIGURATION = "nebula-cluster.xml";
	
	/** Grid Node Configuration Properties File */
	public static final String GRIDNODE_PROPERTY_CONFIGURATION = "nebula-client.properties";
	
	/** Grid Node XML Properties File */
	public static final String GRIDNODE_XML_CONFIGURATION = "nebula-client.xml";
	
	/** Cluster Spring Beans Configuration File */
	public static final String CLUSTER_SPRING_CONTEXT = "org/nebulaframework/grid/cluster/manager/cluster-manager.xml";
	
	/** Grid Node Spring Beans Configuration File */
	public static final String GRIDNODE_CONTEXT = "org/nebulaframework/grid/cluster/node/grid-node.xml";
	
	/** Light Weight (Non-worker) Grid Node Spring Beans Configuration File */
	public static final String GRIDNODE_LIGHT_CONTEXT = "org/nebulaframework/grid/cluster/node/grid-nonworker-node.xml";
	
	
	
	private static NebulaApplicationContext applicationContext = null;
	private static boolean clusterManager = false;

	/**
	 * No instantiation
	 */
	private Grid() {
		// No instantiation
	}

	/**
	 * Starts {@link ClusterManager} instance with default settings,
	 * read from default properties file.
	 * 
	 * @return ClusterManager
	 * 
	 * @throws IllegalStateException if a Grid Member (Cluster / Node) has
	 * already started with in the current VM. Nebula supports only one Grid
	 * Member per VM.
	 */
	public static ClusterManager startClusterManager() throws IllegalStateException{

		if (applicationContext != null) {
			// A Grid Member has already started in this VM
			throw new IllegalStateException("A Grid Memeber Already Started in VM");
		}
		
		
		// Detect Configuration
		Properties config = ConfigurationSupport.detectClusterConfiguration();
		
		// Register with any Colombus Servers
		ClusterDiscoverySupport.registerColombus(config);
		
		clusterManager = true;
		applicationContext = new NebulaApplicationContext(CLUSTER_SPRING_CONTEXT, config);
		return (ClusterManager) applicationContext.getBean("clusterManager");
	}


	/**
	 * Starts a {@link GridNode} with default settings, read from
	 * default properties file.
	 * 
	 * @return GridNode
	 * 
	 * @throws IllegalStateException if a Grid Member (Cluster / Node) has
	 * already started with in the current VM. Nebula supports only one Grid
	 * Member per VM.
	 */
	public static GridNode startGridNode() throws IllegalStateException {
		
		if (applicationContext != null) {
			// A Grid Member has already started in this VM
			throw new IllegalStateException("A Grid Memeber Already Started in VM");
		}
		
		// Detect Configuration
		Properties config = ConfigurationSupport.detectNodeConfiguration();
		
		// Discover Cluster If Needed
		GridNodeDiscoverySupport.discover(config);
		
		applicationContext = new NebulaApplicationContext(GRIDNODE_CONTEXT, config);
		clusterManager = false;
		return (GridNode) applicationContext.getBean("localNode");
	}
	
	/**
	 * Starts a Light-weight {@link GridNode} (a GridNode without
	 * Job Execution Support, that is non-worker) with default
	 * settings, read from default properties file.
	 * 
	 * @return GridNode
	 * 
	 * @throws IllegalStateException if a Grid Member (Cluster / Node) has
	 * already started with in the current VM. Nebula supports only one Grid
	 * Member per VM.
	 */
	public static GridNode startLightGridNode() throws IllegalStateException {
		
		if (applicationContext != null) {
			// A Grid Member has already started in this VM
			throw new IllegalStateException("A Grid Memeber Already Started in VM");
		}
		
		Properties config = ConfigurationSupport.detectNodeConfiguration();
		
		// Discover Cluster If Needed
		GridNodeDiscoverySupport.discover(config);
		
		clusterManager = false;
		applicationContext = new NebulaApplicationContext(GRIDNODE_LIGHT_CONTEXT, config);
		return (GridNode) applicationContext.getBean("localNode");
	}

	/**
	 * Returns the {@link NebulaApplicationContext} for this 
	 * Grid Member, if available.
	 * 
	 * @return NebulaApplicationContext
	 * 
	 * @throws IllegalStateException if not initialized (started)
	 */
	public static NebulaApplicationContext getApplicationContext() throws IllegalStateException {
		if (applicationContext==null) throw new IllegalStateException ("Not Initialized");
		return applicationContext;
	}
	
	/**
	 * Returns true if the current node is 
	 * a {@code ClusterManager}.
	 * 
	 * @return true if the current node is 
	 * a {@code ClusterManager}.
	 */
	public static boolean isClusterManager() {
		return clusterManager;
	}
	
	/**
	 * Returns true if the current node is 
	 * a {@code GridNode}.
	 * 
	 * @return true if the current node is 
	 * a {@code GridNode}.
	 */
	public static boolean isNode() {
		return ! clusterManager;
	}

	
}