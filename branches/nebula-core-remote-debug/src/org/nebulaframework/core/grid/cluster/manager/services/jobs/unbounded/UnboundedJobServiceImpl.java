package org.nebulaframework.core.grid.cluster.manager.services.jobs.unbounded;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.InternalClusterJobService;
import org.springframework.beans.factory.annotation.Required;

// TODO FixDoc
public class UnboundedJobServiceImpl implements
		UnboundedJobService {

	private static Log log = LogFactory.getLog(UnboundedJobServiceImpl.class);
	private InternalClusterJobService jobService;
	private ConnectionFactory connectionFactory;
	
	
	
	public UnboundedJobServiceImpl() {
		super();
	}


	@Required
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}


	@Required
	public void setJobService(InternalClusterJobService jobService) {
		this.jobService = jobService;
	}


	public void startJobProcessing(final GridJobProfile profile) {
		new Thread(new Runnable() {
			public void run() {
				log.debug("[UnboundedJobService] Starting Processsor");
				new UnboundedJobProcessor(profile, connectionFactory, jobService).start();
			}
		}).start();
		
	}
	
}
