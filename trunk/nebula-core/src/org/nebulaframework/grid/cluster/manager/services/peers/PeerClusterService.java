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
package org.nebulaframework.grid.cluster.manager.services.peers;

/**
 * Peer Cluster Service supports management of
 * peer clusters of the ClusterManager.
 * <p>
 * This interface defines methods which allows
 * to add and remove peer clusters, denoted by
 * a service URL.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public interface PeerClusterService {
	
	/**
	 * Adds a Peer Cluster denoted by the 
	 * given URL, and starts a network connector
	 * for communication with the peer.
	 * 
	 * @param url Service URL
	 */
	public void addCluster(String url);
	
	/**
	 * Removes the peer cluster specified by the
	 * service URL, if exists as a peer of this
	 * cluster, and stops any network connectors
	 * with the peer.
	 * 
	 * @param url removes peer Cluster
	 */
	public void removeCluster(String url);
	
	/**
	 * Returns the peer count for this Cluster.
	 * 
	 * @return peer count
	 */
	public int getPeerCount();
}
