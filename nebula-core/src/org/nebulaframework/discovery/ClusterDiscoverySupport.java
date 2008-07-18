package org.nebulaframework.discovery;

import java.util.Properties;

import org.nebulaframework.configuration.ConfigurationKeys;
import org.nebulaframework.discovery.ws.WSDiscovery;

/**
 * Support class which assists in discovery related operations
 * for {@code ClusterManager}s. 
 *  
 * @author Yohan Liyanage
 * @version 1.0
 */
public class ClusterDiscoverySupport {

	/**
	 * Registers the current {@code ClusterManager} with 
	 * given Colombus Discovery Servers.
	 * 
	 * @param config Cluster Configuration
	 */
	public static void registerColombus(Properties config) {
		if (config.containsKey(ConfigurationKeys.COLOMBUS_SERVERS.getValue())) {
			
			// Read Server URLs
			String servers = config.getProperty(ConfigurationKeys.COLOMBUS_SERVERS.getValue());
			String[] urls = servers.split(",");
			
			// Set in WSDiscovery, to be invoked when Cluster Initialized
			WSDiscovery.setUrls(urls);
		}
		else {
		}
	}
}
