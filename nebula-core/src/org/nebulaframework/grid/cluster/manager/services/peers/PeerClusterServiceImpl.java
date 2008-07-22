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

// TODO FixDoc
public class PeerClusterServiceImpl implements PeerClusterService{

	private static Log log = LogFactory.getLog(PeerClusterServiceImpl.class);
	
	private Map<String, NetworkConnector> peers = new HashMap<String,NetworkConnector>();
	
	@Override
	public void addCluster(String url) {
		
		if (peers.containsKey(url)) return;
		
		BrokerService broker = ClusterManager.getInstance().getBrokerService();
		NetworkConnector con = null;
		
		try {
			
			log.debug("[PeerService] Connecting with Peer Cluster on " + url);
			con = broker.addNetworkConnector(url);
			
			if (con==null) {
				throw new Exception("NetworkConnector Null");
			}
			
			con.addStaticallyIncludedDestination(new ActiveMQTopic("nebula.cluster.service.topic"));
			con.setConduitSubscriptions(false);
			con.setDynamicOnly(true);
			con.addExcludedDestination(new ActiveMQQueue("nebula.cluster.registration.queue"));
			con.addExcludedDestination(new ActiveMQQueue("nebula.cluster.services.facade.queue"));
			con.setNetworkTTL(128);
			con.setDuplex(true);
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


	@Override
	public void removeCluster(String url) {
		
		NetworkConnector con = peers.get(url);
		
		if (con==null) return;
		
		log.debug("[PeerService] Disconnecting from Peer Cluster on " + url);
		
		BrokerService broker = ClusterManager.getInstance().getBrokerService();
		
		try {
			con.stop();
		} catch (Exception e) {
			log.warn("[PeerService] Unable to Stop Network Connector");
		}
		
		broker.removeNetworkConnector(con);
		log.info("[PeerService] Disconnected from Peer Cluster on " + url);
	}

	@Override
	public int getPeerCount() {
		return peers.size();
	}

}
