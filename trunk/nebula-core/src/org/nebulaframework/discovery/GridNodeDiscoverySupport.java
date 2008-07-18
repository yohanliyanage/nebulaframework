package org.nebulaframework.discovery;

import java.net.InetAddress;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.configuration.ConfigurationException;
import org.nebulaframework.configuration.ConfigurationKeys;
import org.nebulaframework.discovery.ws.WSDiscovery;
import org.nebulaframework.grid.Grid;

// TODO FixDoc
public class GridNodeDiscoverySupport {
	
	private static Log log = LogFactory.getLog(GridNodeDiscoverySupport.class);
	
	public static void discover(Properties config) {
		
		InetAddress cluster = null;
		
		// If URL is specified by User, do not attempt discovery
		if (config.containsKey(ConfigurationKeys.CLUSTER_SERVICE.getValue())) {
			
			log.debug("[Discovery] Found Cluster Service in Configuration");
			
			// Update CLUSTER_SERVICE with Service Port
			String clusterService = config.getProperty(ConfigurationKeys.CLUSTER_SERVICE.getValue());
			clusterService = clusterService + ":" + Grid.SERVICE_PORT;
			config.put(ConfigurationKeys.CLUSTER_SERVICE.getValue(), clusterService);
			
			return;
		}
		
		/* -- No User Specified Cluster Service URL : Attempt Discovery -- */
		
		// (1). Attempt Multicast Discovery
		
//		cluster = MulticastDiscovery.discoverCluster();
//		
//		if (cluster != null) {	// Found
//			// FIXME Multicast defaults to TCP ?? OK coz internal cluster comm is TCP by default ?
//			config.put(ConfigurationKeys.CLUSTER_SERVICE.getValue(), "tcp://" + cluster.getHostAddress() + ":" + Grid.SERVICE_PORT);
//			return;
//		}
//		else {
//			log.warn("[Discovery] Multicast Discovery Failed");
//		}
		
		// (2). Attempt Web Service Discovery
		
		if (config.containsKey(ConfigurationKeys.COLOMBUS_SERVERS.getValue())) {
			String[] urls = config.getProperty(ConfigurationKeys.COLOMBUS_SERVERS.getValue()).split(",");
			
			for (String url : urls) {
				try {
					// Process and build full WS URL
					StringBuilder wsURL = new StringBuilder(url.trim());
					if (!url.trim().endsWith("/")) wsURL.append("/");
					wsURL.append(WSDiscovery.WS_DISCOVERY_PATH);
					
					// Attempt Discovery
					String clusterHost = WSDiscovery.discoverCluster(wsURL.toString());
					
					// If Discovered
					if (clusterHost!=null) {
						cluster = InetAddress.getByName(clusterHost);
						break;
					}
				} catch (Exception e) {
					log.debug("[Discovery] Exception while discovering",e);
				}
			}
			
			if (cluster!=null) {
				// Found
				config.put(ConfigurationKeys.CLUSTER_SERVICE.getValue(), "tcp://" + cluster.getHostAddress() + ":" + Grid.SERVICE_PORT);
				return;
			}
		}
		
		throw new ConfigurationException("Unable to Discover a Cluster");
	}
	
}