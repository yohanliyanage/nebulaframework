/*
 * Copyright (C) 2008 Yohan Liyanage. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package org.nebulaframework.core.grid.cluster.manager.services.messaging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.servicemessage.ServiceMessage;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;

/**
 * Implementation of {@code ServiceMessageSender}. This implementation uses
 * Spring's JMS Support to deliver messages to the {@code ServiceTopic}.
 * <p>
 * <i>Spring Managed</i>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class ServiceMessageSenderImpl implements ServiceMessageSender {

	private static Log log = LogFactory.getLog(ServiceMessageSenderImpl.class);
	
	private JmsTemplate jmsTemplate;	// Spring JMS Template

	/**
	 * JmsTemplate is used by this class to send messages to {@code ServiceTopic}.
	 * <p>
	 * Note that the JmsTemplate should be configured to use the designated 
	 * {@code ServiceTopic} as the {@code defaultDestination}.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * @param jmsTemplate
	 */
	@Required
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Sends the message using internal JMSTemplate, as a JMS {@code ObjectMessage}.
	 */
	public void sendServiceMessage(final ServiceMessage message) {
		log.debug("Sending Service Message : " + message);
		jmsTemplate.convertAndSend(message);
	}

}
