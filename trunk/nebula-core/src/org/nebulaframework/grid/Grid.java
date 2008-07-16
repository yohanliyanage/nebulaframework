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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.nebulaframework.util.spring.NebulaApplicationContext;
import org.nebulaframework.util.system.SystemUtils;

/**
 * The general access point to Nebula Grid. Allows to configure
 * and start {@link ClusterManager} and {@link GridNode} instances.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class Grid {
	
	/** Cluster Configuration Properties File */
	public static final String CLUSTER_CONFIGURATION = "nebula-cluster.properties";
	
	/** Grid Node Configuration Properties File */
	public static final String GRIDNODE_CONFIGURATION = "nebula-client.properties";
	
	/** Cluster Spring Beans Configuration File */
	public static final String CLUSTER_SPRING_CONTEXT = "org/nebulaframework/grid/cluster/manager/cluster-manager.xml";
	
	/** Grid Node Spring Beans Configuration File */
	public static final String GRIDNODE_CONTEXT = "org/nebulaframework/grid/cluster/node/grid-node.xml";
	
	/** Light Weight (Non-worker) Grid Node Spring Beans Configuration File */
	public static final String GRIDNODE_LIGHT_CONTEXT = "org/nebulaframework/grid/cluster/node/grid-nonworker-node.xml";
	
	
	
	private static Log log = LogFactory.getLog(Grid.class);
	
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
		
		
		Properties props = new Properties();
		
		try {
			props.load(new FileInputStream(CLUSTER_CONFIGURATION));
			
		} catch (IOException e) {
			log.warn("[Grid] Failed to read " + CLUSTER_CONFIGURATION, e);
			// TODO Fail Over To Default
			throw new AssertionError(e);
		}
		
		applicationContext = new NebulaApplicationContext(CLUSTER_SPRING_CONTEXT, props);
		clusterManager = true;
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
		
		Properties props = new Properties();
		
		// Get SystemInfo
		SystemUtils.detectSystemInfo(props);
		
		
		try {
			props.load(new FileInputStream(GRIDNODE_CONFIGURATION));
			
		} catch (IOException e) {
			log.warn("[Grid] Failed to read " + GRIDNODE_CONFIGURATION, e);
			// TODO Fail Over To Default
			throw new AssertionError(e);
		}
		
		applicationContext = new NebulaApplicationContext(GRIDNODE_CONTEXT, props);
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
		
		Properties props = new Properties();
		
		// Get SystemInfo
		SystemUtils.detectSystemInfo(props);
		
		
		try {
			props.load(new FileInputStream(GRIDNODE_CONFIGURATION));
			
		} catch (IOException e) {
			log.warn("[Grid] Failed to read " + GRIDNODE_CONFIGURATION, e);
			// TODO Fail Over To Default
			throw new AssertionError(e);
		}
		
		applicationContext = new NebulaApplicationContext(GRIDNODE_LIGHT_CONTEXT, props);
		clusterManager = false;
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
