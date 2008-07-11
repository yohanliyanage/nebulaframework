package org.nebulaframework.core.grid.cluster.manager.services.jobs.remote;

import org.nebulaframework.core.job.deploy.GridJobInfo;
import org.nebulaframework.core.job.exceptions.GridJobPermissionDeniedException;

//TODO FixDoc
public interface RemoteClusterJobService {

	GridJobInfo remoteJobRequest(String jobId)throws GridJobPermissionDeniedException, IllegalArgumentException;

}