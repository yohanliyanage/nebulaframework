package org.nebulaframework.configuration;

/**
 * This exception denotes an exceptional situation occurred during
 * the configuration of a Nebula Grid Member.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class ConfigurationException extends RuntimeException {

	private static final long serialVersionUID = 4653298956700502661L;

	/**
	 * Create a ConfigurationException without a message or cause.
	 */
	public ConfigurationException() {
		super();
	}

	/**
	 * Create a ConfigurationException with the given message.
	 * 
	 * @param message
	 *            Message
	 */
	public ConfigurationException(String message) {
		super(message);
	}

	/**
	 * Create a ConfigurationException with the given cause.
	 * 
	 * @param cause
	 *            Cause
	 */
	public ConfigurationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Create a ConfigurationException with the given message and cause.
	 * 
	 * @param message
	 *            Message
	 * @param cause
	 *            Cause
	 */
	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}
