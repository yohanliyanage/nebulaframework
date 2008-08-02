package org.nebulaframework.discovery;

import org.nebulaframework.configuration.ConfigurationException;

/**
 * This exception denotes an situation where discovery has failed
 * to discover Grid members.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class DiscoveryFailureException extends ConfigurationException {

	private static final long serialVersionUID = -8726156918367455610L;
	/**
	 * Create a DiscoveryFailureException without a message or cause.
	 */
	public DiscoveryFailureException() {
		super();
	}

	/**
	 * Create a DiscoveryFailureException with the given message.
	 * 
	 * @param message
	 *            Message
	 */
	public DiscoveryFailureException(String message) {
		super(message);
	}

	/**
	 * Create a DiscoveryFailureException with the given cause.
	 * 
	 * @param cause
	 *            Cause
	 */
	public DiscoveryFailureException(Throwable cause) {
		super(cause);
	}

	/**
	 * Create a DiscoveryFailureException with the given message and cause.
	 * 
	 * @param message
	 *            Message
	 * @param cause
	 *            Cause
	 */
	public DiscoveryFailureException(String message, Throwable cause) {
		super(message, cause);
	}

}
