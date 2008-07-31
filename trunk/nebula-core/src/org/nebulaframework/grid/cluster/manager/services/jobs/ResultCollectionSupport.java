package org.nebulaframework.grid.cluster.manager.services.jobs;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
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
		
		if (!failureTrace.containsKey(workerId)) {
			failureTrace.put(workerId, 1);
		}
		else {
			
			int count = failureTrace.get(workerId);
			
			if (count > MAX_CONSECUTIVE_NODE_FAILS) {
				
				// Add to banned list
				profile.addBannedNode(workerId);
				
				String msgBody = workerId + "#" + profile.getJobId();
				
				// Send banned message
				ServiceMessage message = new ServiceMessage(msgBody, 
				                                            ServiceMessageType.NODE_BANNED);
				ClusterManager.getInstance().getServiceMessageSender().sendServiceMessage(message);
				
				return;
			}
			
			failureTrace.put(workerId, count + 1);
		}
	}
}
