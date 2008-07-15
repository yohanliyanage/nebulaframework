package org.nebulaframework.grid.cluster.manager.services.heartbeat;

import java.util.UUID;

import org.nebulaframework.grid.cluster.manager.services.jobs.ClusterJobService;
import org.nebulaframework.grid.cluster.node.GridNode;

/**
 * Internal interface definition for {@code ClusterHeartBeatService},
 * which keeps track of HeartBeats from local {@link GridNode}s.
 * <p>
 * Internal interface extends the public interface,
 * but allows access to operations which are not exposed by the public API.
 * <p>
 * <b>Warning : </b>This is to be used by the internal system only, and is not a 
 * part of the public API. Use of this API is strongly discouraged. For the 
 * public API of this service, refer to {@link ClusterJobService}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public interface InternalClusterHeartBeatService extends ClusterHeartBeatService {
	
	/**
	 * Adds a {@code GridNode} to the collection of
	 * {@code GridNode}s tracked by the service.
	 * 
	 * @param nodeId {@code GridNode} Id
	 */
	void addNode(UUID nodeId);
	
	/**
	 * Removes a {@code GridNode} from the collection of
	 * {@code GridNode}s tracked by the service.
	 * 
	 * @param nodeId {@code GridNode} Id
	 */
	void removeNode(UUID nodeId);
}
