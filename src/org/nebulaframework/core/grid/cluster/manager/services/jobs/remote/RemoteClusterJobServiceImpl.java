package org.nebulaframework.core.grid.cluster.manager.services.jobs.remote;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.JMSNamingSupport;
import org.nebulaframework.core.job.deploy.GridJobInfo;
import org.nebulaframework.core.job.exceptions.GridJobPermissionDeniedException;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.remoting.JmsInvokerServiceExporter;

// TODO FixDoc
public class RemoteClusterJobServiceImpl implements InternalRemoteClusterJobService, RemoteClusterJobService {

	private static Log log = LogFactory.getLog(RemoteClusterJobServiceImpl.class);
	
	private ClusterManager cluster;
	private ConnectionFactory connectionFactory;
	private DefaultMessageListenerContainer container;
	
	public RemoteClusterJobServiceImpl(ClusterManager cluster, ConnectionFactory connectionFactory) {
		super();
		this.cluster = cluster;
		this.connectionFactory = connectionFactory;
		initialize();
	}		

	protected void initialize() {
		// Create JMS Stuff and expose service
		
		JmsInvokerServiceExporter exporter = new JmsInvokerServiceExporter();
		exporter.setService(this);
		exporter.setServiceInterface(RemoteClusterJobService.class);
		exporter.afterPropertiesSet();
		
		container = new DefaultMessageListenerContainer();
		container.setDestinationName(JMSNamingSupport.getRemoteJobServiceQueueName());
		container.setConnectionFactory(connectionFactory);
		container.setMessageListener(exporter);
		container.setMessageSelector("targetClusterId = '" + cluster.getClusterId() + "'");
		container.afterPropertiesSet();
		
		log.debug("[RemoteJobService] Initialized");
	}
	
	/* (non-Javadoc)
	 * @see org.nebulaframework.core.grid.cluster.manager.services.jobs.remote.RemoteClusterJobService#remoteJobRequest(java.lang.String)
	 */
	public GridJobInfo remoteJobRequest(String jobId) 
			throws GridJobPermissionDeniedException, IllegalArgumentException {
		
		log.debug("[RemoteJobService] Received Request for {" + jobId + "}");
		return cluster.getJobService().requestJob(jobId);
		
	}

	public void shutdown() {
		container.stop();
		container.shutdown();
	}
	
	
}
