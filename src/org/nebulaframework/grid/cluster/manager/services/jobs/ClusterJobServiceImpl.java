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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.ResultCallback;
import org.nebulaframework.core.job.SplitAggregateGridJob;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.deploy.GridJobInfo;
import org.nebulaframework.core.job.exceptions.GridJobPermissionDeniedException;
import org.nebulaframework.core.job.exceptions.GridJobRejectionException;
import org.nebulaframework.core.job.future.GridJobFutureServerImpl;
import org.nebulaframework.core.job.unbounded.UnboundedGridJob;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.manager.services.jobs.aggregator.AggregatorService;
import org.nebulaframework.grid.cluster.manager.services.jobs.remote.RemoteClusterJobService;
import org.nebulaframework.grid.cluster.manager.services.jobs.splitter.SplitterService;
import org.nebulaframework.grid.cluster.manager.services.jobs.unbounded.UnboundedJobService;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;
import org.nebulaframework.util.hashing.SHA1Generator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

/**
 * Default implementation of {@code ClusterJobService}. This class allows
 * {@code GridNode}s to submit {@code GridJob}s to the {@code ClusterManager},
 * and also handles the execution of the {@code GridJobs}, with the support of
 * {@code SplitterService} and {@code AggregatorService}.
 * <p>
 * <i>Spring Managed</i>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClusterJobService
 * @see SplitterService
 * @see AggregatorService
 * @see JobServiceJmsSupport
 * 
 */
public class ClusterJobServiceImpl implements ClusterJobService,
		InternalClusterJobService {

	private static Log log = LogFactory.getLog(ClusterJobServiceImpl.class);

	private ClusterManager cluster;
	private JobServiceJmsSupport jmsSupport;

	
	private SplitterService splitterService;
	private AggregatorService aggregatorService;
	private UnboundedJobService unboundedService;
	
	private RemoteClusterJobService remoteJobServiceProxy;

	
	// Holds GridJobProfiles of all active GridJobs, against its JobId
	// A LinkedHashMap is used to ensure insertion order iteration
	private Map<String, GridJobProfile> jobs = new LinkedHashMap<String, GridJobProfile>();

	/**
	 * Instantiates a ClusterJobServiceImpl for the given {@code ClusterManager}
	 * instance.
	 * 
	 * @param cluster
	 *            Owner {@code ClusterManager}
	 */
	public ClusterJobServiceImpl(ClusterManager cluster) {
		super();
		this.cluster = cluster;
	}

	/**
	 * Implementation of {@link ClusterJobService#submitJob(UUID, GridJob).
	 * <p>
	 * {@inheritDoc}
	 */
	public String submitJob(UUID owner, GridJob<?, ?> job)
			throws GridJobRejectionException {
		// Delegate to overloaded version
		return submitJob(owner, job, null, null);
	}

	/**
	 * Implementation of
	 * {@link ClusterJobService#submitJob(UUID, GridJob, GridArchive)).
	 * <p>
	 * {@inheritDoc}
	 */
	public String submitJob(UUID owner, GridJob<?, ?> job, GridArchive archive)
			throws GridJobRejectionException {
		return submitJob(owner, job, archive, null);
	}

	// TODO FixDoc
	public String submitJob(UUID owner, GridJob<?, ?> job,
			String resultCallbackQueue) throws GridJobRejectionException {
		return submitJob(owner, job, null, resultCallbackQueue);
	}

	// TODO FixDoc	
	public String submitJob(UUID owner, GridJob<?, ?> job, GridArchive archive,
			String resultCallbackQueue) throws GridJobRejectionException {

		// Create JobId [ClusterID.OwnerID.RandomUUID]
		String jobId = this.cluster.getClusterId() + "." + owner + "."
				+ UUID.randomUUID();

		// Create Infrastructure - JMS Queues
		jmsSupport.createTaskQueue(jobId);
		jmsSupport.createResultQueue(jobId);
		jmsSupport.createFutureQueue(jobId);

		// Create GridJobFuture, which will be used remotely by
		// owner node to monitor / obtain results
		GridJobFutureServerImpl future = jmsSupport.createFuture(jobId, this);

		// Create GridJobProfile for GridJob
		GridJobProfile profile = new GridJobProfile();
		profile.setJobId(jobId);
		profile.setOwner(owner);
		profile.setJob(job);
		profile.setFuture(future);

		if (resultCallbackQueue != null) {
			ResultCallback proxy = jmsSupport.createResultCallbackProxy(jobId, resultCallbackQueue);
			profile.setResultCallback(proxy);
		}
		
		if (archive != null) {
			// If Job has a GridArchive, verify integrity
			if (!verifyArchive(archive))
				throw new GridJobRejectionException(
						"Archive verification failed");

			// Put Archive into GridJobProfile
			profile.setArchive(archive);
		}

		synchronized (this) {
			// Insert GridJob to active jobs map
			this.jobs.put(jobId, profile);
		}

		
		
		if (job instanceof SplitAggregateGridJob<?, ?>) {
			
			// Allow Final Results
			profile.getFuture().setFinalResultSupported(true);
			
			// Start Split Aggregate Job
			startSplitAggregateJob(profile);
			
		} else if (job instanceof UnboundedGridJob<?, ?>) {
			
			// Disallow Final Results
			profile.getFuture().setFinalResultSupported(false);
			
			// Start Unbounded Job
			startUnboundedJob(profile);

		} else {
			// Unsupported Type
			new AssertionError("Unsupported GridJob Type");
		}

		// Notify Job Start to Workers
		notifyJobStart(jobId);

		
		return jobId;
	}


	// TODO FixDoc
	private void startSplitAggregateJob(GridJobProfile profile) {
		// Start Splitter & Aggregator for GridJob
		splitterService.startSplitter(profile);
		aggregatorService.startAggregator(profile);

	}

	// TODO FixDoc
	private void startUnboundedJob(GridJobProfile profile) {
		unboundedService.startJobProcessing(profile);
	}

	/**
	 * Verifies the {@code GridArchive} by comparing its provided SHA1 Hash
	 * against generated SHA1 Hash for the bytes of the GridArchive.
	 * 
	 * @param archive
	 *            {@code GridArchive} to be verified
	 * @return if success {@code true}, otherwise {@code false}
	 */
	private boolean verifyArchive(GridArchive archive) {
		// Try to compare SHA1 Digests for bytes
		return SHA1Generator.generateAsString(archive.getBytes())
				.equals(archive.getHash());
	}

	/**
	 * Implementation of {@link ClusterJobService#requestJob(String)}.
	 * <p>
	 * {@inheritDoc}
	 */
	// FIXME currently allows all nodes to participate
	public GridJobInfo requestJob(String jobId)
			throws GridJobPermissionDeniedException, IllegalArgumentException {

		if (isRemoteClusterJob(jobId)) {
			log.debug("[ClusterJobService] Remote Job Request {" + jobId + "}");
			return remoteJobServiceProxy.remoteJobRequest(jobId);
		}

		log.debug("[ClusterJobService] Local Job Request {" + jobId + "}");

		try {
			// Get Profile
			GridJobProfile profile = jobs.get(jobId);

			// If no Job found
			if (profile == null) {
				log.debug("[ClusterJobService] JobId " + jobId
						+ " not in Jobs Collection of Cluster");
				throw new NullPointerException("Job Not Found");
			}

			// Return GridJobInfo for Profile
			return createInfo(profile);

		} catch (NullPointerException ex) {
			throw new IllegalArgumentException("Invalid GridJob Id " + jobId);
		} catch (Exception e) {
			throw new GridJobPermissionDeniedException(
					"Permission denied due to exception", e);
		}
	}

	/**
	 * Returns {@code true} if the passed JobId indicates a remote
	 * {@code GridJob}, that is, a {@code GridJob} of another Cluster. This
	 * method parses the given {@code JobId} to extract the ClusterID portion of
	 * it to identify the originating cluster.
	 * 
	 * @param jobId
	 *            JobId to check
	 * @return true if remote job, false otherwise
	 * @throws IllegalArgumentException
	 *             if {@code jobId} is not valid
	 */
	private boolean isRemoteClusterJob(String jobId)
			throws IllegalArgumentException {

		String jobClusterId = jobId.split("\\.")[0];
		return !(this.cluster.getClusterId().equals(UUID
				.fromString(jobClusterId)));
	}

	/**
	 * {@inheritDoc}
	 */
	public GridJobInfo requestNextJob() throws GridJobPermissionDeniedException {

		try {
			// Find the next available Job
			GridJobProfile profile = findNextJob();

			// If job is available, return profile, or else null
			return (profile != null) ? createInfo(profile) : null;
		} catch (Exception e) {
			throw new GridJobPermissionDeniedException(
					"Permission denied due to exception", e);
		}
	}

	/**
	 * Creates and returns the {@code GridJobInfo} instance for a
	 * {@code GridJob}, denoted by the {@code GridJobProfile}.
	 * 
	 * @param profile
	 *            {@code GridJobProfile} for Job
	 * 
	 * @return The {@code GridJobInfo} for the Job
	 */
	protected GridJobInfo createInfo(GridJobProfile profile) {

		// Check for Nulls
		Assert.notNull(profile);

		GridJobInfo info = new GridJobInfo(profile.getJobId());

		if (profile.isArchived()) {
			// If Archived Job, include Archive
			info.setArchive(profile.getArchive());
		}
		return info;
	}

	/**
	 * Finds and returns the {@code GridJobProfile} for next available Job.
	 * <p>
	 * The current implementation returns the oldest active {@code GridJob},
	 * using the backing data structure implementation - {@link LinkedHashMap},
	 * which ensures insertion order iteration of elements.
	 * 
	 * @return next {@code GridJob}'s {@code GridJobProfile}, or {@code null}
	 *         if no {@code GridJob} exists.
	 */
	protected GridJobProfile findNextJob() {
		return (jobs.size() > 0) ? jobs.values().iterator().next() : null;
	}

	/**
	 * Cancels execution of the given {@code GridJob} on the Grid.
	 * 
	 * @param jobId JobId of the GridJob to be canceled
	 * @return a {@code boolean} value indicating success ({@code true}) / failure ({@code false}).
	 */
	public boolean cancelJob(String jobId) throws IllegalArgumentException {
		
		// Check JobId
		if (! this.jobs.containsKey(jobId)) {
			throw new IllegalArgumentException("Invalid JobId, not an active Job of this Cluster");
		}

		
		GridJobProfile profile = jobs.get(jobId);
		
		log.debug("[ClusterJobService] Cancelling Job : {"+profile.getJobId()+"}");
		
		notifyJobCancel(jobId);
		return profile.cancel();
	}
	
	/**
	 * Notifies that a Job has started to all nodes in this cluster.
	 * 
	 * @param jobId
	 *            JobId of started Job.
	 */
	protected void notifyJobStart(String jobId) {

		// Create ServiceMessage for Job Start Notification
		ServiceMessage message = new ServiceMessage(jobId,
				ServiceMessageType.JOB_START);

		// Send ServiceMessage to GridNodes
		cluster.getServiceMessageSender().sendServiceMessage(message);
		log.debug("[ClusterJobService] Notified Job Start {" + jobId + "}");
	}

	/**
	 * Notifies to GridNodes that a particular GridJob has finished execution.
	 * 
	 * @param jobId
	 *            JobId of the finished GridJob
	 */
	public void notifyJobEnd(String jobId) {
		try {
			// Create ServiceMessage for Job End Notification
			ServiceMessage message = new ServiceMessage(jobId,
					ServiceMessageType.JOB_END);

			// Send ServiceMessage to GridNodes
			cluster.getServiceMessageSender().sendServiceMessage(message);
			log.debug("[ClusterJobService] Notified Job End {" + jobId + "}");
		} finally {
			// Remove GridJob from Active GridJobs map
			removeJob(jobId);
		}
	}

	/**
	 * Notifies to GridNodes that a particular GridJob has been canceled.
	 * 
	 * @param jobId
	 *            JobId of the canceled GridJob.
	 */
	public void notifyJobCancel(String jobId) {
		try {
			// Create ServiceMessage for Job Cancel Notification
			ServiceMessage message = new ServiceMessage(jobId,
					ServiceMessageType.JOB_CANCEL);

			// Send ServiceMessage to GridNodes
			cluster.getServiceMessageSender().sendServiceMessage(message);
			log
					.debug("[ClusterJobService] Notified Job Cancel {" + jobId
							+ "}");
		} finally {
			// Remove GridJob from Active GridJobs map
			removeJob(jobId);
		}
	}

	/**
	 * Removes a given {@code GridJob} from the active GridJobs collection of
	 * this service.
	 * 
	 * @param jobId
	 *            JobId of the GridJob to remove from collection
	 */
	protected synchronized void removeJob(String jobId) {
		this.jobs.remove(jobId);
	}

	/**
	 * Returns the {@code GridJobProfile} for a given {@code GridJob}.
	 * 
	 * @param jobId
	 *            JobId of the {@code GridJob}
	 * @return {@code GridJobProfile} for the specified {@code GridJob}.
	 */
	public synchronized GridJobProfile getProfile(String jobId) {
		return jobs.get(jobId);
	}

	/**
	 * Returns a {@code boolean} value indicating whether a given JobId refers
	 * to an active {@code GridJob} of this service instance.
	 * 
	 * @param jobId
	 *            JobId of the {@code GridJob}
	 * 
	 * @return {@code true} if the {@code GridJob} is active, {@code false}
	 *         otherwise.
	 */
	public synchronized boolean isActiveJob(String jobId) {
		return this.jobs.containsKey(jobId);
	}

	/**
	 * Sets the {@code JobServiceJmsSupport} instance for this service.
	 * <p>
	 * {@code JobServicesJmsSupport} provides support methods which handles JMS
	 * specific activities in Job handling, such as creation of JMS
	 * {@code Queues}, etc.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param jmsSupport
	 *            {@code JobServiceJmsSupport} instance
	 */
	@Required
	public void setJmsSupport(JobServiceJmsSupport jmsSupport) {
		this.jmsSupport = jmsSupport;
	}

	// TODO FixDoc
	@Required
	public void setRemoteJobServiceProxy(
			RemoteClusterJobService remoteJobServiceProxy) {
		this.remoteJobServiceProxy = remoteJobServiceProxy;
	}

	/**
	 * Returns the {@code SplitterService} used by the
	 * {@code ClusterJobServiceImpl}.
	 * <p>
	 * {@code SplitterService} is responsible for splitting a given
	 * {@code GridJob} into {@code GridTask}s which are to be executed
	 * remotely.
	 * 
	 * @return {@code SplitterService} reference.
	 */
	public SplitterService getSplitterService() {
		return splitterService;
	}

	/**
	 * Sets the {@code SplitterService} used by the
	 * {@code ClusterJobServiceImpl}.
	 * <p>
	 * {@code SplitterService} is responsible for splitting a given
	 * {@code GridJob} into {@code GridTask}s which are to be executed
	 * remotely.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param splitterService
	 *            SplitterService for the {@code ClusterJobServiceImpl}
	 */
	@Required
	public void setSplitterService(SplitterService splitterService) {
		this.splitterService = splitterService;
	}

	/**
	 * Returns the {@code AggregatorService} used by the
	 * {@code ClusterJobServiceImpl}.
	 * <p>
	 * {@code AggregatorService} is responsible for collecting results returned
	 * by each {@code GridTask} which was executed on a remote node, and to
	 * aggregate the results to provide the final result for the {@code GridJob}.
	 * 
	 * @return {@code AggregatorService} reference.
	 */
	public AggregatorService getAggregatorService() {
		return aggregatorService;
	}

	/**
	 * Returns the {@code AggregatorService} used by the
	 * {@code ClusterJobServiceImpl}.
	 * <p>
	 * {@code AggregatorService} is responsible for collecting results returned
	 * by each {@code GridTask} which was executed on a remote node, and to
	 * aggregate the results to provide the final result for the {@code GridJob}.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param aggregatorService
	 *            {@code AggregatorService} for the service
	 */
	@Required
	public void setAggregatorService(AggregatorService aggregatorService) {
		this.aggregatorService = aggregatorService;
	}

	/**
	 * Sets the {@code UnboundedJobService} used by the Job Service 
	 * Implementation. {@code UnboundedJobService} is responsible for 
	 * managing the execution of {@link UnboundedGridJob}s.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * @param unboundedService UnboudedJobService
	 */
	@Required
	public void setUnboundedService(UnboundedJobService unboundedService) {
		this.unboundedService = unboundedService;
	}

	
}
