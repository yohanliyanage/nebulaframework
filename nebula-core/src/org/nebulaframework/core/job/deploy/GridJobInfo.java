package org.nebulaframework.core.job.deploy;

import java.io.Serializable;

import org.nebulaframework.core.job.archive.GridArchive;

public class GridJobInfo implements Serializable {
	private static final long serialVersionUID = 4216702256543231614L;

	private String jobId;
	private GridArchive archive;

	public GridJobInfo(String jobId) {
		super();
		this.jobId = jobId;
	}

	public GridArchive getArchive() {
		return archive;
	}

	public void setArchive(GridArchive archive) {
		this.archive = archive;
	}

	public String getJobId() {
		return jobId;
	}
	
	public boolean isArchived() {
		return this.archive != null;
	}

}
