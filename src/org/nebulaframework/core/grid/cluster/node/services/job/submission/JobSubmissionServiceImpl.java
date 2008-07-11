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

package org.nebulaframework.core.grid.cluster.node.services.job.submission;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.manager.services.facade.ClusterManagerServicesFacade;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.JMSNamingSupport;
import org.nebulaframework.core.grid.cluster.node.GridNode;
import org.nebulaframework.core.job.SplitAggregateGridJob;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.exceptions.GridJobRejectionException;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.remoting.JmsInvokerProxyFactoryBean;

/**
 * Implementation of {@code JobSubmissionService}. Allows the {@code GridNode} 
 * to submit {@code GridJob}s to the Grid, through the {@code ClusterManager} 
 * of the local cluster.
 * <p>
 * The {@code JobSubmissionService} uses the {@link ClusterManagerServicesFacade}
 * to submit jobs to the Cluster.
 * <p>
 * <i>Spring Managed</i>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see JobSubmissionService
 * @see ClusterManagerServicesFacade
 * @see SplitAggregateGridJob
 * @see GridArchive
 * @see GridJobFuture
 */
public class JobSubmissionServiceImpl implements JobSubmissionService {

	private static Log log = LogFactory.getLog(JobSubmissionServiceImpl.class);
	
    private GridNode node;
    private ConnectionFactory connectionFactory;

    /**
     * Constructs a {@code  JobSubmissionServiceImpl} for the given {@code GridNode}
     * @param node Owner {@code GridNode}
     */
    public JobSubmissionServiceImpl(GridNode node) {
            super();
            this.node = node;
    }

	/**
	 * Sets the JMS ConnectionFactory used by this class to communicate with
	 * remote {@code ClusterManager}.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param connectionFactory
	 *            JMS {@code ConnectionFactory}
	 */
    @Required
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
            this.connectionFactory = connectionFactory;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method delegates to internal overloaded version of 
     * method {@link #submitJob(SplitAggregateGridJob, GridArchive)}
     */
    public GridJobFuture submitJob(SplitAggregateGridJob<?,?> job) throws GridJobRejectionException {
        
    	// Delegate to overloaded version    
    	return submitJob(job, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method delegates to each {@GridJob} submission to 
     * internal overloaded version of method 
     * {@link #submitJob(SplitAggregateGridJob, GridArchive)}
     */
	public Map<String, GridJobFuture> submitArchive(GridArchive archive) {
		
		// GridJobFuture(s) against Fully Qualified Class Name(s) of GridJob(s)
		Map<String, GridJobFuture> futureMap = new HashMap<String, GridJobFuture>();
		
		// Retrieve GridJob Class Names
		String[] classNames = archive.getJobClassNames();
		
		// Use Reflection to create GridJob instances and pass it to the submitJob method.
		for(String className : classNames) {
			try {
				//Instantiate GridJob
				Class<?> cls = Class.forName(className);
				Constructor<?> constructor = cls.getConstructor();
				SplitAggregateGridJob<?,?> job = (SplitAggregateGridJob<?,?>)constructor.newInstance();
				
				// Submit and get GridJobFuture
				futureMap.put(className, submitJob(job, archive));
				
			} catch (GridJobRejectionException e) {
				// Job Rejected
				futureMap.put(className, null);	// Put null to FutureMap
				log.warn("GridJob Rejected : " + className, e);
			} catch (Exception e) {
				// Failed to Submit due to Exception
				futureMap.put(className, null); // Put null to FutureMap
				log.fatal("Unable to submit GridJob " + className + " due to exception", e);
				
			}
		}
		return futureMap;
	}
	
	/**
	 * Internal method which submits the Job through Services Facade reference. If submitted
	 * successfully, it creates a proxy to the {@link GridJobFuture} for the submitted Job,
	 * and returns it. The actual {@code GridJobFuture} resides in the {@code ClusterManager}.
	 * 
	 * @param job GridJob to be submitted
	 * @param archive Archive, if applicable. This may be <tt>null</tt>
	 * @return GridJobFuture for the submitted job
	 */
	protected GridJobFuture submitJob(SplitAggregateGridJob<?,?> job, GridArchive archive) throws GridJobRejectionException {
		
		log.info("Submitting GridJob " + job.getClass().getName());
		
        // Submit Job to Cluster and retrieve JobId
        String jobId = this.node.getServicesFacade().submitJob(this.node.getId(), job, archive);
        
        log.debug("Submitted");
        
        // Create local proxy to interface remote service
        JmsInvokerProxyFactoryBean proxyFactory = new JmsInvokerProxyFactoryBean();
        proxyFactory.setConnectionFactory(connectionFactory);
        proxyFactory.setQueueName(JMSNamingSupport.getFutureQueueName(jobId));
        proxyFactory.setServiceInterface(GridJobFuture.class);
        
        // Manually call the afterPropertiesSet() to allow object to initialize
        proxyFactory.afterPropertiesSet();
        
        // Return Proxy
        return (GridJobFuture) proxyFactory.getObject();
	}

}
