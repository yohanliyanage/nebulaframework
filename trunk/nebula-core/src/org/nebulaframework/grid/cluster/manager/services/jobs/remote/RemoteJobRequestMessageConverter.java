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

package org.nebulaframework.grid.cluster.manager.services.jobs.remote;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.remoting.support.RemoteInvocation;

/**
 * A JMS {@code MessageConverter} which appends meta-data to
 * {@code RemoteClusterJobService} messages. This converter
 * attaches a property to each JMS Message sent by the service
 * to indicate the target Cluster ID. This ensures that the
 * message is dispatched to the intended cluster's RemoteJobService
 * only.
 * <p>
 * <i>Spring Managed</i>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
class RemoteJobRequestMessageConverter extends SimpleMessageConverter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object fromMessage(Message message) throws JMSException,
			MessageConversionException {
		
		// No Special Operation
		return super.fromMessage(message);
	}

	/**
	 * Attaches a String property to the JMS Messages which
	 * indicates the target cluster's Cluster ID.
	 * 
	 * @param object Object to be converted
	 * @param JMS Session
	 * 
	 * @return JMS Message
	 */
	@Override
	public Message toMessage(Object object, Session session)
			throws JMSException, MessageConversionException {

		Message m = super.toMessage(object, session);

		if (object instanceof RemoteInvocation) {
			RemoteInvocation invocation = (RemoteInvocation) object;
			
			// If its a invocation of remoteJobRequest
			if (invocation.getMethodName().equals("remoteJobRequest")) {
				
				// Attach Target ClusterID
				m.setStringProperty("targetClusterId",
									parseClusterId((String) invocation
											.getArguments()[0]));
			}
		}

		return m;
	}

	/**
	 * Returns the ClusterID of a given JobID.
	 * <p>
	 * Note that JobID format specifies that
	 * a job ID should be,
	 * <pre>
	 *     <i>ClusterId</i>.<i>NodeId</i>.<i>RandomUUID</i>
	 * </pre>
	 * 
	 * @param jobId JobId
	 * @return ClusterId
	 * @throws MessageConversionException if JobId is invalid
	 */
	private String parseClusterId(String jobId)
			throws MessageConversionException {
		try {
			return jobId.split("\\.")[0];
		} catch (Exception e) {
			throw new MessageConversionException(
					"Exception while parsing ClusterId", e);
		}
	}
}
