package org.nebulaframework.core.grid.cluster.manager.services.jobs.unbounded;

import org.nebulaframework.core.grid.cluster.manager.services.jobs.GridJobProfile;

public interface UnboundedJobService {

	void startJobProcessing(GridJobProfile profile);
}
