package org.nebulaframework.core.grid.cluster.node.services.job.execution;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.node.GridNode;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.deploy.GridJobInfo;
import org.nebulaframework.core.job.exceptions.GridJobPermissionDeniedException;
import org.nebulaframework.core.servicemessage.ServiceMessage;
import org.nebulaframework.core.servicemessage.ServiceMessageType;
import org.nebulaframework.deployment.classloading.service.ClassLoadingService;
import org.nebulaframework.deployment.classloading.service.support.ClassLoadingServiceSupport;
import org.springframework.beans.factory.annotation.Required;

public class JobExecutionServiceImpl implements JobExecutionService {

	private static Log log = LogFactory.getLog(JobExecutionServiceImpl.class);
	private boolean idle = true;
	private String currentJobId;
	
	private GridNode node;
	private ConnectionFactory connectionFactory;
	
	private ClassLoadingService classLoadingService;
	
	public JobExecutionServiceImpl(GridNode node) {
		super();
		this.node = node;
	}

	@Required
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}


	public void onServiceMessage(ServiceMessage message) {
		log.debug("Job Service Message Received : " + message.getType());
		if (message.getType().equals(ServiceMessageType.JOB_START)) {
			newJob(message.getMessage());
		}
		else if (message.getType().equals(ServiceMessageType.JOB_END)) {
			endJob(message.getMessage());
		}
		else if (message.getType().equals(ServiceMessageType.JOB_CANCEL)) {
			terminateJob(message.getMessage());
		}
	}


	
	private synchronized void newJob(String jobId) {
		
		initalizeService();
		
		if (idle) {
			// Request for Job
			try {
				
				GridJobInfo jobInfo = node.getServicesFacade().requestJob(jobId);
				
				// Start it
				if (jobInfo.isArchived()) {
					startNewArchivedJob(jobId, jobInfo.getArchive());
				}
				else {
					startNewJob(jobId);
				}
				
			} catch (GridJobPermissionDeniedException e) {
				log.info("Permission denied to participate in Job " + jobId);
			}		
		}
		else {
			log.debug("New Job notified, ignoring as Local Node is busy : " + jobId);
			return;
		}
	}

	@SuppressWarnings("unchecked")
	private synchronized void startNewJob(String jobId) {

		log.debug("Starting new Job Processing");
		
		this.idle = false;
		this.currentJobId = jobId;

		TaskExecutor.startForJob(jobId, node, connectionFactory, classLoadingService, null);
	}
	
	@SuppressWarnings("unchecked")
	private synchronized void startNewArchivedJob(String jobId, GridArchive archive) {

		log.debug("Starting new Archived Job Processing");
		
		this.idle = false;
		this.currentJobId = jobId;

		TaskExecutor.startForJob(jobId, node, connectionFactory, classLoadingService, archive);
	}
	
	private synchronized void endJob(String jobId) {
		if ((this.currentJobId != null) && (this.currentJobId.equals(jobId))) {
			TaskExecutor.stopForJob(jobId);
			this.currentJobId = null;
			this.idle = true;
		}
		else {
			log.debug("Job End Message Received [" + jobId + "], but ignoring as not applicable");
		}
	}

	private synchronized void terminateJob(String jobId) {
		if ((this.currentJobId != null) && (this.currentJobId.equals(jobId))) {
			log.info("Terminating Job Execution " + jobId);
			TaskExecutor.stopForJob(jobId);
			this.currentJobId = null;
			this.idle = true;
		}
		else {
			log.debug("Job Terminate Message Received [" + jobId + "], but ignoring as not applicable");
		}
	}	

	public String getCurrentJobId() {
		return currentJobId;
	}

	public void initalizeService () {
		if (classLoadingService == null) {
			classLoadingService = ClassLoadingServiceSupport.createProxy(node.getNodeRegistrationService().getRegistration().getClusterId(), connectionFactory);
		}
	}
	

	

}
