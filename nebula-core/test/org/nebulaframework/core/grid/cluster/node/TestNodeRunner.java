package org.nebulaframework.core.grid.cluster.node;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.nebulaframework.core.grid.cluster.registration.RegistrationException;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.springframework.context.ApplicationContext;
import org.springframework.remoting.RemoteInvocationFailureException;

import test.nebulaframework.simpleTest.TestJob;

public class TestNodeRunner {
	
	private static Log log = LogFactory.getLog(TestNodeRunner.class);
	
	public static void main(String[] args) {

		// Test Job
		TestJob testJob = new TestJob();
		
		try {

			ApplicationContext ctx = new ClassPathXmlApplicationContext("org/nebulaframework/core/grid/cluster/node/grid-node.xml");
			GridNode node = (GridNode) ctx.getBean("localNode");

			log.debug("Registering Node");
			node.getNodeRegistrationService().register();
			
			Date dStart = new Date();
			// Submit Job
			log.debug("Submitting Job");
			GridJobFuture future = node.getJobSubmissionService().submitJob(testJob);
			try {
				log.info("RESULT : " + future.getResult());
			} catch (RemoteInvocationFailureException e) {
				e.getCause().printStackTrace();
			}
			Date dEnd = new Date();
			
			log.debug("Job Duration : " + (dEnd.getTime() - dStart.getTime()) + " ms");
			log.debug("Waiting...");
			System.in.read();
			log.debug("Unregistering Node");
			node.getNodeRegistrationService().unregister();
		} catch (RegistrationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
