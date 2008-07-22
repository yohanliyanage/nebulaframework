package org.nebulaframework.configuration;

/**
 * Enumeration which contains key names values used in
 * Configuration Property File.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public enum ConfigurationKeys {
	
	/**
	 * Cluster Service URL (Broker)
	 */
	CLUSTER_SERVICE("cluster.service"), 
	
	/**
	 * Alternative Transport URLs
	 */
	TRANSPORT_URLS("cluster.transports"), 
	
	/**
	 * Colombus Servers (List, splitted by ',')
	 */
	COLOMBUS_SERVERS ("colombus.servers");
	
	private String value = "";
	
	/**
	 * Constructs a {@link ConfigurationKeys} instance with
	 * given String as value.
	 * 
	 * @param value value of key
	 */
	private ConfigurationKeys(String value) {
		this.value = value;
	}

	/**
	 * Returns the value of this Configuration Key.
	 * 
	 * @return String value of Configuration Key.
	 */
	public String value() {
		return value;
	}


	
}
