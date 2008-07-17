package org.nebulaframework.discovery.ws;

import javax.jws.WebService;

import org.nebulaframework.discovery.ws.datastore.ClusterData;
import org.nebulaframework.discovery.ws.datastore.ColombusServices;

@WebService(endpointInterface="org.nebulaframework.discovery.ws.ColombusManager", serviceName="ColombusManager")
public class ColombusManagerImpl implements ColombusManager {

	public void registerCluster(String ip) {
		ClusterData.addCluster(ip);
	}

	public void registerColombusService(String ip) {
		ColombusServices.addColombusServer(ip);
	}

	public void unregisterCluster(String ip) {
		ClusterData.removeCluster(ip);
	}

	public void unregisterColombusService(String ip) {
		ColombusServices.removeColombusServer(ip);
	}
}