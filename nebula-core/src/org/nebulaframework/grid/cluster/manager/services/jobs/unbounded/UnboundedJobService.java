package org.nebulaframework.grid.cluster.manager.services.jobs.unbounded;

import org.nebulaframework.grid.cluster.manager.services.jobs.GridJobProfile;

public interface UnboundedJobService {

	void startJobProcessing(GridJobProfile profile);
}
