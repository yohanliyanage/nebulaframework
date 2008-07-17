package org.nebulaframework.discovery.ws;

import javax.jws.WebService;
import org.nebulaframework.discovery.ws.datastore.ClusterData;

@WebService(endpointInterface="org.nebulaframework.discovery.ws.ColombusDiscovery", serviceName="ColombusDiscovery")
public class ColombusDiscoveryImpl implements ColombusDiscovery {
	
	public String discover() {
		return ClusterData.nextCluster();
	}

}