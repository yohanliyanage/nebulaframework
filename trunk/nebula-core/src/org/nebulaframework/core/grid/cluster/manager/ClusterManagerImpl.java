package org.nebulaframework.core.grid.cluster.manager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.ServiceMessage;
import org.nebulaframework.core.grid.cluster.manager.messaging.ServiceMessageSender;
import org.nebulaframework.core.grid.cluster.registration.Registration;
import org.nebulaframework.core.grid.cluster.registration.RegistrationImpl;
import org.nebulaframework.core.job.GridJobProfile;
import org.nebulaframework.core.support.ID;

public class ClusterManagerImpl implements ClusterManager {

	private static final long SERVICE_PORT = 61616L;
	private static final String JMS_DOMAIN_PREFIX = "nebula.cluster.";
	private static final String JMS_SERVICE_TOPIC_SUFFIX = ".service.topic";
	private static Log log = LogFactory.getLog(ClusterManagerImpl.class);

	private UUID id;
	private Set<UUID> clusterNodes = new HashSet<UUID>();
	private String brokerUrl; // JMS Broker URL for Cluster Manager
	private ServiceMessageSender serviceMessageSender;

	public ClusterManagerImpl() throws UnknownHostException {
		super();
		this.id = ID.getId();
		this.brokerUrl = InetAddress.getLocalHost().getHostAddress() + ":"
				+ SERVICE_PORT;

		log.info("ClusterManager created [UUID:" + this.id + "]");
		log.debug("Broker URL : " + this.brokerUrl);
		log.debug("Service Topic : " + this.getServiceTopicName());
	}

	/**
	 * {@inheritDoc}
	 */
	public Registration registerNode(UUID nodeId) {

		// Set the Registration Data
		RegistrationImpl reg = new RegistrationImpl();
		reg.setBrokerUrl(this.brokerUrl);
		reg.setServiceTopic(this.getServiceTopicName());
		reg.setUuid(nodeId);
		reg.setClusterId(this.id);

		this.clusterNodes.add(nodeId);

		log.info("Node registered [ID:" + nodeId + "]");
		
		new Thread(new Runnable() {

			public void run() {
				try {
					Thread.sleep(2000);
					serviceMessageSender.sendServiceMessage(new ServiceMessage("Welcome to Nebula Cluster " + id));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}).start();
		// Return Registration Data to Node
		return reg;
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterNode(UUID id) {
		this.clusterNodes.remove(id);
		log.info("Node unregistered [ID:" + id + "]");
	}

	/**
	 * {@inheritDoc}
	 */
	public UUID getId() {
		return id;
	}

	public String getServiceTopicName() {
		return JMS_DOMAIN_PREFIX + this.id.toString()
				+ JMS_SERVICE_TOPIC_SUFFIX;
	}

	public GridJobProfile requestJob(String jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	public ServiceMessageSender getServiceMessageSender() {
		return serviceMessageSender;
	}

	public void setServiceMessageSender(
			ServiceMessageSender serviceMessageSender) {
		this.serviceMessageSender = serviceMessageSender;
	}

}
