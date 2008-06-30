package org.nebulaframework.core.grid.cluster.node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.nebulaframework.core.grid.cluster.registration.RegistrationException;
import org.springframework.context.ApplicationContext;
import org.springframework.remoting.RemoteAccessException;

public class TestNodeRunner {
	
	private static Log log = LogFactory.getLog(TestNodeRunner.class);
	
	public static void main(String[] args) {

		try {

			ApplicationContext ctx = new ClassPathXmlApplicationContext("org/nebulaframework/core/grid/cluster/node/grid-node.xml");
			GridNode node = (GridNode) ctx.getBean("localNode");

			log.debug("Registering Node");
			node.register();
			log.debug("Sleeping...");
			Thread.sleep(10000);
			log.debug("Unregistering Node");
			node.unregister();
		} catch (RegistrationException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteAccessException ex) {
			log.error("Unable to connect to Cluster",ex);
		}
		
		
	}
}
