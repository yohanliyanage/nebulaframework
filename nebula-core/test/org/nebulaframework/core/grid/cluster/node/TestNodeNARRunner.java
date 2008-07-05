package org.nebulaframework.core.grid.cluster.node;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.archive.support.GridArchiveSupport;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.springframework.context.ApplicationContext;
import org.springframework.remoting.RemoteInvocationFailureException;
import org.springframework.util.StopWatch;

public class TestNodeNARRunner {
	
	private static Log log = LogFactory.getLog(TestNodeRunner.class);
	

	public static void main(String[] args) {
		try {

			ApplicationContext ctx = new ClassPathXmlApplicationContext("org/nebulaframework/core/grid/cluster/node/grid-node.xml");
			GridNode node = (GridNode) ctx.getBean("localNode");
			
			log.debug("Registering Node");
			node.getNodeRegistrationService().register();
			
			StopWatch sw = new StopWatch();
			sw.start();
			
			// Submit Job
			log.debug("Submitting Job");
			GridArchive archive =  GridArchiveSupport.createGridArchive(new File("simpletestjob.nar"));
			GridJobFuture future = (GridJobFuture) node.getJobSubmissionService().submitArchive(archive).values().toArray()[0];
			
			try {
				log.info("RESULT : " + future.getResult());
			} catch (RemoteInvocationFailureException e) {
				e.getCause().printStackTrace();
			}

			sw.stop();
			
			log.debug("Job Duration : " + (sw.getLastTaskTimeMillis()) + " ms");
			log.debug("Waiting...");
			System.in.read();
			log.debug("Unregistering Node");
			node.getNodeRegistrationService().unregister();
			System.exit(0);
		} 
		catch (Exception e) {
			log.error(e);
		}

	}
}

