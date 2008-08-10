package org.nebulaframework.benchmark.scimark2.grid.jobs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.nebulaframework.benchmark.scimark.grid.BenchmarkType;
import org.nebulaframework.benchmark.scimark.grid.ExecutionResult;
import org.nebulaframework.benchmark.scimark2.Random;
import org.nebulaframework.benchmark.scimark2.Stopwatch;
import org.nebulaframework.benchmark.scimark2.grid.BenchmarkResult;
import org.nebulaframework.benchmark.scimark2.grid.SciMark2Task;
import org.nebulaframework.benchmark.scimark2.grid.StandardBenchmarkCycles;
import org.nebulaframework.core.job.splitaggregate.SplitAggregateGridJob;
import org.nebulaframework.core.task.GridTask;

public abstract class AbstractScimark2Job implements SplitAggregateGridJob<ExecutionResult, BenchmarkResult> {

	private static final long serialVersionUID = 3768514079250195505L;

	protected BenchmarkType type;
	protected long cyclesPerTask;
	protected int numTasks = 4;

	
	protected Random random = new Random();
	protected Stopwatch sw = new Stopwatch();

	
	
	public AbstractScimark2Job(BenchmarkType type) {
		super();
		this.type = type;
		this.cyclesPerTask = StandardBenchmarkCycles.getCycles(type);
	}

	public AbstractScimark2Job(BenchmarkType type, int numTasks) {
		this(type);
		this.numTasks = numTasks;
	}

	public AbstractScimark2Job(BenchmarkType type, long cyclesPerTask, int numTasks) {
		this(type);
		this.cyclesPerTask = cyclesPerTask;
		this.numTasks = numTasks;
	}

	@Override
	public List<GridTask<ExecutionResult>> split() {
		
		sw.reset();
		sw.start();
		
		List<GridTask<ExecutionResult>> tasks = new ArrayList<GridTask<ExecutionResult>>();
		for (int i=0; i < numTasks; i++) {
			tasks.add(new SciMark2Task(type, cyclesPerTask, random));
		}
		
		return tasks;
	}

	@Override
	public abstract BenchmarkResult aggregate(List<? extends Serializable> results);

}
