package org.nebulaframework.benchmark.scimark.grid.jobs;

import java.io.Serializable;
import java.util.List;

import org.nebulaframework.benchmark.scimark.Constants;
import org.nebulaframework.benchmark.scimark.Random;
import org.nebulaframework.benchmark.scimark.grid.ExecutionResult;
import org.nebulaframework.core.job.splitaggregate.SplitAggregateGridJob;
import org.nebulaframework.core.task.GridTask;

public abstract class AbstractBenchmarkJob implements SplitAggregateGridJob<ExecutionResult, Double> {

	private static final long serialVersionUID = 6956932641722913717L;
	
	protected Random random =  new Random(Constants.RANDOM_SEED);
	protected int taskCount = 5;
	
	

	public AbstractBenchmarkJob() {
		super();
	}

	public AbstractBenchmarkJob(int taskCount) {
		super();
		this.taskCount = taskCount;
	}




	@Override
	public abstract List<GridTask<ExecutionResult>> split();



	@Override
	public abstract Double aggregate(List<? extends Serializable> results);


}
