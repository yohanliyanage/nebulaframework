package org.nebulaframework.core.grid.cluster.manager.services.jobs.aggregator;

import org.nebulaframework.core.grid.cluster.manager.services.jobs.GridJobProfile;

public interface AggregatorService {
	public void startAggregator(GridJobProfile profile);
	public void aggregateResults(GridJobProfile profile);
}
