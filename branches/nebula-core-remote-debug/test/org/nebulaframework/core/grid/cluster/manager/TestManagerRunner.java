package org.nebulaframework.core.grid.cluster.manager;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.network.NetworkConnector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StopWatch;


public class TestManagerRunner {
	
	private static Log log = LogFactory.getLog(TestManagerRunner.class);
	
	private static ApplicationContext ctx;
	
	public static void main(String[] args) throws Exception {
		
		ClusterManager mgr = startContainer();
		
		BrokerService broker = (BrokerService) ctx.getBean("broker");
		
		System.out.println("Press any key to Add new Network Broker");
		System.in.read();
		
		NetworkConnector con = broker.addNetworkConnector("static://(tcp://excalibur:61616)");
		
		if (con!=null) {
			// con.addExcludedDestination(new ActiveMQQueue(">"));
			con.addStaticallyIncludedDestination(new ActiveMQTopic("nebula.cluster.service.topic"));
			con.setConduitSubscriptions(false);
			con.setDynamicOnly(true);
			con.addExcludedDestination(new ActiveMQQueue("nebula.cluster.registration.queue"));
			con.addExcludedDestination(new ActiveMQQueue("nebula.cluster.services.facade.queue"));
			con.setNetworkTTL(128);
			con.start();
		}

//		System.out.println("Press any key to Shutdown");
//		System.in.read();
//		
//		mgr.shutdown();
	}
	
	public static ClusterManager startContainer() {
		log.info("ClusterManager Starting");
		StopWatch sw = new StopWatch();
		sw.start();
		ctx = new ClassPathXmlApplicationContext("org/nebulaframework/core/grid/cluster/manager/cluster-manager.xml");
		sw.stop();
		log.info("ClusterManager Started Up. [" + sw.getLastTaskTimeMillis() + " ms]");
		return (ClusterManager) ctx.getBean("clusterManager");
	}
	
}