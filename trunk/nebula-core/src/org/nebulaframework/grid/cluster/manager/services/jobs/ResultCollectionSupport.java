package org.nebulaframework.grid.cluster.manager.services.jobs;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.manager.services.messaging.ServiceMessageSender;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;

// TODO FixDoc
public class ResultCollectionSupport {

	public static final int MAX_CONSECUTIVE_NODE_FAILS = 3;

	private static final Log log = LogFactory
			.getLog(ResultCollectionSupport.class);
	
	// Failure Traces. Keeps track of consecutive failures from a given
	// worker. If the # of failures > MAX_CONSECUTIVE_NODE_FAILS,
	// the node will be banned from this job.
	protected Map<UUID, Integer> failureTrace = new HashMap<UUID, Integer>();
	protected GridJobProfile profile;

	/**
	 * Clears any failure traces for a given worker node.
	 * 
	 * @param workerId Worker UUID
	 */
	protected void clearFailureTrace(UUID workerId) {
		
		log.trace("Clearing Failure Trace for " + workerId);
		
		if (failureTrace.containsKey(workerId)) {
			failureTrace.remove(workerId);
		}
	}
	
	/**
	 * Adds a failure to the failure trace of the 
	 * given worker node.
	 * 
	 * @param workerId Worker UUID
	 */
	protected void addFailureTrace(UUID workerId) {
		
		log.trace("Adding Failure for " + workerId);
		
		synchronized (failureTrace) {
			if (!failureTrace.containsKey(workerId)) {
				failureTrace.put(workerId, 1);
			}	
			else {
				
				int count = failureTrace.get(workerId) + 1;
				
				if (count > MAX_CONSECUTIVE_NODE_FAILS) {
					
					log.trace("Baning " + workerId);
					
					// Add to banned list
					try {
						profile.addBannedNode(workerId);
					} catch (RuntimeException e1) {
						e1.printStackTrace();
					}
					
					String msgBody = workerId + "#" + profile.getJobId();
					
					log.trace("Msg " + msgBody);
					
					// Send banned message
					ServiceMessage message = new ServiceMessage(msgBody, 
					                                            ServiceMessageType.NODE_BANNED);
					
					log.trace("Msg Created");
					
					try {
						ServiceMessageSender sender = ClusterManager.getInstance().getServiceMessageSender();
						log.trace("I got Sender : " + sender);
						sender.sendServiceMessage(message);
						log.trace("Sent");
					} catch (Exception e) {
						log.error("Error Sending Message", e);
					}
					
					log.trace("Sent Bann Message " + workerId);
				}
				
				failureTrace.put(workerId, count);
			}
		}
		log.trace("Failure Count for " + workerId + " = " + failureTrace.get(workerId));
	}
}
