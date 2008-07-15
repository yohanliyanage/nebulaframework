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

import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.node.GridNode;

/**
 * Holds information regarding the current GridNode.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class GridInfo {
	
	private static boolean initialized = false;
	private static boolean clusterManager = false;

	/**
	 * Initializes the {@code GridNodeInfo} class.
	 * Note that this can be configured only once.
	 * 
	 * @param clusterManager {@code true} if this node is a cluster manager
	 * @throws IllegalStateException if initialized before
	 */
	public static void initialize(boolean clusterManager) throws IllegalStateException {
		
		if (initialized) {
			throw new IllegalStateException("Already Initialized");
		}
		
		GridInfo.clusterManager = clusterManager;
	}
	
	/**
	 * Returns true if the current node is 
	 * a {@link ClusterManager}.
	 * 
	 * @return true if the current node is 
	 * a {@link ClusterManager}.
	 */
	public static boolean isClusterManager() {
		return clusterManager;
	}
	
	/**
	 * Returns true if the current node is 
	 * a {@link GridNode}.
	 * 
	 * @return true if the current node is 
	 * a {@link GridNode}.
	 */
	public static boolean isNode() {
		return ! clusterManager;
	}
	
	
}
