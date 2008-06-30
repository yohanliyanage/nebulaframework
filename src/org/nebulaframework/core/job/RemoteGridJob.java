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
package org.nebulaframework.core.job;

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.GridJob;
import org.nebulaframework.core.GridJobFutureImpl;

/**
 * Remotely executable GridJob wrapper.
 * @author Yohan Liyanage
 *
 */
public class RemoteGridJob implements Comparable<RemoteGridJob> {
	
	private static Log log = LogFactory.getLog(RemoteGridJob.class);
	
	private GridJob<? extends Serializable> job = null;
	private JobPriority priority = null;
	private UUID jobId = null;
	private GridJobFutureImpl future;

	public RemoteGridJob(UUID jobId, GridJob<? extends Serializable> job, JobPriority priority, GridJobFutureImpl future) {
		super();
		this.jobId = jobId;
		this.job = job;
		this.priority = priority;
		this.future = future;
	}

	public JobPriority getPriority() {
		return priority;
	}
	
	public UUID getJobId() {
		return jobId;
	}
	
	public GridJob<? extends Serializable> getJob() {
		return this.job;
	}

	
	public GridJobFutureImpl getFuture() {
		log.debug("Returning Future");
		return future;
	}

	@Override
	public int compareTo(RemoteGridJob o) {
		return this.priority.compareTo(o.priority);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RemoteGridJob) {
			return this.jobId.equals( ( (RemoteGridJob) obj).getJobId());
		}
		else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return this.jobId.hashCode();
	}

	@Override
	public String toString() {
		return this.jobId + " [ " + this.priority.toString() + " ]";
	}
	
}