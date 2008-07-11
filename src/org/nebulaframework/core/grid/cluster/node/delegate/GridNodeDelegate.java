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

package org.nebulaframework.core.grid.cluster.node.delegate;

import java.util.UUID;

import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.deployment.classloading.node.exporter.GridNodeClassExporter;

/**
 * Represents a {@code GridNode} at the {@code ClusterManager}. This class is used by 
 * {@code ClusterManager} to access certain services of a {@code GridNode}.
 * <p>
 * Such services include,
 * <ul>
 * 	<li>{@link GridNodeClassExporter} (Proxy)</li>
 * </ul>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClusterManager
 * @see GridNode
 */
public class GridNodeDelegate {

	private UUID nodeId;
	private GridNodeClassExporter classExporter;


	/**
	 * Constructs a {@code GridNodeDelegate} for a {@code GridNode},
	 * denoted by {@code nodeId}.
	 * 
	 * @param nodeId {@code UUID} Node Identifier
	 */
	public GridNodeDelegate(UUID nodeId) {
		super();
		this.nodeId = nodeId;
	}

	/**
	 * Returns a reference to the {@code GridNodeClassExporter} service
	 * of this {@GridNode}. Note that this is a proxy object, which
	 * communicates with the actual service implementation at the 
	 * {@code GridNode}, using JMS.
	 * 
	 * @return {@code GridNodeClassExporter} proxy
	 */
	public GridNodeClassExporter getClassExporter() {
		return classExporter;
	}

	/**
	 * Sets the {@code GridNodeClassExporter} service proxy, which can be
	 * used to communicate with the actual service on this {@code GridNode}.
	 * 
	 * @param classExporter {@code GridNodeClassExporter} proxy
	 */
	public void setClassExporter(GridNodeClassExporter classExporter) {
		this.classExporter = classExporter;
	}

	/**
	 * Returns the Node Identifier for the represented {@code GridNode}.
	 * 
	 * @return {@code UUID} nodeId
	 */
	public UUID getNodeId() {
		return nodeId;
	}

}
