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
package org.nebulaframework.discovery.ws;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;
import org.nebulaframework.util.net.NetUtils;
import org.springframework.util.Assert;

/**
 * Support class which allows to invoke Web Service (Colombus) based
 * discovery mechanisms.
 * <p>
 * This class facilitates the Cluster registration with Colombus Servers
 * and also, peer discovery and cluster discovery for GridNodes.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class WSDiscovery {
	
	/**
	 * Web Service End-Point URL Suffix for Discovery
	 */
	public static final String WS_DISCOVERY_PATH = "Colombus/Discovery";
	
	/**
	 * Web Service End-Point URL Suffix for Manager
	 */
	public static final String WS_MANAGER_PATH = "Colombus/Manager";
	
	/**
	 * Web Service Namespace for Colombus
	 */
	public static final String WS_NAMESPACE = "http://ws.discovery.nebulaframework.org";
	
	private static Log log = LogFactory.getLog(WSDiscovery.class);
	private static URL[] urls = null;
	
	private static ExecutorService exService = Executors.newSingleThreadExecutor();
	
	/**
	 * Sets the Colombus Server URLs
	 * 
	 * @param urls colombus servers
	 */
	public static void setUrls(String[] urls) {
		
		List<URL> lst = new ArrayList<URL> ();
		for (String strUrl : urls) {
			try {
				// Process and build full WS URL
				StringBuilder wsURL = new StringBuilder(strUrl.trim());
				if (wsURL.toString().endsWith("/")) wsURL.append("/");
				wsURL.append(WSDiscovery.WS_MANAGER_PATH);
				
				// Create and enlist URL object
				URL url = new URL(wsURL.toString());
				lst.add(url);
			} catch (MalformedURLException e) {
				log.warn("Malformed URL in Colombus Server URLs : " + strUrl);
			}
		}
		
		WSDiscovery.urls = lst.toArray(new URL[] {});
	}

	/**
	 * Registers the current ClusterManager in the Colombus
	 * Severs specified using {@link #setUrls(String[])} method.
	 * 
	 * @throws UnsupportedOperationException if invoked by any member
	 * other than a ClusterManager.
	 */
	public static void registerCluster() throws UnsupportedOperationException {
		
		if (! Grid.isClusterManager()) {
			throw new UnsupportedOperationException("Allowed only to ClusterManagers");
		}
		
		if (urls==null) {
			log.debug("[WSDiscovery] No Colombus Servers to Register");
			return;
		}
		
		// Register on each Colombus Server
		for (URL url : urls) {
			registerCluster(url);
		}
		
		// Shutdown ThreadExecutor when finished
		exService.shutdown();
	}
	
	/**
	 * Registers the current ClusterManager in the Colombus
	 * Sever specified by {@code url}.
	 * 
	 * @throws IllegalArgumentException if URL is invalid
	 * @throws UnsupportedOperationException if invoked by any member
	 * other than a ClusterManager.
	 */
	public static void registerCluster(final URL url)  
			throws 	IllegalArgumentException, 
					UnsupportedOperationException {
		
		Assert.notNull(url);
		
		if (! Grid.isClusterManager()) {
			throw new UnsupportedOperationException("Allowed only to ClusterManagers");
		}
		
		exService.submit(new Runnable() {
			public void run() {
				doRegisterCluster(url);
			}
		});
	}

	/**
	 * Internal method which registers the current ClusterManager in a given
	 * Colombus Server.
	 * 
	 * @param url Server URL
	 * @throws IllegalArgumentException if URL is invalid
	 */
	protected static void doRegisterCluster(URL url) throws IllegalArgumentException {
		
			Assert.notNull(url);
			
			try {
				log.debug("[WSDiscovery] Connecting Colombus on " + url);
				
				// Create CXF JAX-WS Proxy
				JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
				factory.setServiceClass(ColombusManager.class);
				factory.setAddress(url.toString());
				final ColombusManager mgr = (ColombusManager) factory.create();

				// Get Broker Service Host IP
				String serviceUrl = ClusterManager.getInstance().getClusterInfo().getServiceUrl();
				final String serviceIP = NetUtils.getHostAddress(serviceUrl);
				
				// Register in Colombus Service
				mgr.registerCluster(serviceIP);

				// Register for Cluster Shutdown Event to Unregister Cluster
				ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {

					@Override
					public void onServiceEvent(ServiceMessage event) {
						mgr.unregisterCluster(serviceIP);
					}
					
				}, ClusterManager.getInstance().getClusterId().toString()
				, ServiceMessageType.CLUSTER_SHUTDOWN);
				
				log.info("[WSDiscovery] Registered on Colombus Server " + url.getHost());
				
			} catch (UnknownHostException e) {
				log.warn("[WSDiscovery] Registration Failed : " + url.getHost());
			}
	}

	/**
	 * Allows GridNodes to discover clusters using a given Colombus Server,
	 * denoted by the {@code url}.
	 * 
	 * @param url server URL
	 * @return IP Address of Cluster 
	 */
	public static String discoverCluster(String url)  {
		
		log.debug("[WSDiscovery] Attempting Discovery  (" + url + ")");
		
		// Create CXF JAX-WS Proxy
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(ColombusDiscovery.class);
		factory.setAddress(url);
		final ColombusDiscovery discovery = (ColombusDiscovery) factory.create();
		
		// Attempt Discovery
		String cluster = discovery.discover();
		
		if (cluster==null) {
			// Discovery Failed
			log.debug("[WSDiscovery] Discovery Failed (" + url + ")");
		}
		else {
			// Discovered
			log.info("[WSDiscovery] Discovered Cluster " + cluster);
		}
		
		return cluster;
	}


	// FIXME Implement Peer Cluster Discovery
	
	
}
