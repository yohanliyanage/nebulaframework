package org.nebulaframework.benchmark.scimark.grid.jobs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.nebulaframework.benchmark.scimark.Constants;
import org.nebulaframework.benchmark.scimark.Random;
import org.nebulaframework.benchmark.scimark.Stopwatch;
import org.nebulaframework.benchmark.scimark.grid.ExecutionResult;
import org.nebulaframework.benchmark.scimark.grid.MFLOPSCalculator;
import org.nebulaframework.benchmark.scimark.grid.tasks.FFTTask;
import org.nebulaframework.benchmark.scimark.grid.tasks.LUTask;
import org.nebulaframework.benchmark.scimark.grid.tasks.MonteCarloTask;
import org.nebulaframework.benchmark.scimark.grid.tasks.SORTask;
import org.nebulaframework.benchmark.scimark.grid.tasks.SparseTask;
import org.nebulaframework.core.job.splitaggregate.SplitAggregateGridJob;
import org.nebulaframework.core.task.GridTask;

// TODO Remove
public class BenchmarkJob implements SplitAggregateGridJob<ExecutionResult, Double>{

	private static final long serialVersionUID = -5195505326303404249L;
	
	protected Random random =  new Random(Constants.RANDOM_SEED);
	protected static final int NUM_TASKS = 5;

	private Stopwatch sw;
	
	
	@Override
	public List<GridTask<ExecutionResult>> split() {
		List<GridTask<ExecutionResult>> list = new ArrayList<GridTask<ExecutionResult>>();
		
		for (int i=0; i<NUM_TASKS; i++) {
			list.add(new FFTTask(random));
			list.add(new SORTask(random));
			list.add(new MonteCarloTask());
			list.add(new SparseTask(random));
			list.add(new LUTask(random));
		}
		
		sw = new Stopwatch();
		sw.start();
		
		return list;
	}
	
	@Override
	public Double aggregate(List<? extends Serializable> results) {
		
		sw.stop();
		
		MFLOPSCalculator calc = new MFLOPSCalculator(NUM_TASKS, sw.read());
		
		for (Serializable r : results) {
			if (r instanceof ExecutionResult) {
				calc.addResult((ExecutionResult)r);
			}
		}
		
		return calc.getGridMLFLOPS();
	}


}
