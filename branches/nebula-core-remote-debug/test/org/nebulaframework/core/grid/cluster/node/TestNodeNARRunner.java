package org.nebulaframework.core.grid.cluster.node;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.springframework.context.ApplicationContext;
import org.springframework.remoting.RemoteInvocationFailureException;
import org.springframework.util.StopWatch;

public class TestNodeNARRunner {
	
	private static Log log = LogFactory.getLog(TestNodeRunner.class);
	

	public static void main(String[] args) {
		try {

			log.info("GridNode Starting...");
			StopWatch sw = new StopWatch();
			sw.start();
			
			ApplicationContext ctx = new ClassPathXmlApplicationContext("org/nebulaframework/core/grid/cluster/node/grid-node.xml");
			GridNode node = (GridNode) ctx.getBean("localNode");
			
			log.info("GridNode ID : " + node.getId());
			
			node.getNodeRegistrationService().register();
			log.info("Registered in Cluster : " + node.getNodeRegistrationService().getRegistration().getClusterId());
			
			sw.stop();

			log.info("GridNode Started Up. [" + sw.getLastTaskTimeMillis() + " ms]");
			
			// Submit Job
			log.debug("Submitting Job");
			
			sw.start();
			
			GridArchive archive =  GridArchive.fromFile(new File("simpletestjob.nar"));
			GridJobFuture future = (GridJobFuture) node.getJobSubmissionService().submitArchive(archive).values().toArray()[0];
			
			try {
				log.info("RESULT : " + future.getResult());
			} catch (RemoteInvocationFailureException e) {
				e.getCause().printStackTrace();
			}
			
			sw.stop();
			log.info("GridJob Finished. Duration " + sw.getLastTaskTimeMillis() + " ms");
			
			log.debug("Press any key to unregister GridNode and terminate");
			System.in.read();
			node.getNodeRegistrationService().unregister();
			
			log.info("Unregistered, Terminating...");
			System.exit(0);
			
			
		} 
		catch (Exception e) {
			log.error(e);
		}

	}
}

