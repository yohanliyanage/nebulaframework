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
package org.nebulaframework.grid.cluster.manager.services.heartbeat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessage;
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
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {
			public void onServiceEvent(ServiceMessage message) {
				removeNode(nodeId);
			}
		},nodeId.toString(),ServiceMessageType.NODE_UNREGISTERED, ServiceMessageType.HEARTBEAT_FAILED );
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
