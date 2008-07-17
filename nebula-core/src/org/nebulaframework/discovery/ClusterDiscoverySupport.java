package org.nebulaframework.discovery;

import java.util.Properties;

import org.nebulaframework.configuration.ConfigurationKeys;
import org.nebulaframework.discovery.ws.WSDiscovery;

public class ClusterDiscoverySupport {

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
