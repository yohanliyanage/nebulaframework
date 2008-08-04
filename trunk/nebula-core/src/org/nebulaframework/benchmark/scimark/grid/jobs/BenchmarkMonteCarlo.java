package org.nebulaframework.benchmark.scimark.grid.jobs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.nebulaframework.benchmark.scimark.Stopwatch;
import org.nebulaframework.benchmark.scimark.grid.CalculationSupport;
import org.nebulaframework.benchmark.scimark.grid.ExecutionResult;
import org.nebulaframework.benchmark.scimark.grid.tasks.MonteCarloTask;
import org.nebulaframework.core.job.splitaggregate.SplitAggregateGridJob;
import org.nebulaframework.core.task.GridTask;

public class BenchmarkMonteCarlo extends AbstractBenchmarkJob implements SplitAggregateGridJob<ExecutionResult, Double> {

	private static final long serialVersionUID = 9146126605813424591L;
	private Stopwatch sw = new Stopwatch();
	
	
	public BenchmarkMonteCarlo() {
		super();
	}

	public BenchmarkMonteCarlo(int taskCount) {
		super(taskCount);
	}

	@Override
	public List<GridTask<ExecutionResult>> split() {
		
		List<GridTask<ExecutionResult>> list = new ArrayList<GridTask<ExecutionResult>>();
		for (int i=0;i<taskCount;i++){
			list.add(new MonteCarloTask());
		}
		
		sw.start();
		return list;
	}
	
	@Override
	public Double aggregate(List<? extends Serializable> results) {
		
		sw.stop();
		
		double overallTime = sw.read();
		
		System.err.println("* MC | Overall Time" + overallTime);
		
		long cycles = 0;
		
		double calcTime = 0;
		double execTime = 0;
		
		for (Serializable result : results) {
			if (result instanceof ExecutionResult) {
				ExecutionResult exResult = (ExecutionResult) result;
				cycles += exResult.getCycles();
				calcTime += exResult.getCalculationTime();
				execTime += exResult.getExecutionTime();
			}
		}
		
		System.err.println("* MC | Cycles " + cycles);
		System.err.println("* MC | Cal Time" + calcTime);
		System.err.println("* MC | Ex Time" + execTime);
		
		double effectiveTime = (calcTime/execTime) * overallTime;
		
		System.err.println("* MC | Effective Time" + effectiveTime);
		System.err.println("* MC | MC FLOPPS" + CalculationSupport.getMonteCarlo(cycles, effectiveTime));
		return CalculationSupport.getMonteCarlo(cycles, effectiveTime);
	}

	

}
