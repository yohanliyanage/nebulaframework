package org.nebulaframework.core.job.exceptions;

public class GridJobRejectionException extends RuntimeException {

	private static final long serialVersionUID = 175575667877619537L;

	public GridJobRejectionException() {
	}

	public GridJobRejectionException(String message) {
		super(message);
	}

	public GridJobRejectionException(Throwable cause) {
		super(cause);
	}

	public GridJobRejectionException(String message, Throwable cause) {
		super(message, cause);
	}

}
