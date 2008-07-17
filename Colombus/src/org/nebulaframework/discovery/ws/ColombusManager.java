package org.nebulaframework.discovery.ws;

import javax.jws.WebService;

@WebService
public interface ColombusManager {
	public void registerCluster(String ip);
	public void unregisterCluster(String ip);
	public void registerColombusService(String ip);
	public void unregisterColombusService(String ip);
}