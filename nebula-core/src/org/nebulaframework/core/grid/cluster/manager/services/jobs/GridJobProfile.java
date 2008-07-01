package org.nebulaframework.core.grid.cluster.manager.services.jobs;

import java.io.Serializable;
import java.util.UUID;

import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.future.GridJobFutureImpl;

public class GridJobProfile {
	
	private String jobId;
	private UUID owner;
	private GridJob<? extends Serializable> job;
	private GridJobFutureImpl future;

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


}
