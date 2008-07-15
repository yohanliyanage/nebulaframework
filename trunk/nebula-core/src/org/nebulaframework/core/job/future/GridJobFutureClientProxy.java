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
package org.nebulaframework.core.job.future;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.ResultCallback;
import org.nebulaframework.grid.GridExecutionException;
import org.nebulaframework.grid.GridTimeoutException;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.nebulaframework.util.hashing.SHA1Generator;
import org.nebulaframework.util.jms.JMSRemotingSupport;

/**
 * Client-side proxy for GridJobFuture. This proxy wraps the remote 
 * InternalGridJobFuture service proxy returned after submitting a 
 * {@code GridJob}. 
 * <p>
 * This delegates most of the operations to the remote service, but 
 * provides additional support which is not supported by the remote 
 * service, such as {@code #getResult} methods. Furthermore, this proxy 
 * attempts to minimize the remote calls.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see GridJobFuture
 */
public class GridJobFutureClientProxy implements GridJobFuture {

	private static Log log = LogFactory.getLog(GridJobFutureClientProxy.class);
	
	// Subject of this Proxy
	private InternalGridJobFuture future;
	
	private Object mutex = new Object();
	private Serializable result = null;
	private boolean jobFinished = false;
	
	/**
	 * Constructs a {@code GridJobFutureClientProxy} which
	 * will act as a proxy for the given {@code GridJobFuture}
	 * instance.
	 * 
	 * @param future subject of this proxy
	 */
	public GridJobFutureClientProxy(InternalGridJobFuture future) {
		super();
		this.future = future;
		
		// Attach a ResultCallback to track results
		if (future.isFinalResultSupported()) {
			
			addFinalResultCallback(new ResultCallback () {
				
				public void onResult(Serializable result) {
					GridJobFutureClientProxy.this.result = result;
					jobFinished = true;
					
					// Notify all waiting threads
					synchronized (mutex) {
						mutex.notifyAll();
					}
				}
				
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean cancel() throws IllegalStateException {
		if (jobFinished) {
			throw new IllegalStateException("Job has finished. Cancel not supported");
		}
		return future.cancel();
	}

	/**
	 * {@inheritDoc}
	 */
	public Exception getException() {
		if (!jobFinished) {
			throw new IllegalStateException("Job has finished. getException not supported");
		}
		return (result instanceof Exception) ? (Exception) result : null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Serializable getResult() throws GridExecutionException, IllegalStateException {
		try {
			return getResult(0);
		} catch (GridTimeoutException e) {
			throw new AssertionError("Unexpected GridTimeoutException");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Serializable getResult(long timeout) throws GridExecutionException,
			GridTimeoutException, IllegalStateException {
		
		// If final result is not supported, exception (i.e. Unbounded Jobs)
		if (!future.isFinalResultSupported()) {
			 throw new IllegalStateException("GridJob does not support final results");
		}
		
		// This sync block waits until result is available.
		
		// Note that in case of timeouts, our own time tracking is needed
		// as a waking up due to unknown reasons may reset the timeouts
		synchronized (mutex) {
			try {
				// If job is not finished, wait till it finishes
				while (!isJobFinished()) {
					
					// Mark start time
					long tStart = new Date().getTime();
					
					// Wait for result
					mutex.wait(timeout);
					
					// Mark current time
					long tNow = new Date().getTime();
					
					// Check for timeout
					if (timeout > 0 && !isJobFinished()) {
						
						if ((tNow - tStart) > timeout ) {
							// timeout has occurred
							throw new GridTimeoutException("Timeout, Result Not Available");
						}
						else {
							// notified before timeout, update timeout
							timeout -= (tNow - tStart);
						}
					}
				}
			} catch (InterruptedException e) {
				log.error(e);
				throw new RuntimeException(
						"Interrupted while waiting for Result");
			}
		}

			if ( !(result instanceof Exception)) {
				return result;
			} else {
				throw new GridExecutionException("Execution Failed", (Exception) result);

		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isJobFinished() {
		return jobFinished;
	}

	/**
	 * {@inheritDoc}
	 */
	public GridJobState getState() {
		if (!isJobFinished()) {
			return future.getState();
		}
		else {
			if (!( result instanceof Exception)) {
				return GridJobState.COMPLETE;
			}
			else {
				return GridJobState.FAILED;
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addFinalResultCallback(ResultCallback callback) {
		// Remote Enable callback and add it
		String queueName = exposeCallback(callback);
		future.addFinalResultCallback(queueName);
	}

	/**
	 * Exposes the given {@code ResultCallback} by remote
	 * enabling it using JMS Remoting API.
	 * 
	 * @param callback {@code ResultCallback} to be remote 
	 * enabled
	 * 
	 * @return Name of the Queue to be used for communication 
	 * with callback
	 */
	private String exposeCallback(ResultCallback callback) {

		// Generate QueueName [nebula.job.callback.<SHA1>]
		String queueName = "nebula.job.callback."
				+ SHA1Generator.generate("" + GridNode.getInstance().getClusterId()
						+ GridNode.getInstance().getId() + UUID.randomUUID());

		ConnectionFactory cf = GridNode.getInstance().getConnectionFactory();
		JMSRemotingSupport.createService(cf, queueName, callback, ResultCallback.class);
		
		return queueName;
	}
}
