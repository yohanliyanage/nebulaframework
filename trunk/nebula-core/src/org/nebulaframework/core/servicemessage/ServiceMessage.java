package org.nebulaframework.core.servicemessage;

import java.io.Serializable;

public class ServiceMessage implements Serializable {

	private static final long serialVersionUID = -4854863707920186063L;
	private ServiceMessageType type;
	private String message;

	public ServiceMessage() {
		super();
	}

	public ServiceMessage(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ServiceMessageType getType() {
		return type;
	}

	public void setType(ServiceMessageType type) {
		this.type = type;
	}

	public boolean isJobMessage() {
		return 	(type == ServiceMessageType.JOB_START) ||
				(type == ServiceMessageType.JOB_END) ||
				(type == ServiceMessageType.JOB_CANCEL);
	}

	public boolean isClusterMessage() {
		return 	(type == ServiceMessageType.CLUSTER_SHUTDOWN);
	}

	@Override
	public String toString() {
		return message;
	}

}
