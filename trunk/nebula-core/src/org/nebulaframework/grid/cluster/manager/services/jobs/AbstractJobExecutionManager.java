package org.nebulaframework.grid.cluster.manager.services.jobs;

import java.util.LinkedList;
import java.util.Queue;

// TODO FixDoc
public abstract class AbstractJobExecutionManager implements
		JobExecutionManager {

	protected Queue<String> recentlyCancelled = new LinkedList<String>();
	
	protected void markCanceled(String jobId) {
		
		// Keep Queue Size with in 10
		if (recentlyCancelled.size()>=10) {
			recentlyCancelled.remove();
		}
		
		// Add this Job as canceled
		recentlyCancelled.add(jobId);
		
	}
	
	protected boolean isRecentlyCancelled(String jobId) {
		return (recentlyCancelled.contains(jobId));
	}

}
