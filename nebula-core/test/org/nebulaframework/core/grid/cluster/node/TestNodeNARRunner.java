package org.nebulaframework.core.grid.cluster.node;

import java.io.File;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.archive.support.GridArchiveSupport;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.springframework.context.ApplicationContext;
import org.springframework.remoting.RemoteInvocationFailureException;

public class TestNodeNARRunner {
	
	private static Log log = LogFactory.getLog(TestNodeRunner.class);
	

	public static void main(String[] args) {
		try {

			ApplicationContext ctx = new ClassPathXmlApplicationContext("org/nebulaframework/core/grid/cluster/node/grid-node.xml");
			GridNode node = (GridNode) ctx.getBean("localNode");

			log.debug("Registering Node");
			node.getNodeRegistrationService().register();
			
			Date dStart = new Date();
			// Submit Job
			log.debug("Submitting Job");
			GridArchive archive =  GridArchiveSupport.createGridArchive(new File("test\\simpletestjob.nar"));
			GridJobFuture future = node.getJobSubmissionService().submitArchive(archive)[0];
			
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
		} 
		catch (Exception e) {
			log.error(e);
		}

	}
}

