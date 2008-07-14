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

package org.nebulaframework.grid.cluster.node.services.job.execution;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.deploy.GridJobInfo;
import org.nebulaframework.core.job.exceptions.GridJobPermissionDeniedException;
import org.nebulaframework.core.service.message.ServiceMessage;
import org.nebulaframework.core.service.message.ServiceMessageType;
import org.nebulaframework.deployment.classloading.GridArchiveClassLoader;
import org.nebulaframework.deployment.classloading.GridNodeClassLoader;
import org.nebulaframework.deployment.classloading.node.exporter.GridNodeClassExporter;
import org.nebulaframework.deployment.classloading.service.ClassLoadingService;
import org.nebulaframework.deployment.classloading.service.ClassLoadingServiceSupport;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

/**
 * Implementation of {@link JobExecutionService}. This class is responsible of
 * joining new jobs, and executing tasks from joined projects, at a
 * {@code GridNode}.
 * <p>
 * The {@link #onServiceMessage(ServiceMessage)} method is invoked when a
 * {@code GridJob} related {@code ServiceMessage} is arrived. This information
 * is used to detect new jobs, and also to identify termination of existing
 * jobs. Once a new job is available, the {@code JobExecutionServiceImpl}
 * attempts to register for the job and if succeeded, it starts task execution
 * of the job, after the necessary resources are initialized (such as
 * downloading {@code GridArchive}, if needed).
 * <p>
 * The implementation relies on {@link TaskExecutor} to carry out the execution
 * of tasks, once registered for a {@code GridJob} at the {@code ClusterManager}.
 * <p>
 * Furthermore, {@code JobExecutionServiceImpl} also maintains a
 * {@code ClassLoadingService}, which is a service proxy to the
 * {@link ClassLoadingService} of {@code ClusterManager}, which is used to load
 * classes from remote nodes, specifically, the job submitter node. For more
 * information regarding the class loading strategy, refer to
 * {@link ClassLoadingService}, {@link GridNodeClassExporter},
 * {@link GridNodeClassLoader}, {@link GridArchiveClassLoader}.
 * <p>
 * <i>Spring Managed</i>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see JobExecutionService
 * @see GridNode
 * @see TaskExecutor
 * @see ClassLoadingService
 */
public class JobExecutionServiceImpl implements JobExecutionService, InitializingBean {

	private static Log log = LogFactory.getLog(JobExecutionServiceImpl.class);

	private boolean idle = true; // Idle = No active Job
	private String currentJobId; // JobId, if exists

	private GridNode node; // Owner Node
	private ConnectionFactory connectionFactory; // JMS ConnectionFactory
	private ClassLoadingService classLoadingService; // Service Proxy

	/**
	 * Constructs a {@code JobExecutionServiceImpl} for the given
	 * {@code GridNode}.
	 * 
	 * @param node
	 *            {@code GridNode} owner
	 */
	public JobExecutionServiceImpl(GridNode node) {
		super();
		this.node = node;
	}

	/**
	 * Sets the JMS ConnectionFactory used by this class to communicate with
	 * remote {@code ClusterManager}.
	 * <p>
	 * This reference is passed to {@code TaskExecutor}, which in turn uses it
	 * to create listener for {@code TaskQueue} and also to write results to the
	 * {@code ResultQueue}.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param connectionFactory
	 *            JMS {@code ConnectionFactory}
	 */
	@Required
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Delegates to handler methods for each type of notification.
	 */
	public void onServiceMessage(ServiceMessage message) {

		if (message.getType().equals(ServiceMessageType.JOB_START)) {
			// New Job
			newJob(message.getMessage());
		} else if (message.getType().equals(ServiceMessageType.JOB_END)) {
			// Job Finished
			endJob(message.getMessage());
		} else if (message.getType().equals(ServiceMessageType.JOB_CANCEL)) {
			// Job Terminated
			terminateJob(message.getMessage());
		}
	}

	/**
	 * Handler method for new job notifications. The method attempts to register
	 * for the new job, if this {@code GridNode} is in {@code idle} state.
	 * Otherwise, it ignores the notification.
	 * <p>
	 * If registration is successful, it starts {@code TaskExecutor} instance
	 * for the {@code GridJob}.
	 * 
	 * @param jobId JobId of new Job
	 */
	protected synchronized void newJob(String jobId) {

		// Initialize the ClassLoading Service
		if (!initalizeService()) {
			log.debug("[JobExecution] Aborting Request as ClassLoadingService not initialized");
			return;
		}

		if (idle) {
			// Request for Job
			try {
				GridJobInfo jobInfo = node.getServicesFacade().requestJob(jobId);

				// Start it
				if (jobInfo.isArchived()) {	// Archived Job
					startNewArchivedJob(jobId, jobInfo.getArchive());
				} else {					// Normal Job
					startNewJob(jobId);
				}

			} catch (GridJobPermissionDeniedException e) {
				// Permission Denied
				log.warn("[JobExecution] Permission Denied for Job {" + jobId + "}");
			} catch (Exception e) {
				log.warn("[JobExecution] Exception", e);
			}
		} else {
			log.debug("[JobExecution] BUSY : Ignored New Job {"+ jobId + "}");
			return;
		}
	}

	protected synchronized void requestNextJob() {
		
		// Initialize the ClassLoading Service
		if (!initalizeService()) {
			log.debug("[JobExecution] Aborting Request as ClassLoadingService not initialized");
			return;
		}
		
		if (idle) {
			// Request for Job
			try {
				GridJobInfo jobInfo = node.getServicesFacade().requestNextJob();

				// If no job, do nothing
				if (jobInfo == null) {
					log.info("[JobExecution] Idle as no active GridJobs");
					return;
				}
				
				// Start it
				if (jobInfo.isArchived()) {	// Archived Job
					startNewArchivedJob(jobInfo.getJobId(), jobInfo.getArchive());
				} else {					// Normal Job
					startNewJob(jobInfo.getJobId());
				}

			} catch (GridJobPermissionDeniedException e) {
				// Permission Denied
				log.warn("[JobExecution] Permission Denied for Request Next Available Job");
			}
		} 
	}
	
	/**
	 * Starts {@code TaskExecutor} for the given <i>non-archived</i> Job.
	 * This type of {@code GridJob}s rely on Node-based Class Loading,
	 * if the classes are not available locally (more likely).
	 * 
	 * @param jobId JobId of new Job
	 */
	protected synchronized void startNewJob(String jobId) {

		log.info("[JobExecution] Starting Non-Archive Job {" + jobId + "}");

		// Update state
		this.idle = false;
		this.currentJobId = jobId;

		// Start TaskExecutor
		TaskExecutor.startForJob(jobId, node, connectionFactory,
									classLoadingService, null);
	}

	/**
	 * Starts {@code TaskExecutor} for the given <i>archived</i> Job.
	 * This type of {@code GridJob}s rely on GridArchive Class Loading.
	 * 
	 * @param jobId JobId of new Job
	 */
	protected synchronized void startNewArchivedJob(String jobId,
			GridArchive archive) {

		log.info("[JobExecution] Starting Archived Job {" + jobId + "}");

		// Update state
		this.idle = false;
		this.currentJobId = jobId;

		// Start TaskExecutor
		TaskExecutor.startForJob(jobId, node, connectionFactory,
									classLoadingService, archive);
	}

	/**
	 * Ends task execution for a specified Job (if executed by this service), 
	 * by stopping the {@code TaskExecutor} instance for the Job. 
	 * <p>
	 * This is invoked after successful completion of a {@code GridJob}.
	 * 
	 * @param jobId JobId of finished {@code GridJob}
	 */
	protected synchronized void endJob(String jobId) {
		
		//If the notification is for current Job
		if ((this.currentJobId != null) && (this.currentJobId.equals(jobId))) {
			
			log.info("[JobExecution] Stopping Job Execution {" + jobId + "}");
			
			// Stop TaskExecutor
			TaskExecutor.stopForJob(jobId);
			
			// Update state
			this.currentJobId = null;
			this.idle = true;
			
			requestNextJob();
			
		} else {	// Log & ignore
			log.debug("[JobExecution] Ignored Job End | N/A {" + jobId + "}");
		}
	}



	/**
	 * Terminates task execution for a specified Job (if executed by this service), 
	 * by stopping the {@code TaskExecutor} instance for the Job. 
	 * <p>
	 * This is invoked on cancellation of a {@code GridJob}.
	 * 
	 * @param jobId JobId of canceled {@code GridJob}
	 */
	protected synchronized void terminateJob(String jobId) {
		
		//If the notification is for current Job
		if ((this.currentJobId != null) && (this.currentJobId.equals(jobId))) {
			
			log.info("[JobExecution] Terminating Job Execution {" + jobId + "}");
			
			// Stop Task Executor
			TaskExecutor.stopForJob(jobId);
			
			// Update state
			this.currentJobId = null;
			this.idle = true;
			
			requestNextJob();
			
		} else { // Log & ignore
			log.debug("[JobExecution] Ignored Job Termination | N/A {" + jobId + "}");
		}
	}

	/**
	 * Returns the Current Job Id for the {@link JobExecutionService}.
	 * May return {@code null} if no active job.
	 * 
	 * @return {@code String} current JobId or {@code null}, if none.
	 */
	public String getCurrentJobId() {
		return currentJobId;
	}

	/**
	 * Returns {@code true} if no active job is on execution, 
	 * or {@code false} otherwise.
	 * 
	 * @return {@code true} if not active, {@code false} otherwise.
	 */
	public synchronized boolean isIdle() {
		return idle;
	}

	/**
	 * Initializes the {@code ClassLoadingService} proxy. This method creates a
	 * proxy object which is used to communicate with the
	 * {@code ClassLoadingService} of the {@code ClusterManager}, if it has not
	 * been already created.
	 * <p>
	 * This method is invoked at initialization of JobExecutionService. However
	 * it is possible that ClassLoadingService cannot be created at this point,
	 * as  <b>it requires the node to be registered with a {@code ClusterManager}</b>. 
	 * {@link #afterPropertiesSet()} method ensures that this gets initialized
	 * properly by repeatedly invoking {@code initializeService()} until it returns
	 * {@code true}, within 2 second intervals.
	 * 
	 * @return if already started or started successfully, {@code true},
	 * otherwise {@code false}.
	 */
	private boolean initalizeService() {
		
		
		// If not initialized before
		if (classLoadingService == null) {
			// Create Service Proxy
			try {
				classLoadingService = ClassLoadingServiceSupport.createProxy(node
						.getNodeRegistrationService().getRegistration()
						.getClusterId(), connectionFactory);
				log.info("[JobExecution] Started ClassLoadingServiceProxy");
				return true;
			} catch (Exception e) {
				log.debug("[JobExecution] Unable to start ClassLoadingServiceProxy");
				return false;
			}
		}
		else {
			return true;
		}
		
	}

	/**
	 * This method ensures that all dependencies of the {@code JobExecutionServiceImpl} 
	 * is set. Also, this method requests the next available Job from Cluster, enabling
	 * the node to start processing once started up.
	 * <p>
	 * Not that this method ensures that the {@code ClassLoadingServiceProxy} gets initialized
	 * properly by repeatedly invoking {@code initializeService()} until it returns
	 * {@code true}, within 2 second intervals.
	 * <p>
	 * <b>Note : </b>In default implementation, this method will be invoked automatically by Spring Container.
	 * If this class is used outside of Spring Container, this method should be invoked explicitly
	 * to initialize the {@code GridNode} properly.
	 * <p>
	 * <i>Spring Invoked</i>
	 * 
	 * @throws Exception if dependencies are not set.
	 */
	public void afterPropertiesSet() throws Exception {
		
		// Check required dependencies
		Assert.notNull(node);
		Assert.notNull(connectionFactory);
		
		// Wait until initialized and request 
		// for next available job
		new Thread(new Runnable() {
			public void run() {
			
				try {
					// Keep track of attempts
					int attempts = 0;
					
					// Initial Delay
					Thread.sleep(2000);
					
					// Check until initialized
					while(!initalizeService()) {
						
						attempts++; // Increment attempts
						
						if (attempts >= 3) {
						// If more than 3 attempts, log Error message
							log.error("[JobExecution] Unable to Start ClassLoadingServiceProxy, Re-trying in 2 seconds");
						}
						
						// Wait and re-attempt
						Thread.sleep(2000);
					}
					
					// Initialized, request Job
					requestNextJob();
					
				} catch (InterruptedException e) {
					log.error(e);
				}
				
			}
		}).start();
	}

}
