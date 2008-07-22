package org.nebulaframework.discovery;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.configuration.ConfigurationException;
import org.nebulaframework.configuration.ConfigurationKeys;
import org.nebulaframework.discovery.multicast.MulticastDiscovery;
import org.nebulaframework.discovery.ws.WSDiscovery;

/**
 * Discovery Support for GridNodes. This class provides
 * support routines which allows to discover clusters
 * using Property Files, XML Configuration, Multicast
 * and Colombus Web Service Discovery.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class GridNodeDiscoverySupport {
	
	private static Log log = LogFactory.getLog(GridNodeDiscoverySupport.class);
	
	/**
	 * Attempts to discover a cluster using the given
	 * GridNode configuration.
	 * 
	 * @param config GridNode Configuration
	 */
	public static void discover(Properties config) {
		
		String cluster = null;
		
		// If URL is specified by User, do not attempt discovery
		if (config.containsKey(ConfigurationKeys.CLUSTER_SERVICE.value())) {
			log.debug("[Discovery] Found Cluster Service in Configuration");
			return;
		}
		
		// FIXME Multicast Commented to test WS
		
		/* -- No User Specified Cluster Service URL : Attempt Discovery -- */
		
		// (1). Attempt Multicast Discovery
		
		cluster = MulticastDiscovery.discoverCluster();
		
		if (cluster != null) {	// Found
			// FIXME Multicast defaults to TCP ?? OK coz internal cluster comm is TCP by default ?
			config.put(ConfigurationKeys.CLUSTER_SERVICE.value(), "tcp://" + cluster);
			return;
		}
		else {
			log.warn("[Discovery] Multicast Discovery Failed");
		}
		
		// (2). Attempt Web Service Discovery
		
		if (config.containsKey(ConfigurationKeys.COLOMBUS_SERVERS.value())) {
			String[] urls = config.getProperty(ConfigurationKeys.COLOMBUS_SERVERS.value()).split(",");
			
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
						cluster = clusterHost;
						break;
					}
				} catch (Exception e) {
					log.debug("[Discovery] Exception while discovering",e);
				}
			}
			
			if (cluster!=null) {
				// Found
				config.put(ConfigurationKeys.CLUSTER_SERVICE.value(), "tcp://" + cluster);
				return;
			}
		}
		
		throw new ConfigurationException("Unable to Discover a Cluster");
	}
	
}
