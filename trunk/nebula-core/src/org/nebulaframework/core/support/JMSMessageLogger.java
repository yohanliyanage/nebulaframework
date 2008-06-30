package org.nebulaframework.core.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JMSMessageLogger {

	private static Log log  = LogFactory.getLog(JMSMessageLogger.class);
	
	public void logMessage(String msg) {
		log.debug("[MSG LOG] <STRING> " + msg);
	}
	
	public void logMessage(Object msg) {
		log.debug("[MSG LOG] <OBJECT> " + msg);
	}
}
