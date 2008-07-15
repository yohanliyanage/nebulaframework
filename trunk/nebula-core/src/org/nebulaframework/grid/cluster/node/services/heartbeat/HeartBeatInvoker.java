package org.nebulaframework.grid.cluster.node.services.heartbeat;

import java.util.UUID;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.cluster.manager.services.heartbeat.ClusterHeartBeatService;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.nebulaframework.util.jms.JMSNamingSupport;
import org.nebulaframework.util.jms.JMSRemotingSupport;
import org.springframework.remoting.RemoteAccessException;

/**
 * {@code HeartBeatInvoker} is responsible for sending
 * heartbeats to {@code ClusterManager} on behalf of a 
 * {@code GridNode}.
 * <p>
 * Once started, {@code HeartBeatInvoker} sends heart beats to the
 * {@code ClusterHeartBeatService} on each 
 * {@code ClusterHeartBeatService.BEAT_PERIOD}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class HeartBeatInvoker implements Runnable {

	private static Log log = LogFactory.getLog(HeartBeatInvoker.class);
	
	private UUID nodeId;
	private ClusterHeartBeatService heartBeatProxy;
	private boolean stopped;
	
	/**
	 * Constructs and initializes {@code HeartBeatInvoker} for
	 * this {@code GridNode}.
	 */
	public HeartBeatInvoker() {
		super();
		nodeId = GridNode.getInstance().getId();
		initialize(GridNode.getInstance().getConnectionFactory());
	}

	/**
	 * Initializes the {@code HeartBeatInvoker} by creating the
	 * {@code ClusterHeartBeatService} proxy.
	 * 
	 * @param cf JMS {@code ConnectionFactory}
	 */
	private void initialize(ConnectionFactory cf) {
		// Get QueueName
		String queueName = JMSNamingSupport.getHeartBeatServiceQueueName();
		
		// Create Remote Service Proxy
		heartBeatProxy = JMSRemotingSupport.createProxy(cf, queueName, ClusterHeartBeatService.class);
	}

	/**
	 * Sends a Heart Beat in every 
	 * {@code ClusterHeartBeatService.BEAT_PERIOD}.
	 */
	public void run() {
		while(!stopped) {

			try {
				// Sleep for BETA_PERIOD
				Thread.sleep(ClusterHeartBeatService.BEAT_PERIOD);
				
			} catch (InterruptedException e) {
				log.warn("[HeartBeat] Interrupted", e);
			}
			
			// Check if stopped during sleep
			if (stopped) break;
			
			try {
				// Send Beat
				heartBeatProxy.beat(nodeId);
			} catch (RemoteAccessException e) {
				
				// Cluster Down
				log.error("[HeartBeat] Error Sending Heart Beat",e);
				stop();
				
				// Shutdown GridNode
				GridNode.getInstance().shutdown(true, true);
			}
		}
	}

	/**
	 * Starts the {@code HeartBeatInvoker}
	 */
	public void start() {
		
		// Check if properly initialized
		if (this.nodeId==null || this.heartBeatProxy==null) {
			throw new IllegalStateException("Not initialized");
		}
		
		// Start Thread
		Thread t = new Thread(this);
		t.setDaemon(true);
		t.start();
		log.debug("[HeartBeat] Invoker Started");
	}
	
	/**
	 * Stops the {@code HeartBeatInvoker}
	 */
	public void stop() {
		stopped = true;
		log.debug("[HeartBeat] Invoker Stopped");
	}
}
