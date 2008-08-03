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

import java.util.LinkedList;
import java.util.Queue;

/**
 * AbstractJobExecutionManager provides common functionalities
 * required by implementation classes of {@link JobExecutionManager}
 * interface.
 * <p>
 * This class provides necessary functionality to track recently
 * canceled jobs for a {@link JobExecutionManager}. Implementers
 * of new {@link JobExecutionManager}s are advised to use this
 * class as the base class for {@link JobExecutionManager}s.
 * Refer to the source of existing {@link JobExecutionManager}s 
 * for usage guidance.
 * <p>
 * Note that by default, this class keeps only 10 jobIds.
 * Oldest jobIds will be removed when new ones are inserted
 * after reaching size 10.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public abstract class AbstractJobExecutionManager implements
		JobExecutionManager {

	/**
	 * Queue of recently canceled JobIds
	 */
	protected Queue<String> recentlyCancelled = new LinkedList<String>();
	
	/**
	 * Marks the given JobId as a recently canceled GridJob.
	 * If the queue already holds 10 or more jobIds,
	 * oldest jobId will be removed.
	 * 
	 * @param jobId JobId
	 */
	protected void markCanceled(String jobId) {
		
		// Keep Queue Size with in 10
		if (recentlyCancelled.size()>=10) {
			recentlyCancelled.remove();
		}
		
		// Add this Job as canceled
		recentlyCancelled.add(jobId);
		
	}
	
	/**
	 * Returns true if the specified GridJob (joId)
	 * is a recently canceled GridJob of this
	 * {@code JobExecutionManager}.
	 * 
	 * @param jobId JobId
	 * @return true if recently canceled, false otherwise
	 */
	protected boolean isRecentlyCancelled(String jobId) {
		return (recentlyCancelled.contains(jobId));
	}

}
