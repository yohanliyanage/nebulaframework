/*
 * Copyright (C) 2008 Yohan Liyanage. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
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
