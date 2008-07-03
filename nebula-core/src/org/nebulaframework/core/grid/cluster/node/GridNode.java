package org.nebulaframework.core.grid.cluster.node;

import java.util.UUID;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.manager.services.facade.ClusterManagerServicesFacade;
import org.nebulaframework.core.grid.cluster.node.services.job.execution.JobExecutionService;
import org.nebulaframework.core.grid.cluster.node.services.job.submission.JobSubmissionService;
import org.nebulaframework.core.grid.cluster.node.services.message.ServiceMessagesSupport;
import org.nebulaframework.core.grid.cluster.node.services.registration.NodeRegistrationService;
import org.nebulaframework.core.servicemessage.ServiceMessage;
import org.nebulaframework.core.support.ID;
import org.nebulaframework.deployment.classloading.node.exporter.support.GridNodeClassExporterSupport;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

public class GridNode implements InitializingBean{

	private static Log log = LogFactory.getLog(GridNode.class);

	private UUID id;
	private GridNodeProfile profile;

	private ConnectionFactory connectionFactory;
	private NodeRegistrationService nodeRegistrationService;
	private ServiceMessagesSupport serviceMessageSupport;
	private ClusterManagerServicesFacade servicesFacade;
	private JobExecutionService jobExecutionService;
	private JobSubmissionService jobSubmissionService;
	
	public GridNode(GridNodeProfile profile) {
		super();
		this.id = ID.getId();
		this.profile = profile;
		log.debug("Node " + id + " created");
	}

	public UUID getId() {
		return id;
	}

	public GridNodeProfile getProfile() {
		return profile;
	}

	public void onServiceMessage(ServiceMessage obj) {
		log.info(obj);
	}

	public NodeRegistrationService getNodeRegistrationService() {
		return nodeRegistrationService;
	}

	public void setNodeRegistrationService(
			NodeRegistrationService nodeRegistrationService) {
		this.nodeRegistrationService = nodeRegistrationService;
	}

	public ServiceMessagesSupport getServiceMessageSupport() {
		return serviceMessageSupport;
	}

	public void setServiceMessageSupport(
			ServiceMessagesSupport serviceMessageSupport) {
		this.serviceMessageSupport = serviceMessageSupport;
	}

	public ClusterManagerServicesFacade getServicesFacade() {
		return servicesFacade;
	}

	public void setServicesFacade(ClusterManagerServicesFacade servicesFacade) {
		this.servicesFacade = servicesFacade;
	}

	public JobExecutionService getJobExecutionService() {
		return jobExecutionService;
	}

	public void setJobExecutionService(JobExecutionService jobExecutionService) {
		this.jobExecutionService = jobExecutionService;
	}

	public JobSubmissionService getJobSubmissionService() {
		return jobSubmissionService;
	}

	public void setJobSubmissionService(JobSubmissionService jobSubmissionService) {
		this.jobSubmissionService = jobSubmissionService;
	}

	@Required
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public void afterPropertiesSet() throws Exception {
		GridNodeClassExporterSupport.startService(this.id, this.connectionFactory);
	}
	
	

	
}
