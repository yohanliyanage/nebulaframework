package org.nebulaframework.core.grid.cluster.manager.services.jobs.remote;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.remoting.support.RemoteInvocation;

// TODO Fix Doc
public class RemoteJobRequestMessageConverter extends SimpleMessageConverter {

	@Override
	public Object fromMessage(Message message) throws JMSException,
			MessageConversionException {
		// TODO Auto-generated method stub
		return super.fromMessage(message);
	}

	@Override
	public Message toMessage(Object object, Session session)
			throws JMSException, MessageConversionException {

		Message m = super.toMessage(object, session);

		if (object instanceof RemoteInvocation) {
			RemoteInvocation invocation = (RemoteInvocation) object;
			if (invocation.getMethodName().equals("remoteJobRequest")) {
				m.setStringProperty("targetClusterId",
									parseClusterId((String) invocation
											.getArguments()[0]));
			}
		}

		return m;
	}

	public String parseClusterId(String jobId)
			throws MessageConversionException {
		try {
			return jobId.split("\\.")[0];
		} catch (Exception e) {
			throw new MessageConversionException(
					"Exception while parsing ClusterId", e);
		}
	}
}
