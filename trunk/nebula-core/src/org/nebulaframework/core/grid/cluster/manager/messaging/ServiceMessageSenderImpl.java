package org.nebulaframework.core.grid.cluster.manager.messaging;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.ServiceMessage;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class ServiceMessageSenderImpl implements ServiceMessageSender {

	private static Log log = LogFactory.getLog(ServiceMessageSenderImpl.class);
	private JmsTemplate jmsTemplate;

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public void sendServiceMessage(final ServiceMessage message) {
		
		log.debug("Sending Service Message : " + message);
		
		jmsTemplate.send(new MessageCreator() {
			
			public Message createMessage(Session session) throws JMSException {
				return session.createObjectMessage(message);
			}
			
		});
	}

}
