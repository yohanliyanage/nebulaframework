package org.nebulaframework.grid.cluster.manager.services.peers;

// TODO FixDoc
public interface PeerClusterService {
	public void addCluster(String url);
	public void removeCluster(String url);
	public int getPeerCount();
}
