package org.nebulaframework.grid.cluster.manager.services.jobs.remote;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.deploy.GridJobInfo;
import org.nebulaframework.core.job.exceptions.GridJobPermissionDeniedException;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.manager.services.jobs.JMSNamingSupport;
import org.nebulaframework.util.jms.JMSRemotingSupport;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

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
		String queueName = JMSNamingSupport.getRemoteJobServiceQueueName();
		JMSRemotingSupport.createService(connectionFactory, queueName, this, RemoteClusterJobService.class);
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
