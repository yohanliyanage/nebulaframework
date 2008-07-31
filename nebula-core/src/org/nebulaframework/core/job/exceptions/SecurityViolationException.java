package org.nebulaframework.core.job.exceptions;

// TODO FixDoc
public class SecurityViolationException extends SecurityException {

	private static final long serialVersionUID = -7979918329053885097L;

	public SecurityViolationException() {
		super();
	}

	public SecurityViolationException(String message) {
		super(message);
	}

	public SecurityViolationException(Throwable cause) {
		super(cause);
	}

	public SecurityViolationException(String message, Throwable cause) {
		super(message, cause);
	}

}
