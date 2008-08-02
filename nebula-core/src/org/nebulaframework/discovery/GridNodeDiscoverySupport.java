package org.nebulaframework.discovery;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	
	private static Properties config = null;
	
	
	/**
	 * Attempts to discover a cluster using the given
	 * GridNode configuration. This overloaded version
	 * invokes the {@link #discover(Properties, boolean)} method
	 * with boolean value {@code true}.
	 * 
	 * @param config GridNode Configuration
	 * in the property file
	 * 
	 * @throws DiscoveryFailureException if failed to discover a node
	 */
	public static void discover(Properties config) {
		discover(config, true);
	}
	
	/**
	 * Attempts to discover a cluster using the given
	 * GridNode configuration.
	 * 
	 * @param config GridNode Configuration
	 * @param useConfig Indicated whether to use value supplied
	 * in the property file
	 * 
	 * @throws DiscoveryFailureException if failed to discover a node
	 */
	public static void discover(Properties config, boolean useConfig) throws DiscoveryFailureException {
		
		
		GridNodeDiscoverySupport.config = config;
		
		// If URL is specified by User, do not attempt discovery
		if (useConfig && config.containsKey(ConfigurationKeys.CLUSTER_SERVICE.value())) {
			log.debug("[Discovery] Found Cluster Service in Configuration");
			return;
		}
		
		/* -- No User Specified Cluster Service URL : Attempt Discovery -- */
		
		// (1). Attempt Multicast Discovery
		if (discoverMulticast()) return;
		
		// (2). Attempt Web Service Discovery
		if (discoverColombus()) return;
		
		throw new DiscoveryFailureException("Unable to Discover a Cluster");
	}
	
	public static boolean discoverMulticast() {
		
		if (config == null) {
			throw new IllegalStateException("Configuration not Set");
		}
		
		String cluster = MulticastDiscovery.discoverCluster();
		
		if (cluster != null) {	// Found
			// FIXME Multicast defaults to TCP ?? OK coz internal cluster comm is TCP by default ?
			config.put(ConfigurationKeys.CLUSTER_SERVICE.value(), "tcp://" + cluster);
			return true;
		}
		else {
			log.debug("[Discovery] Multicast Discovery Failed");
			return false;
		}
	}
	
	public static boolean discoverColombus() {
		
		if (config == null) {
			throw new IllegalStateException("Configuration not Set");
		}
		
		String cluster = null;
		
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
				return true;
			}
		}
		return false;

	}

	/**
	 * Returns the Configuration Properties.
	 * 
	 * @return Configuration Properties
	 */
	public static Properties getConfig() {
		return (Properties) config.clone();
	}
	
	
}
