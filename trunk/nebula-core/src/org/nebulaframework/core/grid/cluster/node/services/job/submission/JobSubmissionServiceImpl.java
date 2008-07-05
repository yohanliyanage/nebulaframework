package org.nebulaframework.core.grid.cluster.node.services.job.submission;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.node.GridNode;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.exceptions.GridJobRejectionException;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.springframework.jms.remoting.JmsInvokerProxyFactoryBean;


public class JobSubmissionServiceImpl implements JobSubmissionService {

	private static Log log = LogFactory.getLog(JobSubmissionServiceImpl.class);
	
    private GridNode node;
    private ConnectionFactory connectionFactory;

    public JobSubmissionServiceImpl(GridNode node) {
            super();
            this.node = node;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
            this.connectionFactory = connectionFactory;
    }

    // Create and return proxy for GridJobFuture
    public GridJobFuture submitJob(GridJob<? extends Serializable> job) throws GridJobRejectionException {
            return submitJob(job, null);

    }

	public Map<String, GridJobFuture> submitArchive(GridArchive archive) {
		
		Map<String, GridJobFuture> futureMap = new HashMap<String, GridJobFuture>();
		
		String[] classNames= archive.getJobClassNames();
		
		// Use Reflection to create GridJob instances and pass it to the submitJob method.
		for(String className : classNames) {
			try {
				Class<?> cls = Class.forName(className);
				Constructor<?> constructor = cls.getConstructor();
				Object job = constructor.newInstance();
				futureMap.put(className, submitJob((GridJob<?>)job, archive));
			} catch (GridJobRejectionException e) {
				log.warn("GridJob Rejected : " + className, e);
				futureMap.put(className, null);	// Put null to FutureMap
			} catch (Exception e) {
				log.fatal("Unable to submit GridJob " + className + " due to exception", e);
				futureMap.put(className, null); // Put null to FutureMap
			}
		}
		return futureMap;
	}
	
	/**
	 * Internal method which submits the Job through Services Facade reference.
	 * @param job GridJob to be submitted
	 * @param archive Archive, if applicable. This may be <tt>null</tt>
	 * @return GridJobFuture for the submitted job
	 */
	protected GridJobFuture submitJob(GridJob<? extends Serializable> job, GridArchive archive) throws GridJobRejectionException {
		
		log.info("Submitting GridJob " + job.getClass().getName());
		
        // Submit Job to Cluster and retrieve JobId
        String jobId = this.node.getServicesFacade().submitJob(this.node.getId(), job, archive);
        
        // Create local proxy to interface remote service
        JmsInvokerProxyFactoryBean proxyFactory = new JmsInvokerProxyFactoryBean();
        proxyFactory.setConnectionFactory(connectionFactory);
        proxyFactory.setQueueName("nebula.jobs." + jobId + ".future.queue");
        proxyFactory.setServiceInterface(GridJobFuture.class);
        
        // Manually call the afterPropertiesSet() to allow object to initialize
        proxyFactory.afterPropertiesSet();
        
        // Return Proxy
        return (GridJobFuture) proxyFactory.getObject();
	}

}
