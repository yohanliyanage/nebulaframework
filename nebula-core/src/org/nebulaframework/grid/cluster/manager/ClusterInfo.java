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
package org.nebulaframework.grid.cluster.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.util.net.NetUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 * Holds information regarding a {@link ClusterManager}
 * configuration. 
 * <p>
 * <i>Spring Managed</i>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class ClusterInfo {

	private static Log log = LogFactory.getLog(ClusterInfo.class);
	
	private String serviceUrl;
	private String[] transportUrls;
	
	
	/**
	 * Returns the JMS Broker URL used by this cluster.
	 * <p>
	 * This Broker URL is used by nodes with in this cluster to communicate with 
	 * the ClusterManager, and to access its services.
	 * 
	 * @return {@code String} Broker URL
	 */
	public String getServiceUrl() {
		return serviceUrl;
	}
	
	/**
	 * Sets the Broker URL used by Cluster. This Broker URL should point to a JMS Broker, 
	 * which is to be used by the {@code ClusterManager} to communicate with its nodes.
	 * <p>
	 * In default implementation, this URL refers to an embedded JMS Broker managed by the 
	 * Spring container.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param serviceUrl Broker URL
	 */
	@Required
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}
	
	
	/**
	 * Returns the Transport URLs supported by this Cluster.
	 * @return String[] of transport URLs
	 */
	public String[] getTransportUrls() {
		return transportUrls;
	}
	
	/**
	 * Sets the Transport URLs supported by this Cluster.
	 * Transport URLs are service URLs (one for each
	 * protocol) supported by this
	 * ClusterManager for communication with GridNodes 
	 * and other Clusters.
	 * <p>
	 * For example, http://192.168.2.1:61617 is a transport
	 * URL.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param transportUrls Transport URLs, each separated
	 * by a comma.
	 */
	public void setTransports(String transportUrls) {
		
		// Parse URL array from String
		String[] arr = transportUrls.split(",");
		List<String> urls = new ArrayList<String>();
		
		for (int i=0;i<arr.length;i++) {
			
			arr[i] = arr[i].trim();
			
			// If Valid URL
			try {
				if (NetUtils.getHostName(arr[i])!=null) {
					urls.add(arr[i]);
				}
			} catch (RuntimeException e) {
				log.warn("Invalid Transport URL " + arr[i]);
			}
		}
		
		this.transportUrls = urls.toArray(new String[urls.size()]);
	}
	
	/**
	 * Returns the Host Information for the Cluster.
	 * Host information is the hostname:port combination.
	 * 
	 * @return hostname:port
	 */
	public String getHostInfo() {
		return NetUtils.getHostName(serviceUrl) + ":" + NetUtils.getHostPort(serviceUrl);
	}
	
	/**
	 * Returns the list of supported protocols by this Cluster,
	 * separated by spaces.
	 * 
	 * @return supported protocols
	 */
	public String getProtocolInfo() {
		StringBuilder sb = new StringBuilder(NetUtils.getProtocol(serviceUrl));
		
		for (String url : transportUrls) {
			sb.append(" ");
			sb.append(NetUtils.getProtocol(url));
		}
		
		return sb.toString();
	}
}
