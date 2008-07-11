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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.GridExecutionException;
import org.nebulaframework.core.GridTimeoutException;
import org.nebulaframework.core.job.SplitAggregateGridJob;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.GridJobStateListener;

/**
 * Implementation of {@link GridJobFuture} interface, which represents the 
 * result of a deployed {@code GridJob}.
 * <p>
 * {@code GridJobFuture} is returned after submitting the {@code GridJob} to the Grid. 
 * This class allows to check the status of a {@code GridJob}, to cancel 
 * execution of a {@code GridJob}, and to obtain the result of a {@code GridJob}, 
 * blocking until result is available. A {@code GridJob} can be requested to be 
 * canceled using the {@link #cancel()} method.  
 * <p>
 * The implementation of this interface resides at the {@code ClusterManager}'s JVM,
 * and is exposed as a remote service to the submitter node, using proxy classes.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see GridJobFuture
 * @see SplitAggregateGridJob
 * @see GridJobState
 */
public class GridJobFutureImpl implements GridJobFuture {

	private static Log log = LogFactory.getLog(GridJobFutureImpl.class);
	
	protected String jobId;				// GridJob Id
	protected Serializable result;		// Final Result of GridJob
	protected Exception exception;		// Exception, if failed due to one
	protected GridJobState state;		// Current state of GridJob
	
	// TODO Do we need listeners ?
	protected List<GridJobStateListener> listeners = new ArrayList<GridJobStateListener>();
	
	// Synchronization Mutex
	private Object mutex = new Object(); 

	/**
	 * Constructs a {@code GridJobFutureImpl} instance
	 * for given {@code GridJob}.
	 * 
	 * @param jobId JobId of {@code GridJob}
	 */
	public GridJobFutureImpl(String jobId) {
		super();
		
		// Set initial state
		 state = GridJobState.WAITING;
	}

	/**
	 * {@inheritDoc}
	 */	
	public boolean cancel() {
		// TODO Implement Cancel
		return false;
	}

	/**
	 * Sets the result after the {@link SplitAggregateGridJob} execution.
	 * 
	 * @param result Result
	 */
	public void setResult(Serializable result) {
		this.result = result;
	}

	/**
	 * {@inheritDoc}
	 */	
	public Serializable getResult() throws GridExecutionException {
		
		synchronized (mutex) {
			if (result == null) {
				try {
					log.debug("Waiting for Result...");
					mutex.wait();
					log.debug("Resuming after Result...");
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new RuntimeException("Interrupted while waiting for Result");
				}
			}
		}

		if (this.getState().equals(GridJobState.COMPLETE)) {
			log.debug("Returning Result");
			return this.result;
		} else {
			if (this.exception != null) {
				throw new GridExecutionException("Execution Failed", exception);
			}
			else {
				throw new GridExecutionException("Execution Failed (Job State : " + this.getState() + ")");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */	
	public Serializable getResult(long timeout) throws GridExecutionException,
			GridTimeoutException {
		// TODO Implement getResult TimeOut
		return null;
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
			if (state == GridJobState.COMPLETE || state == GridJobState.FAILED
					|| state == GridJobState.CANCELED) {
				
				// Notify waiting threads (getResult)
				mutex.notifyAll();		
				
				// Notify Listeners
				notifyListeners(state);
			}
		}
	}

	/**
	 * Internal method which notifies each registered 
	 * {@code GridJobStateListener} regarding the state
	 * change.
	 * 
	 * @param state new state
	 */
	private void notifyListeners(final GridJobState state) {
		new Thread(new Runnable() {
			public void run() {
				// Invoke state changed on each listener
				for (GridJobStateListener listener : listeners) {
					listener.stateChanged(state);
				}
			}
		}).start();
	}

	/**
	 * {@inheritDoc}
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * Sets the Exception for the {@code GridJob}, if
	 * applicable. This will be used to notify the job
	 * submitter regarding any exceptions that may have
	 * caused the {@code GridJob} to be failed.
	 * 
	 * @param exception Exception
	 */
	public void setException(Exception exception) {
		this.exception = exception;
	}

	/**
	 * Adds the given {@code GridJobStateListener} as a listener to the
	 * {@code GridJob} represented by this {@code GridJobFuture}.
	 * 
	 * @param listener {@code GridJobStateListener} to add
	 */
	public void addGridJobStateListener(GridJobStateListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * Removes the given {@code GridJobStateListener} from the collection of
	 * {@code GridJobStateListener}s of this {@code GridJobFuture}.
	 * 
	 * @param listener {@code GridJobStateListener} to remove
	 * @return a {@code boolean} value, {@code true} if success, {@code false} otherwise.
	 */
	public boolean removeGridJobStateListener(GridJobStateListener listener) {
		return this.listeners.remove(listener);
	}
}
