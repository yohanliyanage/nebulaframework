package org.nebulaframework.grid.cluster.manager.services.heartbeat;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;

/**
 * HeartBeatTracker keeps track of HeartBeats from a specific
 * {@code GridNode}, and if more than {@link ClusterHeartBeatService#MAX_MISS}
 * heartbeats are missed, it dispatches a {@link ServiceMessage}
 * notifying that a {@link ServiceMessageType#HEARTBEAT_FAILED} has
 * occurred for the given {@code GridNode}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
class HeartBeatTracker implements Runnable {

	private static Log log = LogFactory.getLog(HeartBeatTracker.class);

	private UUID nodeId;
	private long lastBeat = System.currentTimeMillis();
	private int missed = 0;
	private boolean stopped = false;

	/**
	 * Constructs a {@code HeartBeatTracker} which is to
	 * track the heartbeats from a {@code GridNode} with
	 * given nodeId
	 * 
	 * @param nodeId {@code GridNode} id
	 */
	public HeartBeatTracker(UUID nodeId) {
		super();
		this.nodeId = nodeId;

	}

	/**
	 * Checks for HeartBeats and notifies if heartbeats 
	 * have missed for more than {@link ClusterHeartBeatService#MAX_MISS}.
	 * <p>
	 * Executed on a separate thread.
	 */
	public void run() {
		while (!stopped) {

			// Remember time before sleeping
			long sleepTime = System.currentTimeMillis();

			try {
				// Sleep for BEAT_PERIOD
				Thread.sleep(ClusterHeartBeatService.BEAT_PERIOD);
			} catch (InterruptedException e) {
				log.warn("Heartbeat Sleep Interrupted", e);
				continue;
			}

			// Check if stopped during sleep
			if (stopped)
				break;

			// If no new beat during sleep time
			if (lastBeat - sleepTime < 0) {
				
				missed++;

				// If  missed more than MAX_MISS, notify
				if (missed > ClusterHeartBeatService.MAX_MISS) {
					ServiceMessage message = new ServiceMessage(nodeId
							.toString(), ServiceMessageType.HEARTBEAT_FAILED);
					ClusterManager.getInstance().getServiceMessageSender()
							.sendServiceMessage(message);
				}
			}
			
		}
	}

	/**
	 * Invoked to notify that a new heartbeat has received
	 * from the {@code GridNode} of this {@code HeartBeatTracker}.
	 */
	public void notifyBeat() {
		lastBeat = System.currentTimeMillis();
		missed = 0;
	}

	/**
	 * Starts the {@code HeartBeatTracker}.
	 */
	public void start() {
		Thread beatThread = new Thread(this, "HeartBeatTracker-"
				+ nodeId.toString());
		beatThread.setDaemon(true);
		beatThread.start();
		log.debug("[HeartBeat] Started Tracking : " + nodeId);
	}

	/**
	 * Stops the {@code HeartBeatTracker}.
	 */
	public void stop() {
		log.debug("[HeartBeat] Stopped Tracking : " + nodeId);
		this.stopped = true;
	}

}
