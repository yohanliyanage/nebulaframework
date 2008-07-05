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

package org.nebulaframework.core.grid.cluster.manager.services.jobs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.aggregator.AggregatorService;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.splitter.SplitterService;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.support.JobServiceJmsSupport;
import org.nebulaframework.core.grid.cluster.node.GridNode;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.deploy.GridJobInfo;
import org.nebulaframework.core.job.exceptions.GridJobPermissionDeniedException;
import org.nebulaframework.core.job.exceptions.GridJobRejectionException;
import org.nebulaframework.core.job.future.GridJobFutureImpl;
import org.nebulaframework.core.servicemessage.ServiceMessage;
import org.nebulaframework.core.servicemessage.ServiceMessageType;
import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.util.hashing.SHA1Generator;
import org.springframework.beans.factory.annotation.Required;

/**
 * Default implementation of {@link ClusterJobService}. This class allows {@link GridNode}s to
 * submit {@link GridJob}s to the {@link ClusterManager}, and also handles the execution of the
 * <tt>GridJobs</tt>, with the support of {@link SplitterService} and {@link AggregatorService}.
 * 
 * <p><i>Spring Managed</i></p>
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
public class ClusterJobServiceImpl implements ClusterJobService {

	private ClusterManager cluster;				
	private JobServiceJmsSupport jmsSupport;	

	private SplitterService splitterService;		
	private AggregatorService aggregatorService;	

	// Holds GridJobProfiles of all active GridJobs, against its JobId
	private Map<String, GridJobProfile> jobs = new HashMap<String, GridJobProfile>();
	
	/**
	 * Instantiates a ClusterJobServiceImpl for the given {@link ClusterManager} instance.
	 * 
	 * @param cluster Owner <tt>ClusterManager</tt>
	 */
	public ClusterJobServiceImpl(ClusterManager cluster) {
		super();
		this.cluster = cluster;
	}

	/**
	 * {@inheritDoc}
	 */
	public String submitJob(UUID owner, GridJob<? extends Serializable> job) throws GridJobRejectionException {
		// Delegate to overloaded version
		return submitJob(owner, job, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public String submitJob(UUID owner, GridJob<? extends Serializable> job,
			GridArchive archive) throws GridJobRejectionException {
		
		// Create JobId [ClusterID.OwnerID.RandomUUID]
		String jobId = this.cluster.getClusterId() + "." + owner + "."
				+ UUID.randomUUID();


		// Create Infrastructure - JMS Queues
		jmsSupport.createTaskQueue(jobId);
		jmsSupport.createResultQueue(jobId);
		jmsSupport.createFutureQueue(jobId);

		// Create GridJobFuture, which will be used remotely by 
		// owner node to monitor / obtain results
		GridJobFutureImpl future = jmsSupport.createFuture(jobId);


		// Create GridJobProfile for GridJob
		GridJobProfile profile = new GridJobProfile();
		profile.setJobId(jobId);
		profile.setOwner(owner);
		profile.setJob(job);
		profile.setFuture(future);
		
		if (archive!=null) {
			// If Job has a GridArchive, verify integrity
			if (!verifyArchive(archive)) throw new GridJobRejectionException("Archive verification failed");
			
			// Put Archive into GridJobProfile
			profile.setArchive(archive);
		}
		
		synchronized (this) {
			// Insert GridJob to active jobs map
			this.jobs.put(jobId, profile);
		}

		// Start Splitter & Aggregator for GridJob
		splitterService.startSplitter(profile);
		aggregatorService.startAggregator(profile);

		// Notify Job Start to Workers
		notifyJobStart(jobId);

		return jobId;
	}

	/**
	 * Verifies the {@link GridArchive} by comparing its provided SHA1 Hash against 
	 * generated SHA1 Hash for the bytes of the GridArchive.
	 *  
	 * @param archive {@link GridArchive} to be verified
	 * @return <tt>true</tt> if success, <tt>false</tt> otherwise
	 */
	private boolean verifyArchive(GridArchive archive) {
		// Try to compare SHA1 Digests for bytes
		return SHA1Generator.bytesToString(SHA1Generator.generate(archive.getBytes())).equals(archive.getHash());
	}

	/**
	 * {@inheritDoc}
	 */
	public GridJobInfo requestJob(String jobId) throws GridJobPermissionDeniedException {
		
		// FIXME currently allows all nodes to participate
		
		// Create GridJobInfo, which returns Job Information to worker node
		GridJobInfo info = new GridJobInfo(jobId);
		
		try {
			GridJobProfile profile = jobs.get(jobId);
			if (profile.isArchived()) {
				// If Archived Job, include Archive
				info.setArchive(profile.getArchive());
			}
		} catch (Exception e) {
			new GridJobPermissionDeniedException("Permission denied due to exception", e);
		}
		
		return info;
	}


	/**
	 * Notifies that a Job has started to all nodes in this cluster.
	 * 
	 * @param jobId JobId of started Job.
	 */
	protected void notifyJobStart(String jobId) {
		
		// Create ServiceMessage for Job Start Notification
		ServiceMessage message = new ServiceMessage(jobId);
		message.setType(ServiceMessageType.JOB_START);

		// Send ServiceMessage to GridNodes
		cluster.getServiceMessageSender().sendServiceMessage(message);
	}

	/**
	 * Notifies to GridNodes that a particular GridJob has finished execution.
	 * 
	 * @param jobId JobId of the finished GridJob
	 */
	public void notifyJobEnd(String jobId) {
		try {
			// Create ServiceMessage for Job End Notification
			ServiceMessage message = new ServiceMessage(jobId);
			message.setType(ServiceMessageType.JOB_END);

			// Send ServiceMessage to GridNodes
			cluster.getServiceMessageSender().sendServiceMessage(message);
		} finally {
			// Remove GridJob from Active GridJobs map
			removeJob(jobId);
		}
	}

	/**
	 * Notifies to GridNodes that a particular GridJob has been canceled.
	 * 
	 * @param jobId JobId of the canceled GridJob.
	 */
	public void notifyJobCancel(String jobId) {
		try {
			// Create ServiceMessage for Job Cancel Notification
			ServiceMessage message = new ServiceMessage(jobId);
			message.setType(ServiceMessageType.JOB_CANCEL);

			// Send ServiceMessage to GridNodes
			cluster.getServiceMessageSender().sendServiceMessage(message);
		} finally {
			// Remove GridJob from Active GridJobs map
			removeJob(jobId);
		}
	}

	/**
	 * Removes a given {@link GridJob} from the active GridJobs collection of this service.
	 * 
	 * @param jobId JobId of the GridJob to remove from collection
	 */
	protected synchronized void removeJob(String jobId) {
		this.jobs.remove(jobId);
	}

	/**
	 * Returns the {@link GridJobProfile} for a given {@link GridJob}.
	 * 
	 * @param jobId JobId of the {@link GridJob}
	 * @return {@link GridJobProfile} for the specified {@link GridJob}.
	 */
	public synchronized GridJobProfile getProfile(String jobId) {
		return jobs.get(jobId);
	}

	/**
	 * Returns a <tt>boolean</tt> value indicating whether a given JobId refers
	 * to an active {@link GridJob} of this service instance.
	 * 
	 * @param jobId JobId of the {@link GridJob}
	 * 
	 * @return <tt>true</tt> if the {@link GridJob} is active, <tt>false</tt> otherwise.
	 */
	public synchronized boolean isActiveJob(String jobId) {
		return this.jobs.containsKey(jobId);
	}
	
	/**
	 * <p>Sets the {@link JobServiceJmsSupport} instance for this service.</p> 
	 * 
	 * <p><tt>JobServicesJmsSupport</tt> provides support methods which handles 
	 * JMS specific activities in Job handling, such as creation of JMS <tt>Queues</tt>, etc.</p>
	 * 
	 * <p><b>Note :</b>This is a <b>required</b> dependency.</p>
	 * 
	 * <p><i>Spring Injected</i></p>
	 * 
	 * @param jmsSupport {@link JobServiceJmsSupport} instance
	 */
	@Required
	public void setJmsSupport(JobServiceJmsSupport jmsSupport) {
		this.jmsSupport = jmsSupport;
	}

	/**
	 * <p>Returns the {@link SplitterService} used by the <tt>ClusterJobServiceImpl</tt></p>.
	 * 
	 * <p>{@link SplitterService} is responsible for splitting a given {@link GridJob} into
	 * {@link GridTask}s which are to be executed remotely.</p>
	 * 
	 * @return {@link SplitterService} reference.
	 */
	public SplitterService getSplitterService() {
		return splitterService;
	}

	/**
	 * <p>Sets the {@link SplitterService} used by the <tt>ClusterJobServiceImpl</tt></p>.
	 * 
	 * <p>{@link SplitterService} is responsible for splitting a given {@link GridJob} into
	 * {@link GridTask}s which are to be executed remotely.</p>
	 * 
	 * <p><b>Note :</b>This is a <b>required</b> dependency.</p>
	 * 
	 * <p><i>Spring Injected</i></p>
	 * 
	 * @param splitterService SplitterService for the <tt>ClusterJobServiceImpl</tt>
	 */
	@Required
	public void setSplitterService(SplitterService splitterService) {
		this.splitterService = splitterService;
	}

	/**
	 * <p>Returns the {@link AggregatorService} used by the <tt>ClusterJobServiceImpl</tt></p>.
	 * 
	 * <p>{@link AggregatorService} is responsible for collecting results returned by each 
	 * {@link GridTask} which was executed on a remote node, and to aggregate the results
	 * to provide the final result for the {@link GridJob}.</p>
	 * 
	 * @return {@link AggregatorService} reference.
	 */
	public AggregatorService getAggregatorService() {
		return aggregatorService;
	}

	/**
	 * <p>Returns the {@link AggregatorService} used by the <tt>ClusterJobServiceImpl</tt></p>.
	 * 
	 * <p>{@link AggregatorService} is responsible for collecting results returned by each 
	 * {@link GridTask} which was executed on a remote node, and to aggregate the results
	 * to provide the final result for the {@link GridJob}.</p>
	 * 
	 * <p><b>Note :</b>This is a <b>required</b> dependency.</p>
	 * 
	 * <p><i>Spring Injected</i></p>
	 * 
	 * @param aggregatorService {@link AggregatorService} for the service
	 */
	@Required
	public void setAggregatorService(AggregatorService aggregatorService) {
		this.aggregatorService = aggregatorService;
	}	

}
