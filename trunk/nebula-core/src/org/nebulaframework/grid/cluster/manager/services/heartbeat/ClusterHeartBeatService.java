package org.nebulaframework.grid.cluster.manager.services.heartbeat;

import java.util.UUID;

import org.nebulaframework.grid.cluster.node.GridNode;

/**
 * {@code ClusterHeartBeatService} keeps track of 
 * HeartBeats from local {@link GridNode}s. This service
 * is exposed as a remote service and all {@code GridNode}s
 * are to invoke the {@link #beat(UUID)} method in cycles of
 * {@link #BEAT_PERIOD}.
 * <p>
 * If a {@link GridNode} fails to provide {@link #MAX_MISS}
 * number of heart beats consecutively, the node is considered
 * to be failed, and a ServiceMessage will be dispatched 
 * regarding the event.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public interface ClusterHeartBeatService {

	/** Period between heart beats in milliseconds */
	public static final long BEAT_PERIOD = 2500L;
	
	/** Maximum allowed missed beats before considering disconnected */
	public static final int MAX_MISS = 3;
	
	/**
	 * Invoked to notify a heartbeat from the {@code GridNode}
	 * with the given nodeId.
	 * 
	 * @param nodeId {@code GridNode} Id
	 */
	public void beat(UUID nodeId);
}
