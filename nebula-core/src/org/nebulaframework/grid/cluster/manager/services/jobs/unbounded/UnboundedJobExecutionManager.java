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
package org.nebulaframework.grid.cluster.manager.services.jobs.unbounded;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.unbounded.UnboundedGridJob;
import org.nebulaframework.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.grid.cluster.manager.services.jobs.JobExecutionManager;
import org.nebulaframework.grid.cluster.manager.services.jobs.tracking.GridJobTaskTracker;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;

/**
 * Implementation of {@code UnboundedJobService} interface, which
 * is responsible for starting and managing execution of 
 * {@code UnboundedGridJob}s.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class UnboundedJobExecutionManager implements JobExecutionManager {

	private static Log log = LogFactory.getLog(UnboundedJobExecutionManager.class);
	
	private Map<String, UnboundedJobProcessor> processors = new HashMap<String, UnboundedJobProcessor>();


	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns the {@code UnboundedGridJob} class.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends GridJob> getInterface() {
		
		// Return UnboundedGridJob Class
		return UnboundedGridJob.class;
	}
	

	/**
	 * Starts given {@code UnboundedGridJob} on the Grid.
	 * 
	 * @param profile GridJobProfile
	 */
	@Override
	public boolean startExecution(final GridJobProfile profile) {
		
		// If Valid GridJob Type
		if (profile.getJob() instanceof UnboundedGridJob) {
			
			// Disallow Final Results
			profile.getFuture().setFinalResultSupported(false);
			
			// Start Task Tracker
			profile.setTaskTracker(GridJobTaskTracker.startTracker(profile, this));
			
			new Thread(new Runnable() {
				public void run() {
					
					log.info("[UnboundedJobService] Starting Processsor");
					
					// Create Processor
					UnboundedJobProcessor processor = new UnboundedJobProcessor(profile);
					processors.put(profile.getJobId(), processor);
					
					
					// Add Service Hook to Remove Processor When Done
					ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {

						@Override
						public void onServiceEvent(ServiceMessage message) {
							// Remove Processor
							processors.remove(profile.getJobId());
						}
						
					}, profile.getJobId(), ServiceMessageType.JOB_CANCEL, ServiceMessageType.JOB_END);
					processor.start();
				}
			}).start();
			
			// Register as Execution Manager
			profile.setExecutionManager(this);
			
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean cancel(String jobId) {
		if (processors.containsKey(jobId)){
			return processors.get(jobId).cancel();
		}
		else {
			log.warn("[UnboundedJobService] Unable to Cancel Job " 
			         + jobId + " : No Processor Reference");
			return false;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reEnqueueTask(String jobId, int taskId) {
		if (processors.containsKey(jobId)){
			log.debug("[UnboundedJobService] Re-enqueing Task" 
			          + jobId + "|" + taskId);
			processors.get(jobId).reEnqueueTask(taskId);
		}
		else {
			log.debug("[UnboundedJobService] Unable to Re-enqueue Task " 
			         + jobId + "|" + taskId +" - No Processor Reference");
		}
	}




}
