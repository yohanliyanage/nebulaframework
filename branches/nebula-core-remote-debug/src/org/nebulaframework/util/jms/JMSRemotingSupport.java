package org.nebulaframework.util.jms;

import javax.jms.ConnectionFactory;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.remoting.JmsInvokerProxyFactoryBean;
import org.springframework.jms.remoting.JmsInvokerServiceExporter;

// TODO FixDoc
public class JMSRemotingSupport {

	@SuppressWarnings("unchecked")
	/* Ignore Unchecked Cast */
	public static <T> T createProxy(ConnectionFactory cf, String queueName,
			Class<T> service) {
		JmsInvokerProxyFactoryBean proxyFactory = new JmsInvokerProxyFactoryBean();
		proxyFactory.setConnectionFactory(cf);
		proxyFactory.setQueueName(queueName);
		proxyFactory.setServiceInterface(service);
		proxyFactory.afterPropertiesSet();

		return (T) proxyFactory.getObject();
	}

	public static <T> DefaultMessageListenerContainer createService(
			ConnectionFactory cf, String queueName, T service, Class<T> clazz) {

		ActiveMQQueue queue = new ActiveMQQueue(queueName);

		// Export Service
		JmsInvokerServiceExporter exporter = new JmsInvokerServiceExporter();
		exporter.setServiceInterface(clazz);
		exporter.setService(service);
		exporter.afterPropertiesSet();

		DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		container.setConnectionFactory(cf);
		container.setDestination(queue);
		container.setMessageListener(exporter);
		container.afterPropertiesSet();

		return container;
	}
}
