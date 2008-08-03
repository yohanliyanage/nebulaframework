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
