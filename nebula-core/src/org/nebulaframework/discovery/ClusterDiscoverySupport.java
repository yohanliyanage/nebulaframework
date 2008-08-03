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
		if (config.containsKey(ConfigurationKeys.COLOMBUS_SERVERS.value())) {
			
			// Read Server URLs
			String servers = config.getProperty(ConfigurationKeys.COLOMBUS_SERVERS.value());
			String[] urls = servers.split(",");
			
			// Set in WSDiscovery, to be invoked when Cluster Initialized
			WSDiscovery.setUrls(urls);
		}
		else {
		}
	}
}
