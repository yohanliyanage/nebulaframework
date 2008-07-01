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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.GridExecutionException;
import org.nebulaframework.core.GridTimeoutException;
import org.nebulaframework.core.job.GridJobState;

/**
 * Implementation of {@link GridJobFuture} interface.
 * 
 * @author Yohan Liyanage
 * 
 */
public class GridJobFutureImpl implements GridJobFuture {

	private static Log log = LogFactory.getLog(GridJobFutureImpl.class);
	private static final long serialVersionUID = 3998658173730612929L;
	String jobId = null;
	Serializable result = null;
	GridJobState state = GridJobState.WAITING;
	Object mutex = new Object(); // Synchronization Mutex

	public GridJobFutureImpl(String jobId) {
		super();
	}

	public boolean cancel() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setResult(Serializable result) {
		this.result = result;
	}

	public Serializable getResult() throws GridExecutionException {
		synchronized (mutex) {
			if (result == null) {
				try {
					log.debug("Waiting for Result...");
					mutex.wait();
					log.debug("Resuming after Result...");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		if (this.getState().equals(GridJobState.COMPLETE)) {
			log.info("Returning Result");
			return this.result;
		} else {
			throw new GridExecutionException("Execution Failed");
		}
	}

	public Serializable getResult(long timeout) throws GridExecutionException,
			GridTimeoutException {
		// TODO Auto-generated method stub
		return null;
	}

	public GridJobState getState() {
		return state;
	}

	public void setState(GridJobState state) {

		synchronized (mutex) {

			log.info("Setting State to " + state);

			this.state = state;
			if (state == GridJobState.COMPLETE || state == GridJobState.FAILED
					|| state == GridJobState.CANCELED) {
				log.info("Notifying....");
				mutex.notifyAll();
			}
		}

	}


}
