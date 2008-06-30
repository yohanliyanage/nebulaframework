package org.nebulaframework.core.grid.cluster;

import java.io.Serializable;

public class ServiceMessage implements Serializable {
	
	private static final long serialVersionUID = -4854863707920186063L;
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

	@Override
	public String toString() {
		return message;
	}
	
	
	
}
