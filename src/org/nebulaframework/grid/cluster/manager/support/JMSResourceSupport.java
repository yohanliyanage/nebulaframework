package org.nebulaframework.grid.cluster.manager.support;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.cluster.manager.ClusterManager;

public class JMSResourceSupport {

	private static final int MAX_ATTEMPTS = 5;
	
	private static Log log = LogFactory.getLog(JMSResourceSupport.class);
	
	public static void removeQueue(String queueName) {
		removeDestination(new ActiveMQQueue(queueName));
	}
	
	public static void removeTopic (String topicName) {
		removeDestination(new ActiveMQTopic(topicName));
	}
	
	private static void removeDestination(final ActiveMQDestination destination) {
		new Thread(new Runnable() {

			public void run() {

				BrokerService broker = ClusterManager.getInstance().getBrokerService();
				
				if (broker==null) { // If No BrokerService
					return;
				}
				
				boolean attempt = true;
				int count = 0;
				
				while (attempt) {
					try {
						
						broker.removeDestination(destination);
						log.debug("JMS Destination " +destination.getPhysicalName() + " removed");
						
						attempt = false;
					} catch (Exception e) {
						
						if (++count > MAX_ATTEMPTS) {
							attempt = false;
							log.warn("Unable to remove Destination : " + destination.getPhysicalName(),e);
							return;
						}
						
						if (count > 2) {
							log.debug("Unable to remove Destination : " + destination.getPhysicalName() + " - Attempt " + count);
						}
						
						attempt = true;
						try {
							Thread.sleep(5000);
						} catch (InterruptedException ex) {
							log.debug(ex);
						}
						
						
					}
				}				
			}
			
		}).start();
	}
}
