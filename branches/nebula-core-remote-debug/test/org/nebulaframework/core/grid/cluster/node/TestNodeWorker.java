package org.nebulaframework.core.grid.cluster.node;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.nebulaframework.grid.cluster.registration.RegistrationException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StopWatch;

public class TestNodeWorker {
	
	private static Log log = LogFactory.getLog(TestNodeWorker.class);
	
	public static void main(String[] args) {

		try {

			log.info("GridNode Starting...");
			StopWatch sw = new StopWatch();
			sw.start();
			
			ApplicationContext ctx = new ClassPathXmlApplicationContext("org/nebulaframework/core/grid/cluster/node/grid-node.xml");
			GridNode node = (GridNode) ctx.getBean("localNode");
			
			sw.stop();
			log.info("GridNode Started Up. [" + sw.getLastTaskTimeMillis() + " ms]");
			
			log.info("GridNode ID : " + node.getId());
			
			node.getNodeRegistrationService().register();
			log.info("Registered in Cluster : " + node.getNodeRegistrationService().getRegistration().getClusterId());
			
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
}
