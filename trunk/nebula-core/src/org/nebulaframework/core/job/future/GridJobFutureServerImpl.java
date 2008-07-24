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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.GridJobStateListener;
import org.nebulaframework.core.job.ResultCallback;
import org.nebulaframework.core.job.SplitAggregateGridJob;
import org.nebulaframework.core.job.UnboundedGridJob;
import org.nebulaframework.grid.GridExecutionException;
import org.nebulaframework.grid.GridTimeoutException;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.manager.services.jobs.InternalClusterJobService;
import org.nebulaframework.grid.cluster.manager.support.CleanUpSupport;
import org.nebulaframework.util.jms.JMSRemotingSupport;

/**
 * Implementation of {@link GridJobFuture} interface, which represents the
 * result of a deployed {@code GridJob}.
 * <p>
 * A remote reference to this is returned after submitting the {@code GridJob}
 * to the Grid. This class allows to check the status of a {@code GridJob}, to
 * cancel execution of a {@code GridJob}, and to obtain the result of a
 * {@code GridJob}, blocking until result is available. A {@code GridJob} can
 * be requested to be canceled using the {@link #cancel()} method.
 * <p>
 * The implementation of this interface resides at the {@code ClusterManager}'s
 * JVM, and is exposed as a remote service to the submitter node, using proxy
 * classes.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see GridJobFuture
 * @see GridJob
 * @see GridJobState
 */
public class GridJobFutureServerImpl implements InternalGridJobFuture, GridJobFuture {

	private static Log log = LogFactory.getLog(GridJobFutureServerImpl.class);

	private String jobId; // GridJob Id
	private Serializable result; // Final Result of GridJob
	private Exception exception; // Exception, if failed due to one
	private GridJobState state; // Current state of GridJob
	private boolean finalResultSupported = false; // Final result allowed
	
	private InternalClusterJobService jobService;	// Job Service of CM

	ExecutorService executorService = Executors.newCachedThreadPool();
	
	// Server-side Listeners (in ClusterManager's VM)
	private List<GridJobStateListener> serverListeners = new ArrayList<GridJobStateListener>();
	

	// Synchronization Mutex
	private Object mutex = new Object();

	/**
	 * Constructs a {@code GridJobFutureImpl} instance for given {@code GridJob}.
	 * 
	 * @param jobId
	 *            JobId of {@code GridJob}
	 */
	public GridJobFutureServerImpl(String jobId, InternalClusterJobService jobService) {
		super();

		// Set initial state
		this.jobId = jobId;
		this.state = GridJobState.WAITING;
		this.jobService = jobService;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean cancel() {
		boolean result =  jobService.cancelJob(jobId);
		
		if (result) {
			this.setState(GridJobState.CANCELED);
		}
		
		return result;
	}

	/**
	 * Sets the result after the {@link GridJob} execution.
	 * 
	 * @param result
	 *            Result
	 */
	public void setResult(Serializable result) {
		this.result = result;
	}



	/**
	 * Returns {@code true} if this {@code GridJob} supports
	 * final result. Currently {@link SplitAggregateGridJob}
	 * supports final result, where as {@link UnboundedGridJob}
	 * does not.
	 * 
	 * @return value @code true} final result supported
	 */
	public boolean isFinalResultSupported() {
		return finalResultSupported;
	}

	/**
	 * {@inheritDoc}
	 */
	public GridJobState getState() {
		return state;
	}

	/**
	 * Sets the state of the {@code GridJob} of this {@code GridJobFutureImpl}
	 * to the given {@link GridJobState}.
	 * 
	 * @param state
	 */
	public void setState(GridJobState state) {
		synchronized (mutex) {
			
			this.state = state;
			
			// Notify Listeners
			notifyListeners(state);
			
			if (isJobFinished()) {
				// Notify waiting threads (getResult)
				mutex.notifyAll();
			}
		}
		

	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isJobFinished() {
		return (state == GridJobState.COMPLETE || 
				state == GridJobState.FAILED || 
				state == GridJobState.CANCELED);
	}
	
	/**
	 * Internal method which notifies each registered
	 * {@code GridJobStateListener} regarding the state change.
	 * 
	 * @param state
	 *            new state
	 */
	private void notifyListeners(final GridJobState state) {
		new Thread(new Runnable() {
			public void run() {
				// Invoke state changed on each listener
				for (final GridJobStateListener listener : serverListeners) {
					executorService.execute(new Runnable() {

						public void run() {
							try {
								listener.stateChanged(state);
							} catch (RuntimeException e) {
								log.error("[StateListener] Exception on GridJobStateListener - " + e.getMessage());
							}
						}
					});
				}
			}
		}).start();
	}

	/**
	 * {@inheritDoc}
	 */
	public Exception getException() {
		if (!isJobFinished()) throw new IllegalStateException("Job still executing");
		return exception;
	}

	/**
	 * Sets the Exception for the {@code GridJob}, if applicable. This will be
	 * used to notify the job submitter regarding any exceptions that may have
	 * caused the {@code GridJob} to be failed.
	 * 
	 * @param exception
	 *            Exception
	 */
	public void setException(Exception exception) {
		this.exception = exception;
	}

	/**
	 * Adds the given {@code GridJobStateListener} as a listener to the
	 * {@code GridJob} represented by this {@code GridJobFuture}.
	 * 
	 * @param listener
	 *            {@code GridJobStateListener} to add
	 */
	public void addGridJobStateListener(GridJobStateListener listener) {
		this.serverListeners.add(listener);
	}

	/**
	 * Removes the given {@code GridJobStateListener} from the collection of
	 * {@code GridJobStateListener}s of this {@code GridJobFuture}.
	 * 
	 * @param listener
	 *            {@code GridJobStateListener} to remove
	 * @return a {@code boolean} value, {@code true} if success, {@code false}
	 *         otherwise.
	 */
	public boolean removeGridJobStateListener(GridJobStateListener listener) {
		return this.serverListeners.remove(listener);
	}

	/**
	 * Sets whether the {@code GridJob} supports final
	 * result.
	 * 
	 * @param finalResultSupported boolean value
	 */
	public void setFinalResultSupported(boolean finalResultSupported) {
		this.finalResultSupported = finalResultSupported;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <b>Not Supported, invoking this method causes {@code UnsupportedOperationException}.</b>
	 * This is only supported in the ClientSideProxy of GridJobFuture.
	 * 
	 */
	public void addFinalResultCallback(ResultCallback callback) {
		throw new UnsupportedOperationException("Only supported by Client Side Proxy");
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addFinalResultCallback(String queueName) throws IllegalStateException {
		
		// If final result is not supported, exception (i.e. Unbounded Jobs)
		if (!isFinalResultSupported()) {
			 throw new IllegalStateException("GridJob does not support final results");
		}
		
		// Create Proxy for callback
		ConnectionFactory cf = ClusterManager.getInstance().getConnectionFactory();
		final ResultCallback callback = JMSRemotingSupport.createProxy (cf, queueName, ResultCallback.class);
		
		// Clean Up Hook
		CleanUpSupport.removeQueueWhenFinished(jobId, queueName);
		
		Thread t = new Thread(new Runnable(){

			public void run() {
				
				// This sync block waits until result is available.
				synchronized (mutex) {
					try {
						// If job is not finished, wait till it finishes
						while (!isJobFinished()) {
							// Wait for result
							mutex.wait();
						}
						
						// Job Finished
						if (state==GridJobState.COMPLETE) {
							callback.onResult(result);
						}
						else {
							Exception e = null;
							
							// If we got an exception while execution
							if (exception!=null) {
								e = new GridExecutionException("Execution Failed", exception);
							}
							else { // If failed for other reason
								e = new GridExecutionException("Execution Failed (Job State : " + getState() + ")");
								
							}
							// Send Exception 
							callback.onResult(e);
						}
					} catch (InterruptedException e) {
						log.error(e);
						throw new RuntimeException("Interrupted while waiting for Result");
					}
				}				
			}
			
		});
		
		// Make the thread to be daemon, because if this thread never gets notified,
		// we don't want the cluster manager to wait for this thread to finish.
		t.setDaemon(true);
		t.start();
		
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <b>Not Supported, invoking this method causes {@code UnsupportedOperationException}.</b>
	 * This is only supported in the ClientSideProxy of GridJobFuture.
	 * 
	 */
	public Serializable getResult() throws GridExecutionException,
			IllegalStateException {
		throw new UnsupportedOperationException("Only supported by Client Side Proxy");
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <b>Not Supported, invoking this method causes {@code UnsupportedOperationException}.</b>
	 * This is only supported in the ClientSideProxy of GridJobFuture.
	 * 
	 */
	public Serializable getResult(long timeout) throws GridExecutionException,
			GridTimeoutException, IllegalStateException {
		throw new UnsupportedOperationException("Only supported by Client Side Proxy");
	}
	
}
