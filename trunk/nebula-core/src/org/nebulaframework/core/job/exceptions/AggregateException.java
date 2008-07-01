package org.nebulaframework.core.job.exceptions;

public class AggregateException extends Exception {

	private static final long serialVersionUID = -9036054729117862852L;

	public AggregateException() {
		super();
	}

	public AggregateException(String message, Throwable cause) {
		super(message, cause);
	}

	public AggregateException(String message) {
		super(message);
	}

	public AggregateException(Throwable cause) {
		super(cause);
	}

}
