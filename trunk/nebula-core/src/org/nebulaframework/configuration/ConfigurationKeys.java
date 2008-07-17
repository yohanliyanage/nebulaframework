package org.nebulaframework.configuration;

// TODO FixDOc
public enum ConfigurationKeys {
	
	CLUSTER_SERVICE("cluster.service"), COLOMBUS_SERVERS ("colombus.servers");
	
	private String value = "";
	
	private ConfigurationKeys(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}


	
}
