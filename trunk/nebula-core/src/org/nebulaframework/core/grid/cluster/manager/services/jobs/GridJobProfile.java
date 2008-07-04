package org.nebulaframework.core.grid.cluster.manager.services.jobs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.future.GridJobFutureImpl;
import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.core.task.GridTaskResult;

public class GridJobProfile {
	
	private String jobId;
	private UUID owner;
	private GridJob<? extends Serializable> job;
	private GridJobFutureImpl future;
	private GridArchive archive;
	
	private Map<Integer, GridTask<? extends Serializable>> taskMap = new HashMap<Integer, GridTask<? extends Serializable>>();
	private Map<Integer, GridTaskResult> resultMap = new HashMap<Integer, GridTaskResult>();
	
	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public UUID getOwner() {
		return owner;
	}

	public void setOwner(UUID owner) {
		this.owner = owner;
	}

	public GridJob<? extends Serializable> getJob() {
		return job;
	}

	public void setJob(GridJob<? extends Serializable> job) {
		this.job = job;
	}

	public GridJobFutureImpl getFuture() {
		return future;
	}

	public void setFuture(GridJobFutureImpl future) {
		this.future = future;
	}

	public Map<Integer, GridTask<? extends Serializable>> getTaskMap() {
		return taskMap;
	}

	public Map<Integer, GridTaskResult> getResultMap() {
		return resultMap;
	}

	public GridArchive getArchive() {
		return archive;
	}

	public void setArchive(GridArchive archive) {
		this.archive = archive;
	}

	public boolean isArchived() {
		return this.archive!=null;
	}
	
	

}
