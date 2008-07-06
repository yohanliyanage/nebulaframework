package org.nebulaframework.deployment.classloading.node.exporter;

import java.util.UUID;

import javax.jms.ConnectionFactory;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.remoting.JmsInvokerProxyFactoryBean;
import org.springframework.jms.remoting.JmsInvokerServiceExporter;

public class GridNodeClassExporterSupport {
	
	private static Log log = LogFactory.getLog(GridNodeClassExporterSupport.class);
	
	public static void startService(UUID nodeId, ConnectionFactory connectionFactory) {
		
		GridNodeClassExporterImpl service = new GridNodeClassExporterImpl();
		
		JmsInvokerServiceExporter exporter = new JmsInvokerServiceExporter();
		exporter.setService(service);
		exporter.setServiceInterface(GridNodeClassExporter.class);
		exporter.afterPropertiesSet();
		
		ActiveMQQueue queue = new ActiveMQQueue(getExporterQueueName(nodeId));
		DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setDestination(queue);
		container.setMessageListener(exporter);
		container.afterPropertiesSet();
		
		log.debug("GridNodeClassExporter Service Started");
	}

	private static String getExporterQueueName(UUID nodeId) {
		return "nebula.node." + nodeId + ".classexport.queue";
	}

	public static GridNodeClassExporter createServiceProxy(UUID nodeId, ConnectionFactory connectionFactory) {
		JmsInvokerProxyFactoryBean proxyFactory = new JmsInvokerProxyFactoryBean();
		proxyFactory.setConnectionFactory(connectionFactory);
		proxyFactory.setQueueName(getExporterQueueName(nodeId));
		proxyFactory.setServiceInterface(GridNodeClassExporter.class);
		proxyFactory.afterPropertiesSet();
		
		return (GridNodeClassExporter) proxyFactory.getObject();
	}	
}
