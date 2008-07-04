package org.nebulaframework.core.job.exceptions;

public class GridJobPermissionDeniedException extends Exception {

	private static final long serialVersionUID = -3031404025046256470L;

	public GridJobPermissionDeniedException() {
		super();
	}

	public GridJobPermissionDeniedException(String message, Throwable cause) {
		super(message, cause);
	}

	public GridJobPermissionDeniedException(String message) {
		super(message);
	}

	public GridJobPermissionDeniedException(Throwable cause) {
		super(cause);
	}

}
