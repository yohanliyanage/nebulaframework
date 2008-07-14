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

package org.nebulaframework.core.job.deploy;

import java.io.Externalizable;
import java.io.Serializable;

import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.grid.cluster.manager.services.jobs.ClusterJobService;

/**
 * Holds information regarding an active GridJob, and delivered
 * by the {@code ClusterJobService} at the time of a {@code GridNode}
 * registration for a {@code GridJob}.
 * <p>
 * This class implements {@link Externalizable} interface, instead of {@link Serializable}
 * to improve performance in communications, by reducing the data transfer amount and
 * serialization time [Grosso, W. 2001. "Java RMI", Section 10.7.1].
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClusterJobService#requestJob(String)
 */
public class GridJobInfo implements Serializable {

	private static final long serialVersionUID = 1792194789111207025L;
	
	private String jobId;
	private GridArchive archive;

	/**
	 * Constructs a {@code GridJobInfo} instance for
	 * given JobID.
	 * 
	 * @param jobId Job ID of {@code GridJob}
	 */
	public GridJobInfo(String jobId) {
		super();
		this.jobId = jobId;
	}

	/**
	 * Returns the {@code GridArchive} for this {@code GridJob},
	 * if exists, or {@code null}.
	 * 
	 * @return Reference of {@code GridArchive} or {@code null} if none.
	 */
	public GridArchive getArchive() {
		return archive;
	}

	/**
	 * Sets the {@code GridArchive} for this {@code GridJob}.
	 * 
	 * @param archive {@code GridArchive}
	 */
	public void setArchive(GridArchive archive) {
		this.archive = archive;
	}

	/**
	 * Returns the Job Identifier for this {@code GridJob}.
	 * @return JobId of {@code GridJob}.
	 */
	public String getJobId() {
		return jobId;
	}
	
	/**
	 * Returns a {@code boolean} value indicating whether this {@code GridJob} is
	 * an archived {@code GridJob} or not.
	 * 
	 * @return if archived {@code true}, otherwise {@code false}.
	 */
	public boolean isArchived() {
		return this.archive != null;
	}

	// TODO Externalizable ?
//	/**
//	 * {@inheritDoc}
//	 */
//	public void readExternal(ObjectInput in) throws IOException,
//			ClassNotFoundException {
//		this.jobId = in.readUTF();
//		this.archive = (GridArchive) in.readObject();
//	}
//
//	/**
//	 * {@inheritDoc}
//	 */	
//	public void writeExternal(ObjectOutput out) throws IOException {
//		out.writeUTF(jobId);
//		out.writeObject(archive);
//	}

}
