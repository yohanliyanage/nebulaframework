package org.nebulaframework.benchmark.scimark.grid.tasks;

import org.nebulaframework.benchmark.scimark.grid.ExecutionResult;
import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.grid.GridExecutionException;

public abstract class AbstractBenchmarkTask implements GridTask<ExecutionResult> {

	
	@Override
	public ExecutionResult execute() throws GridExecutionException {
		try {
			return doExecute();
		} catch (Exception e) {
			throw new GridExecutionException(e);
		}
	}

	public abstract ExecutionResult doExecute() throws Exception;

}
