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

package org.nebulaframework.grid.cluster.node;

import java.util.UUID;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.deployment.classloading.node.exporter.GridNodeClassExporterSupport;
import org.nebulaframework.grid.ID;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.manager.services.facade.ClusterManagerServicesFacade;
import org.nebulaframework.grid.cluster.node.services.job.execution.JobExecutionService;
import org.nebulaframework.grid.cluster.node.services.job.submission.JobSubmissionService;
import org.nebulaframework.grid.cluster.node.services.message.ServiceMessagesSupport;
import org.nebulaframework.grid.cluster.node.services.registration.NodeRegistrationService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * {@code GridNode} represents a client node with in a Nebula Cluster.
 * This class manages all client-side aspects of Nebula Framework,
 * including Job Submission, Task Execution and other functionalities.
 * <p>
 * In order to facilitate each functionality, {@code GridNode} relies on services,
 * which are as follows:
 * 	<ul>
 * 		<li> {@link NodeRegistrationService} </li>
 * 		<li> {@link JobExecutionService} </li>
 * 		<li> {@link JobSubmissionService} </li>
 * 		<li> {@link ServiceMessagesSupport} (Handles {@code ServiceMessage}s)</li>
 * 		<li> {@link ClusterManagerServicesFacade} (Proxy)</li>
 * 	</ul>
 * <p>
 * <b>Note : </b> This class is managed by Spring Container. If it is required to use 
 * this outside Spring Container, please ensure that the 
 * {@link ClusterManager#afterPropertiesSet()} method is invoked after setting 
 * dependencies of the class. If deployed with in Spring Container, this will be 
 * automatically invoked by Spring itself.</p>
 * <p>
 * <i>Spring Managed</i> 

 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClusterManager
 */
public class GridNode implements InitializingBean{

	private static Log log = LogFactory.getLog(GridNode.class);

	private UUID id;					// Node Id
	private GridNodeProfile profile;	// Holds meta-data about node

	private ConnectionFactory connectionFactory;
	private NodeRegistrationService nodeRegistrationService;
	private ServiceMessagesSupport serviceMessageSupport;
	private ClusterManagerServicesFacade servicesFacade;
	private JobExecutionService jobExecutionService;
	private JobSubmissionService jobSubmissionService;
	
	/**
	 * Constructs a {@code GridNode} with given {@link GridNodeProfile}.
	 */
	public GridNode(GridNodeProfile profile) {
		super();
		this.id = ID.getId();
		this.profile = profile;
		log.debug("Node " + id + " created");
	}

	/**
	 * Returns the NodeId for this {@code GridNode}.
	 * @return {@code UUID} Node Id
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Returns the {@code GridNodeProfile} for this {@GridNode}.
	 * @return
	 */
	public GridNodeProfile getProfile() {
		return profile;
	}

	/**
	 * Returns the {@code NodeRegistrationService} of the {@code GridNode}.
	 * 
	 * @return {@code NodeRegistrationService} registration service
	 */
	public NodeRegistrationService getNodeRegistrationService() {
		return nodeRegistrationService;
	}

	/**
	 * Sets the Returns the {@code NodeRegistrationService} of the {@code GridNode},
	 * which manages the local node registration with a remote {@link ClusterManager}.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param nodeRegistrationService
	 */
	@Required
	public void setNodeRegistrationService(
			NodeRegistrationService nodeRegistrationService) {
		this.nodeRegistrationService = nodeRegistrationService;
	}

	/**
	 * Returns the {@code ServiceMessagesSupport} used by {@code GridNode}. This is exposed
	 * to be used by other services who obtain a reference of {@code GridNode}.
	 * 
	 * @return {@code ServiceMessagesSupport} implementation
	 */
	public ServiceMessagesSupport getServiceMessageSupport() {
		return serviceMessageSupport;
	}

	/**
	 * Sets the {@code ServiceMessagesSupport} used by {@code GridNode}, which listens to
	 * service messages sent by the {@code ClusterManager}.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param serviceMessageSupport {@code ServiceMessagesSupport} implementation
	 */
	@Required
	public void setServiceMessageSupport(
			ServiceMessagesSupport serviceMessageSupport) {
		this.serviceMessageSupport = serviceMessageSupport;
	}

	/**
	 * Returns the {@code ClusterManagerServicesFacade} used by this {@code GridNode}.
	 * This refers to a proxy object which represents the server side remote object.
	 * 
	 * @return {@code ClusterManagerServicesFacade} proxy
	 */
	public ClusterManagerServicesFacade getServicesFacade() {
		return servicesFacade;
	}

	/**
	 * Sets the {@code ClusterManagerServicesFacade} used by this {@code GridNode}.
	 * This should be a proxy for the remote facade of {@code ClusterManager}.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param servicesFacade {@code ClusterManagerServicesFacade} proxy
	 */
	@Required
	public void setServicesFacade(ClusterManagerServicesFacade servicesFacade) {
		this.servicesFacade = servicesFacade;
	}

	/**
	 * Returns the {@code JobExecutionService} used by this {@code GridNode}.
	 * The {@code JobExecutionService} is responsible for registration with 
	 * new jobs and execution of tasks assigned.
	 * 
	 * @return {@code JobExecutionService} implementation
	 */
	public JobExecutionService getJobExecutionService() {
		return jobExecutionService;
	}

	/**
	 * Sets the {@code JobExecutionService} used by this {@code GridNode}.
	 * The {@code JobExecutionService} is responsible for registration with 
	 * new jobs and execution of tasks assigned.
	 * <p>
	 * This may be {@code null} for non-worker GridNodes (job submission only)
	 * <i>Spring Injected</i>
	 * 
	 * @param jobExecutionService {@code JobExecutionService} implementation
	 */
	public void setJobExecutionService(JobExecutionService jobExecutionService) {
		this.jobExecutionService = jobExecutionService;
	}

	/**
	 * Returns the {@code JobSubmissionService} of this {@code GridNode}.
	 * {@code JobSubmissionService} is used by the local node to submit new
	 * {@code GridJob}s to the Grid.
	 * 
	 * @return {@code JobSubmissionService} of the {@code GridNode}.
	 */
	public JobSubmissionService getJobSubmissionService() {
		return jobSubmissionService;
	}

	/**
	 * Sets the {@code JobSubmissionService} of this {@code GridNode}.
	 * {@code JobSubmissionService} is used by the local node to submit new
	 * {@code GridJob}s to the Grid.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param jobSubmissionService {@code JobSubmissionService} implementation.
	 */
	@Required
	public void setJobSubmissionService(JobSubmissionService jobSubmissionService) {
		this.jobSubmissionService = jobSubmissionService;
	}

	/**
	 * Sets the JMS {@code ConnectionFactory} which is utilized by this node to
	 * communicate with the cluster's JMS {@code Broker}.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param connectionFactory JMS {@code ConnectionFactory}
	 */
	@Required
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	/**
	 * This method ensures that all dependencies of the {@code GridNode} is set. 
	 * Also, this method starts {@code GridNodeClassExporter} for the Cluster.
	 * <p>
	 * <b>Note : </b>In default implementation, this method will be invoked automatically by Spring Container.
	 * If this class is used outside of Spring Container, this method should be invoked explicitly
	 * to initialize the {@code GridNode} properly.
	 * <p>
	 * <i>Spring Invoked</i>
	 * 
	 * @throws Exception if dependencies are not set or GridNodeClassExporter fails with Exceptions.
	 */
	public void afterPropertiesSet() throws Exception {
		GridNodeClassExporterSupport.startService(this.id, this.connectionFactory);
	}
	
	public UUID getClusterId() {
		return this.nodeRegistrationService.getRegistration().getClusterId();
	}

	/**
	 * Attempts to shutdown the {@code GridNode}. 
	 * This is the overloaded version of {@link #shutdown(boolean)},
	 * which attempts for a non-forced shutdown.
	 * <p>
	 * If there are any active TaskExecutors, they will be canceled. 
	 * If there are any active {@code GridJob}s submitted by this
	 * {@code GridNode}, this method will fail throwing an
	 * {@code IllegalStateException}.
	 * 
	 *  @throws IllegalStateException if any active {@code GridJob} exists, which is
	 *  submitted by this {@code GridNode}.
	 */
	public void shutdown() throws IllegalStateException {
		shutdown(false, true);
	}
	
	/**
	 * Shutdowns the {@code GridNode}. 
	 * <p>
	 * If there are any active TaskExecutors, they will be canceled.
	 * <p>
	 * In non-forced mode, If there are any active {@code GridJob}s 
	 * submitted by this {@code GridNode}, this method will fail 
	 * throwing an {@code IllegalStateException}.
	 * <p>
	 * In forced mode, any existing {@code GridJob}s will also be 
	 * canceled.
	 * 
	 * @param force boolean indicating whether this is a forced shut down
	 * @param cluster indicates if connection to cluster exists (in failures)
	 * 
	 * 	@throws IllegalStateException In non-forced mode, if any active 
	 * {@code GridJob} exists, which is submitted by this {@code GridNode}.
	 */
	public void shutdown(boolean force, boolean cluster) throws IllegalStateException {
		
		// TODO Check for existing Jobs / Terminate TaskExecutors / Cancle Jobs
		
		if (cluster) {
			log.error("[GridNode] Shutting Down GridNode as ClusterManager has Shutdown");
			System.exit(0);
		}
		else {
			if (nodeRegistrationService.isRegistered()) {
				nodeRegistrationService.unregister();
			}
		}
		
	}
	
}
