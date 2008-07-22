package org.nebulaframework.grid.cluster.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.util.net.NetUtils;
import org.springframework.beans.factory.annotation.Required;

// TODO Fix Doc
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
	
	
	public String[] getTransportUrls() {
		return transportUrls;
	}
	
	
	public void setTransports(String transportUrls) {
		
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
	
	public String getHostInfo() {
		return NetUtils.getHostName(serviceUrl) + ":" + NetUtils.getHostPort(serviceUrl);
	}
	
	public String getProtocolInfo() {
		StringBuilder sb = new StringBuilder(NetUtils.getProtocol(serviceUrl));
		
		for (String url : transportUrls) {
			sb.append(" ");
			sb.append(NetUtils.getProtocol(url));
		}
		
		return sb.toString();
	}
}
