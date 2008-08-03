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

import java.util.HashMap;
import java.util.Map;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.network.NetworkConnector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;

/**
 * Implementation of Peer Cluster Service supports management of
 * peer clusters of the ClusterManager.
 * <p>
 * This class provides mechanisms for establishing network connections
 * between peer clusters for decentralized communication.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class PeerClusterServiceImpl implements PeerClusterService{

	private static Log log = LogFactory.getLog(PeerClusterServiceImpl.class);
	
	// Peer Network Connectors
	private Map<String, NetworkConnector> peers = new HashMap<String,NetworkConnector>();
	
	/**
	 * {@inheritDoc}
	 */
	public void addCluster(String url) {
		
		if (peers.containsKey(url)) return;
		
		// Get JMS Broker
		BrokerService broker = ClusterManager.getInstance().getBrokerService();
		
		// Peer Network Connection
		NetworkConnector con = null;
		
		try {
			
			log.debug("[PeerService] Connecting with Peer Cluster on " + url);
			con = broker.addNetworkConnector(url);
			
			if (con==null) {
				throw new Exception("NetworkConnector Null");
			}
			
			// Configure Network Connection
			con.addStaticallyIncludedDestination(new ActiveMQTopic("nebula.cluster.service.topic"));
			con.setConduitSubscriptions(false);
			con.setDynamicOnly(true);	// Do not forward if no consumers
			con.addExcludedDestination(new ActiveMQQueue("nebula.cluster.registration.queue"));
			con.addExcludedDestination(new ActiveMQQueue("nebula.cluster.services.facade.queue"));
			con.setNetworkTTL(128);		// Default TTL
			con.setDuplex(true);		// Full-duplex communication
			con.start();

			peers.put(url, con);
			log.info("[PeerService] Connected with Peer Cluster on " + url);
			
			// Notify Event Hooks
			ServiceMessage message = new ServiceMessage(url, ServiceMessageType.PEER_CONNECTION);
			ServiceEventsSupport.getInstance().onServiceMessage(message);
			
		} catch (Exception e) {
			log.warn("[PeerService] Unable to connect with Cluster " + url, e);
			if (con!=null) {	// If Connection was created
				try {
					// Try to Stop
					con.stop();
				} catch (Exception e1) {
					log.warn("[PeerService] Unable to Stop Connector " + url, e1);
				}
				broker.removeNetworkConnector(con);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeCluster(String url) {
		
		NetworkConnector con = peers.get(url);
		
		if (con==null) return;
		
		log.debug("[PeerService] Disconnecting from Peer Cluster on " + url);
		
		BrokerService broker = ClusterManager.getInstance().getBrokerService();
		
		try {
			// Stop Network Connection
			con.stop();
		} catch (Exception e) {
			log.warn("[PeerService] Unable to Stop Network Connector");
		}
		
		// Remove Network Connector
		broker.removeNetworkConnector(con);
		log.info("[PeerService] Disconnected from Peer Cluster on " + url);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPeerCount() {
		return peers.size();
	}

}
