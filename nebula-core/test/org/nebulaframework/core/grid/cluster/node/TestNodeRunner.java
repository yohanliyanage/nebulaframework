package org.nebulaframework.core.grid.cluster.node;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.nebulaframework.core.grid.cluster.registration.RegistrationException;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.springframework.context.ApplicationContext;
import org.springframework.remoting.RemoteInvocationFailureException;
import org.springframework.util.StopWatch;

import test.nebulaframework.simpleTest.TestJob;

public class TestNodeRunner {
	
	private static Log log = LogFactory.getLog(TestNodeRunner.class);
	
	public static void main(String[] args) {

		// Test Job
		TestJob testJob = new TestJob();
		
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
			
			GridJobFuture future = node.getJobSubmissionService().submitJob(testJob);
			try {
				log.info("Job Result : " + future.getResult());
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
			
		
		} catch (RegistrationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static List<Serializable> getStuff() {
		return null;
	}
}
