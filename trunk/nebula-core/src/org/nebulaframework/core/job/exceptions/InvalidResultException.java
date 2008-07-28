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
package org.nebulaframework.core.job.exceptions;

import org.nebulaframework.core.job.unbounded.UnboundedGridJob;

/**
 * Thrown when a result of an {@code UnboundedGridJob}'s {@code GridTask} 
 * is invalid at the point of processing.
 * <p>
 * This exception is to be utilized to inform the framework that a
 * result given to the {@link UnboundedGridJob#processResult(Serializable)}
 * method is invalid. 
 * <p>
 * If this exception was thrown, the task will be re-enqueued.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class InvalidResultException extends Exception {

	private static final long serialVersionUID = -4090499278894658656L;

	/**
	 * Create a AggregateException without a message or cause.
	 */
	public InvalidResultException() {
		super();
	}

	/**
	 * Create a AggregateException with the given message and cause.
	 * 
	 * @param message
	 *            Message
	 * @param cause
	 *            Cause
	 */
	public InvalidResultException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a AggregateException with the given message.
	 * 
	 * @param message
	 *            Message
	 */
	public InvalidResultException(String message) {
		super(message);
	}

	/**
	 * Create a AggregateException with the given cause.
	 * 
	 * @param cause
	 *            Cause
	 */
	public InvalidResultException(Throwable cause) {
		super(cause);
	}

}
