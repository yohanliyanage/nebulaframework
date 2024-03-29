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

package org.nebulaframework.grid.cluster.node.services.job.submission;

import java.io.Serializable;
import java.util.Map;

import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.ResultCallback;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.exceptions.GridJobRejectionException;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.nebulaframework.grid.cluster.manager.services.facade.ClusterManagerServicesFacade;

/**
 * Allows the {@code GridNode} to submit {@code GridJob}s to the Grid, through
 * the {@code ClusterManager} of the local cluster.
 * <p>
 * The {@code JobSubmissionService} uses the {@link ClusterManagerServicesFacade}
 * to submit jobs to the Cluster.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * @see GridJob
 * @see GridArchive
 * @see GridJobFuture
 * @see ClusterManagerServicesFacade
 */
public interface JobSubmissionService {
	
	/**
	 * Submits the given <b>Annotated {@code GridJob}</b> to the Grid through {@code ClusterManager}.
	 * <p>
	 * This method dynamically adapts the given Serializable object as a GridJob and wraps
	 * it in an GridJobAdpater, if properly annotated. If not properly annotated or
	 * if annotation processing fails due to any exception, the job will be rejected.
	 * Note that both job class and task class (if task is not in job class itself)
	 * must be serializable.
	 * <p>
	 * 
	 * If successful, returns a (proxy) reference to {@code GridJobFuture} for the Job.
	 * If failed to submit, it throws an unchecked {@code GridJobRejectionException}.
	 * 
	 * @param job {@code GridJob} Job to submit
	 * 
	 * @return {@code GridJobFuture} proxy
	 * 
	 * @throws GridJobRejectionException if submission failed
	 */
	public GridJobFuture submitJob(Serializable annotatedJob) throws GridJobRejectionException;
	
	
	/**
	 * Submits the given <b>Annotated {@code GridJob}</b> to the Grid through {@code ClusterManager}, and
	 * attaches the given {@code ResultCallback} to obtain intermediate results.
	 * <p>
	 * This method dynamically adapts the given Serializable object as a GridJob and wraps
	 * it in an GridJobAdpater, if properly annotated. If not properly annotated or
	 * if annotation processing fails due to any exception, the job will be rejected.
	 * Note that both job class and task class (if task is not in job class itself)
	 * must be serializable.
	 * <p>
	 * If successful, returns a (proxy) reference to {@code GridJobFuture} for the Job.
	 * If failed to submit, it throws an unchecked {@code GridJobRejectionException}.
	 * 
	 * @param job {@code GridJob} Job to submit
	 * @param callback {@code ResultCallback} object
	 * 
	 * @return {@code GridJobFuture} proxy
	 * 
	 * @throws GridJobRejectionException if submission failed
	 */
	public GridJobFuture submitJob(Serializable annotatedJob, ResultCallback callback) throws GridJobRejectionException;
	
	
	/**
	 * Submits the given {@code GridJob} to the Grid through {@code ClusterManager}.
	 * If successful, returns a (proxy) reference to {@code GridJobFuture} for the Job.
	 * If failed to submit, it throws an unchecked {@code GridJobRejectionException}.
	 * 
	 * @param job {@code GridJob} Job to submit
	 * 
	 * @return {@code GridJobFuture} proxy
	 * 
	 * @throws GridJobRejectionException if submission failed
	 */
	public GridJobFuture submitJob(GridJob<?,?> job) throws GridJobRejectionException;
	
	
	/**
	 * Submits the given {@code GridJob} to the Grid through {@code ClusterManager}, and
	 * attaches the given {@code ResultCallback} to obtain intermediate results.
	 * If successful, returns a (proxy) reference to {@code GridJobFuture} for the Job.
	 * If failed to submit, it throws an unchecked {@code GridJobRejectionException}.
	 * 
	 * @param job {@code GridJob} Job to submit
	 * @param callback {@code ResultCallback} object
	 * 
	 * @return {@code GridJobFuture} proxy
	 * 
	 * @throws GridJobRejectionException if submission failed
	 */
	public GridJobFuture submitJob(GridJob<?,?> job, ResultCallback callback) throws GridJobRejectionException;
	
	/**
	 * Submits the given {@code GridArchive} to the Grid through {@code ClusterManager}.
	 * <p>
	 * Each {@code GridJob} with in the {@code GridArchive will be submitted separately.
	 * For each successful submission, the {@code GridJobFuture} will be inserted to a
	 * Map, against the fully qualified class name of the {@code GridJob} class. If a 
	 * {@code GridJob} fails to submit, a {@code null} value will be inserted in place
	 * of the {@code GridJobFuture}.
	 * <p>
	 * Note that this method does not throw any exception on failure to 
	 * submit a {@code GridJob}.
	 * 
	 * @param archive {@code GridArchive} to submit
	 * 
	 * @return A {@code Map> containing {@code GridJobFuture} against fully
	 * qualified class name of the {@code GridJob} class
	 */
	public Map<String, GridJobFuture> submitArchive(GridArchive archive) throws GridJobRejectionException;
	
	/**
	 * Submits the given {@code GridArchive} to the Grid through {@code ClusterManager}
	 * and attaches the given {@code ResultCallback}s to obtain intermediate results.
	 * <p>
	 * Each {@code GridJob} with in the {@code GridArchive will be submitted separately.
	 * For each successful submission, the {@code GridJobFuture} will be inserted to a
	 * Map, against the fully qualified class name of the {@code GridJob} class. If a 
	 * {@code GridJob} fails to submit, a {@code null} value will be inserted in place
	 * of the {@code GridJobFuture}.
	 * <p>
	 * Note that this method does not throw any exception on failure to 
	 * submit a {@code GridJob}.
	 * 
	 * @param archive {@code GridArchive} to submit
	 * @param callbacks A {@link Map} which provides callbacks to be used
	 * against fully qualified class names of GridJobs.
	 * 
	 * @return A {@code Map> containing {@code GridJobFuture} against fully
	 * qualified class name of the {@code GridJob} class
	 */
	public Map<String, GridJobFuture> submitArchive(GridArchive archive, Map<String, ResultCallback> callbacks) throws GridJobRejectionException;;
}
