package org.nebulaframework.core.grid.cluster.node.services.job.execution;

import org.nebulaframework.core.servicemessage.ServiceMessage;

public interface JobExecutionService {
	void onServiceMessage(ServiceMessage message);
}
