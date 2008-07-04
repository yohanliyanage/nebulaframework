package org.nebulaframework.core.grid.cluster.manager.services.jobs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.aggregator.AggregatorService;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.splitter.SplitterService;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.support.JMSSupport;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.deploy.GridJobInfo;
import org.nebulaframework.core.job.exceptions.GridJobPermissionDeniedException;
import org.nebulaframework.core.job.exceptions.GridJobRejectionException;
import org.nebulaframework.core.job.future.GridJobFutureImpl;
import org.nebulaframework.core.servicemessage.ServiceMessage;
import org.nebulaframework.core.servicemessage.ServiceMessageType;
import org.nebulaframework.util.hashing.SHA1Generator;

public class ClusterJobServiceImpl implements ClusterJobService {

	private ClusterManager cluster;
	private JMSSupport jmsSupport;

	private Map<String, GridJobProfile> jobs = new HashMap<String, GridJobProfile>();

	private SplitterService splitterService;
	private AggregatorService aggregatorService;

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
		
		String jobId = this.cluster.getClusterId() + "." + owner + "."
				+ UUID.randomUUID();

		GridJobProfile profile = new GridJobProfile();

		jmsSupport.createTaskQueue(jobId);
		jmsSupport.createResultQueue(jobId);
		jmsSupport.createFutureQueue(jobId);

		GridJobFutureImpl future = jmsSupport.createFuture(jobId);

		profile.setJobId(jobId);
		profile.setOwner(owner);
		profile.setJob(job);
		profile.setFuture(future);
		
		if (archive!=null) {
			if (!verifyArchive(archive)) throw new GridJobRejectionException("Failed to verify Archive");
		}
		
		profile.setArchive(archive);
		
		synchronized (this) {
			this.jobs.put(jobId, profile);
		}

		splitterService.startSplitter(profile);
		aggregatorService.startAggregator(profile);

		// Notify Job Start
		notifyJobStart(jobId);

		return jobId;
	}

	private boolean verifyArchive(GridArchive archive) {
		// Try to compare SHA1 Digests for bytes
		return SHA1Generator.bytesToString(SHA1Generator.generate(archive.getBytes())).equals(archive.getHash());
	}

	/**
	 * {@inheritDoc}
	 */
	public GridJobInfo requestJob(String jobId) throws GridJobPermissionDeniedException {
		
		// FIXME hard coded to allow all nodes to participate.
		
		GridJobInfo info = new GridJobInfo(jobId);
		
		try {
			GridJobProfile profile = jobs.get(jobId);
			if (profile.isArchived()) {
				info.setArchive(profile.getArchive());
			}
		} catch (Exception e) {
			new GridJobPermissionDeniedException("Permission denied due to exception", e);
		}
		
		return info;
	}

	public void setJmsSupport(JMSSupport jmsSupport) {
		this.jmsSupport = jmsSupport;
	}

	public SplitterService getSplitterService() {
		return splitterService;
	}

	public void setSplitterService(SplitterService splitterService) {
		this.splitterService = splitterService;
	}

	public AggregatorService getAggregatorService() {
		return aggregatorService;
	}

	public void setAggregatorService(AggregatorService aggregatorService) {
		this.aggregatorService = aggregatorService;
	}

	public void notifyJobStart(String jobId) {
		ServiceMessage message = new ServiceMessage(jobId);
		message.setType(ServiceMessageType.JOB_START);

		cluster.getServiceMessageSender().sendServiceMessage(message);
	}

	public void notifyJobEnd(String jobId) {
		try {
			ServiceMessage message = new ServiceMessage(jobId);
			message.setType(ServiceMessageType.JOB_END);

			cluster.getServiceMessageSender().sendServiceMessage(message);
		} finally {
			jobs.remove(jobId);
		}
	}

	public void notifyJobCancel(String jobId) {
		try {
			ServiceMessage message = new ServiceMessage(jobId);
			message.setType(ServiceMessageType.JOB_CANCEL);

			cluster.getServiceMessageSender().sendServiceMessage(message);
		} finally {
			jobs.remove(jobId);
		}
	}

	protected synchronized void removeJob(String jobId) {
		this.jobs.remove(jobId);
	}

	public synchronized GridJobProfile getProfile(String jobId) {
		return jobs.get(jobId);
	}

	public synchronized boolean isActiveJob(String jobId) {
		return this.jobs.containsKey(jobId);
	}

}
