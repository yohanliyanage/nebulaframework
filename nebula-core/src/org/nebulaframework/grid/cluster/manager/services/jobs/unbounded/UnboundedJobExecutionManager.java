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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.UnboundedGridJob;
import org.nebulaframework.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.grid.cluster.manager.services.jobs.JobExecutionManager;

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
			
			new Thread(new Runnable() {
				public void run() {
					log.debug("[UnboundedJobService] Starting Processsor");
					UnboundedJobProcessor processor = new UnboundedJobProcessor(profile);
					profile.setCancelCallback(processor);
					processor.start();
				}
			}).start();
			
			return true;
		}
		else {
			return false;
		}
	}




}
