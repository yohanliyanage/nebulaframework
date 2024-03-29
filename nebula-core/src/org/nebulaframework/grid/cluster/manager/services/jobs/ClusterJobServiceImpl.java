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

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.ResultCallback;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.deploy.GridJobInfo;
import org.nebulaframework.core.job.exceptions.GridJobPermissionDeniedException;
import org.nebulaframework.core.job.exceptions.GridJobRejectionException;
import org.nebulaframework.core.job.future.GridJobFutureServerImpl;
import org.nebulaframework.deployment.classloading.GridArchiveClassLoader;
import org.nebulaframework.deployment.classloading.GridNodeClassLoader;
import org.nebulaframework.deployment.classloading.service.ClassLoadingService;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.manager.services.jobs.remote.RemoteClusterJobService;
import org.nebulaframework.grid.cluster.manager.services.jobs.splitaggregate.AggregatorService;
import org.nebulaframework.grid.cluster.manager.services.jobs.splitaggregate.SplitterService;
import org.nebulaframework.grid.cluster.node.GridNodeProfile;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;
import org.nebulaframework.util.hashing.SHA1Generator;
import org.nebulaframework.util.io.IOSupport;
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
	private static int finished = 0;

	private ClusterManager cluster;
	private JobServiceJmsSupport jmsSupport;

	@SuppressWarnings("unchecked")
	private Map<Class<? extends GridJob>, JobExecutionManager> executors = new HashMap<Class<? extends GridJob>, JobExecutionManager>();

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
	 * Registers a {@link JobExecutionManager} with this JobExecutionService,
	 * which is capable of handling {@code GridJob}s of type {@code clazz}.
	 * 
	 * @param clazz
	 *            Type of GridJob Class
	 * @param manager
	 *            JobExecutionManager
	 */

	public void setExecutors(JobExecutionManager[] managers) {
		for (JobExecutionManager manager : managers) {
			executors.put(manager.getInterface(), manager);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String submitJob(UUID owner, String className, byte[] classData)
			throws GridJobRejectionException {
		// Delegate to overloaded version
		return submitJob(owner, className, classData, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String submitJob(UUID owner, String className, byte[] classData,
			GridArchive archive) throws GridJobRejectionException {
		return submitJob(owner, className, classData, archive, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String submitJob(UUID owner, String className, byte[] classData,
			String resultCallbackQueue) throws GridJobRejectionException {
		return submitJob(owner, className, classData, null, resultCallbackQueue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String submitJob(final UUID owner, final String className,
			final byte[] classData, GridArchive archive,
			String resultCallbackQueue) throws GridJobRejectionException {

		// Create JobId [ClusterID.OwnerID.RandomUUID]
		final String jobId = this.cluster.getClusterId() + "." + owner + "."
				+ UUID.randomUUID();

		// Create Infrastructure - JMS Queues
		jmsSupport.createTaskQueue(jobId);
		jmsSupport.createResultQueue(jobId);
		jmsSupport.createFutureQueue(jobId);

		// Create GridJobFuture, which will be used remotely by
		// owner node to monitor / obtain results
		final GridJobFutureServerImpl future = jmsSupport.createFuture(jobId, this);

		// Create GridJobProfile for GridJob
		final GridJobProfile profile = new GridJobProfile();
		profile.setJobId(jobId);
		profile.setOwner(owner);
		profile.setFuture(future);

		ClassLoader classLoader = null;
		if (archive!=null) {
			classLoader = createArchiveClassLoader(archive, owner);
		}
		else {
			classLoader = createNodeClassLoader(owner);
		}
		try {
			// Deserialize and retrieve GridJob
			profile.setJob(getGridJobInstance(owner, classData, archive, classLoader));
		} catch (Exception e) {
			log.warn("[JobService] Unable to de-serialize Job", e);
			throw new GridJobRejectionException("Unable to de-serialize Job", e);
		}

		synchronized (this) {
			// Insert GridJob to active jobs map
			this.jobs.put(jobId, profile);
		}

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

		if (!startGridJob(profile, classLoader)) {
			// Unsupported Type
			throw new GridJobRejectionException("GridJob Type Not Supported : "
					+ profile.getJob().getClass().getName());
		}

		// Track to see if Job Submitter Node Fails
		stopIfNodeFails(owner, jobId);

		// Notify Job Start to Workers
		notifyJobStart(jobId);

		return jobId;
	}

	/**
	 * De-serializes and returns the {@link GridJob} instance, from the byte[].
	 * 
	 * @param owner
	 *            Owner Node (for ClassLoading)
	 * @param classData
	 *            Serialized data
	 * @param archive
	 *            GridArchive, can be null
	 * 
	 * @return GridJob instance
	 * 
	 * @throws IOException
	 *             if occurred while processing
	 * @throws ClassNotFoundException
	 *             if a required class definition is missing
	 */
	private GridJob<?, ?> getGridJobInstance(final UUID owner,
			final byte[] classData, final GridArchive archive, ClassLoader cl)
			throws IOException, ClassNotFoundException {

		if (archive != null) {
			// Read from Archive if Archived GridJob
			return IOSupport.deserializeFromBytes(classData, cl);
		} else {
			// Read from GridNodeClassLoader
			return (GridJob<?, ?>) IOSupport
					.deserializeFromBytes(classData, cl);
		}
	}

	/**
	 * Creates a {@link GridNodeClassLoader} to remotely load necessary class
	 * definitions.
	 * 
	 * @param owner
	 *            Owner GridNode to load classes from
	 * 
	 * @return ClassLoader instance
	 */
	protected ClassLoader createNodeClassLoader(final UUID owner) {
		return AccessController
				.doPrivileged(new PrivilegedAction<ClassLoader>() {

					@Override
					public ClassLoader run() {
						ClassLoadingService service = ClusterManager
								.getInstance().getClassLoadingService();
						return new GridNodeClassLoader(owner, service);
					}

				});
	}

	protected ClassLoader createArchiveClassLoader(final GridArchive archive,
			final UUID owner) {

		return AccessController
				.doPrivileged(new PrivilegedAction<ClassLoader>() {

					@Override
					public ClassLoader run() {
						return new GridArchiveClassLoader(archive,
								createNodeClassLoader(owner));
					}

				});
	}

	/**
	 * Starts the given GridJob on the Grid. This method attempts to find a
	 * proper {@link JobExecutionManager} for the job, and if found, it
	 * delegates to {@link #startExecution(JobExecutionManager, GridJobProfile)}
	 * method.
	 * 
	 * @param profile
	 *            GridJobProfile for the job
	 * @return boolean indicating result
	 */
	private boolean startGridJob(GridJobProfile profile, ClassLoader cl) {

		GridJob<?, ?> job = profile.getJob();

		/* -- Check for direct implementation -- */

		// Get all implemented interfaces of GridJob
		Class<?>[] ifaces = job.getClass().getInterfaces();

		for (Class<?> clazz : ifaces) {

			// If we have a executor for the interface
			if (executors.containsKey(clazz)) {

				// Attempt to Start GridJob
				boolean started = startExecution(executors.get(clazz), profile, cl);

				// If Success
				if (started) {
					return true;
				}
			}
		}

		/* -- Check for indirect implementation -- */
		if (findJobClass(job.getClass(), profile, cl)) {
			return true;
		}

		log.error("[JobService Unable to Find JobManager for Job Type "
				+ job.getClass().getName());

		// No JobExecutionManager for Job
		return false;
	}

	public boolean findJobClass(Class<?> c, GridJobProfile profile, ClassLoader cl) {

		// If we have a executor for the interface
		if (executors.containsKey(c)) {

			// Attempt to Start GridJob
			boolean started = startExecution(executors.get(c), profile, cl);

			// If Success
			if (started) {
				return true;
			}

			log.debug("Unable to Start GridJobManager for GridJob Type "
					+ c.getName());
			return false;
		}

		if (c.getSuperclass() != null) {
			if (findJobClass(c.getSuperclass(), profile, cl)) {
				return true;
			}
		}

		for (Class<?> clazz : c.getInterfaces()) {
			if (findJobClass(clazz, profile, cl)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Starts execution of the given job, using the specified
	 * {@link JobExecutionManager}.
	 * 
	 * @param jobExecutionManager
	 *            Job execution manager for the job type
	 * @param profile
	 *            profile of the GridJob
	 * 
	 * @return boolean indicating success / failure
	 */
	private boolean startExecution(JobExecutionManager jobExecutionManager,
			GridJobProfile profile, ClassLoader cl) {
		return jobExecutionManager.startExecution(profile, cl);
	}

	/**
	 * Stops execution of the given {@code GridJob} if the specified
	 * {@code GridNode} fails or leaves the Grid.
	 * 
	 * @param nodeId
	 *            {@code GridNode} Id
	 * @param jobId
	 *            {@code GridJob} Id
	 */
	private void stopIfNodeFails(UUID nodeId, final String jobId) {

		ServiceEventsSupport.addServiceHook(
			new ServiceHookCallback() {
				public void onServiceEvent(
						ServiceMessage message) {
					try {
						cancelJob(jobId);
					} catch (IllegalArgumentException e) {
						// Job Already Stopped
					}
				}
			}, nodeId.toString(), 
			ServiceMessageType.HEARTBEAT_FAILED,
			ServiceMessageType.NODE_UNREGISTERED);
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
	public GridJobInfo requestJob(String jobId, GridNodeProfile nodeProfile)
			throws GridJobPermissionDeniedException, IllegalArgumentException {

		if (isRemoteClusterJob(jobId)) {
			log.debug("[ClusterJobService] Remote Job Request {" + jobId + "}");
			return remoteJobServiceProxy.remoteJobRequest(jobId, nodeProfile);
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

			if (!profile.processRequest(nodeProfile)) {
				throw new GridJobPermissionDeniedException("Permission Denied");
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
	public GridJobInfo requestNextJob(GridNodeProfile nodeProfile) {

		GridJobProfile profile = null;

		// For each job, try to get permission
		for (GridJobProfile p : jobs.values()) {

			// If Allowed to Participate
			if (p.processRequest(nodeProfile)) {
				profile = p;
				break;
			}
		}

		// If job is available, return profile, or else null
		return (profile != null) ? createInfo(profile) : null;
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

		GridJobInfo info = new GridJobInfo(profile.getJobId(), profile.getJob()
				.getClass().getSimpleName());

		if (profile.isArchived()) {
			// If Archived Job, include Archive
			info.setArchive(profile.getArchive());
		}
		return info;
	}

	/**
	 * Cancels execution of the given {@code GridJob} on the Grid.
	 * 
	 * @param jobId
	 *            JobId of the GridJob to be canceled
	 * @return a {@code boolean} value indicating success ({@code true}) /
	 *         failure ({@code false}).
	 */
	public boolean cancelJob(String jobId) throws IllegalArgumentException {

		// Check JobId
		if (!this.jobs.containsKey(jobId)) {
			throw new IllegalArgumentException(
					"Invalid JobId, not an active Job of this Cluster");
		}

		GridJobProfile profile = jobs.get(jobId);

		log.debug("[ClusterJobService] Cancelling Job : {" + profile.getJobId()
				+ "}");

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

		log.info("[JobService] Starting GridJob " + jobId);

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

		finished++;

		// Remove GridJob from Active GridJobs map
		removeJob(jobId);

		// Create ServiceMessage for Job End Notification
		ServiceMessage message = new ServiceMessage(jobId,
				ServiceMessageType.JOB_END);

		// Send ServiceMessage to GridNodes
		cluster.getServiceMessageSender().sendServiceMessage(message);
		log.debug("[ClusterJobService] Notified Job End {" + jobId + "}");

		log.info("[JobService] Finished GridJob " + jobId);

	}

	/**
	 * Notifies to GridNodes that a particular GridJob has been canceled.
	 * 
	 * @param jobId
	 *            JobId of the canceled GridJob.
	 */
	public void notifyJobCancel(String jobId) {

		finished++;
		// Remove GridJob from Active GridJobs map
		removeJob(jobId);

		// Create ServiceMessage for Job Cancel Notification
		ServiceMessage message = new ServiceMessage(jobId,
				ServiceMessageType.JOB_CANCEL);

		// Send ServiceMessage to GridNodes
		cluster.getServiceMessageSender().sendServiceMessage(message);
		log.debug("[ClusterJobService] Notified Job Cancel {" + jobId + "}");

		log.info("[JobService] Cancelled GridJob " + jobId);

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
		if (jobs.containsKey(jobId)) {
			return jobs.get(jobId);
		} else {
			throw new IllegalArgumentException("GridJob does not exist "
					+ jobId);
		}
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

	/**
	 * Sets the {@code RemoteClusterJobService} proxy to be used by the
	 * {@code ClusterManager}.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param remoteJobServiceProxy
	 *            proxy
	 */
	@Required
	public void setRemoteJobServiceProxy(
			RemoteClusterJobService remoteJobServiceProxy) {
		this.remoteJobServiceProxy = remoteJobServiceProxy;
	}

	/**
	 * Returns the number of jobs which have finished execution on this Cluster.
	 * 
	 * @return finished job count
	 */
	public int getFinishedJobCount() {
		return finished;
	}

	/**
	 * Returns the number of currently active GridJobs on this Cluster.
	 * 
	 * @return active job count
	 */
	public int getActiveJobCount() {
		return jobs.size();
	}

}
