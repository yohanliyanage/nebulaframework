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

/**
 * Support class which provides routines to handle failing GridNodes.
 * <p>
 * This class keeps track of each GridNode, and if a GridNode
 * returns fail results for a job for more than {@code MAX_CONSECUTIVE_NODE_FAILS}
 * times consecutively, it bans the GridNode from further task execution
 * of the GridJob.
 * <p>
 * This is necessary to ensure the accuracy and performance of 
 * GridJob execution, as a faulty node which continues to generate
 * fail results may affect the throughput of the execution.
 * <p>
 * All result collection classes are extended from this
 * class, and uses the functionality of this to handle such
 * issues.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class ResultCollectionSupport {

	/**
	 * Maximum allowed consecutive fail results
	 */
	public static final int MAX_CONSECUTIVE_NODE_FAILS = 3;

	private static final Log log = LogFactory
			.getLog(ResultCollectionSupport.class);
	
	/**
	 * Failure Traces. Keeps track of consecutive failures from a given worker. 
	 */
	protected Map<UUID, Integer> failureTrace = new HashMap<UUID, Integer>();
	
	/**
	 * GridJob Profile
	 */
	protected GridJobProfile profile;

	/**
	 * Clears any failure traces for a given worker node.
	 * 
	 * @param workerId Worker UUID
	 */
	protected void clearFailureTrace(UUID workerId) {
		
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
		
		synchronized (failureTrace) {
			
			// If first consecutive failure
			if (!failureTrace.containsKey(workerId)) {
				failureTrace.put(workerId, 1);
			}	
			else {
				
				// Increment failures
				int count = failureTrace.get(workerId) + 1;
				
				// If fails > MAX
				if (count > MAX_CONSECUTIVE_NODE_FAILS) {
					
					// Add to banned list
					try {
						profile.addBannedNode(workerId);
					} catch (RuntimeException e1) {
						e1.printStackTrace();
					}
					
					String msgBody = workerId + "#" + profile.getJobId();
					
					
					// Send banned message
					ServiceMessage message = new ServiceMessage(msgBody, 
					                                            ServiceMessageType.NODE_BANNED);
					
					try {
						ServiceMessageSender sender = ClusterManager.getInstance().getServiceMessageSender();
						sender.sendServiceMessage(message);
						log.warn("[JobService] Failing GridNode Banned : " + workerId);
					} catch (Exception e) {
						log.error("Error Sending Message", e);
					}
					
				}
				
				failureTrace.put(workerId, count);
			}
		}
	}
}
