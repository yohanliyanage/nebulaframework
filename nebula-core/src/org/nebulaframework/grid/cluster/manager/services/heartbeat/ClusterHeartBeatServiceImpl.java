package org.nebulaframework.grid.cluster.manager.services.heartbeat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.nebulaframework.grid.service.event.ServiceEvent;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessageType;
import org.nebulaframework.util.jms.JMSNamingSupport;
import org.nebulaframework.util.jms.JMSRemotingSupport;

/**
 * Implementation of {@code ClusterHeartBeatService} which keeps 
 * track of HeartBeats from local {@link GridNode}s. This service
 * is exposed as a remote service and all {@code GridNode}s
 * are to invoke the {@link #beat(UUID)} method in cycles of
 * {@link #BEAT_PERIOD}.
 * <p>
 * If a {@link GridNode} fails to provide {@link #MAX_MISS}
 * number of heart beats consecutively, the node is considered
 * to be failed, and a ServiceMessage will be dispatched 
 * regarding the event.
 * <p>
 * <i>Spring Managed</i>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class ClusterHeartBeatServiceImpl implements ClusterHeartBeatService, InternalClusterHeartBeatService {

	private static Log log = LogFactory.getLog(ClusterHeartBeatServiceImpl.class);
	
	/** HeartBeatTrackers for tracked GridNodes */
	private Map<UUID, HeartBeatTracker> trackers = new HashMap<UUID, HeartBeatTracker>();

	
	/**
	 * Constructs a {@code ClusterHeartBeatServiceImpl}.
	 * @param cf JMS {@code ConnectionFactory}
	 */
	public ClusterHeartBeatServiceImpl(ConnectionFactory cf) {
		super();
		initialize(cf);
	}
	
	/**
	 * Initializes the {@code ClusterHeartBeatServiceImpl}, and starts
	 * the remote service for the {@code ClusterHeartBeatService}.
	 * 
	 * @param cf JMS {@code ConnectionFactory}
	 */
	private void initialize(ConnectionFactory cf) {
		
		// Get QueueName
		String queueName = JMSNamingSupport.getHeartBeatServiceQueueName();
		
		// Create Remote Service
		JMSRemotingSupport.createService(cf, queueName, this, ClusterHeartBeatService.class);
		log.debug("[HeartBeat] Service Started");
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void beat(UUID nodeId) {
		// If no tracker, ignore
		if (!trackers.containsKey(nodeId)) return;

		// Notify HeartBeatTracker
		try {
			trackers.get(nodeId).notifyBeat();
		} catch (Exception e) {
			log.warn("[HeartBeat] Exception while HeartBeat notification",e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addNode(final UUID nodeId) {
		
		HeartBeatTracker tracker = new HeartBeatTracker(nodeId);
		this.trackers.put(nodeId, tracker);
		
		tracker.start();
		
		//Add ServiceHook to auto remove node when disconnected
		
		ServiceEvent event = new ServiceEvent();
		event.addType(ServiceMessageType.NODE_UNREGISTERED);
		event.addType(ServiceMessageType.HEARTBEAT_FAILED);
		
		event.setMessage(nodeId.toString());
		
		ServiceHookCallback callback = new ServiceHookCallback() {
			public void onServiceEvent(ServiceEvent event) {
				removeNode(nodeId);
			}
		};
		
		ServiceEventsSupport.getInstance().addServiceHook(event, callback);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeNode(UUID nodeId) {
		HeartBeatTracker tracker = trackers.remove(nodeId);
		if (tracker!=null) {
			tracker.stop();
		}
	}


}
