package org.nebulaframework.benchmark.linpack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.nebulaframework.core.job.splitaggregate.SplitAggregateGridJob;
import org.nebulaframework.core.task.GridTask;
import org.springframework.util.StopWatch;

public class LinPackBenchmark implements SplitAggregateGridJob<LinPackResult, Double> {

	private static final long serialVersionUID = 6153976750227917794L;

	private int tasks = 5;
	private int cycles = 1;
	
	private transient StopWatch sw = null;
	
	public LinPackBenchmark() {
		super();
	}

	
	public LinPackBenchmark(int tasks) {
		this.tasks = tasks;
	}

	

	public LinPackBenchmark(int tasks, int cycles) {
		super();
		this.tasks = tasks;
		this.cycles = cycles;
	}


	@Override
	public List<GridTask<LinPackResult>> split() {
		List<GridTask<LinPackResult>> list = new ArrayList<GridTask<LinPackResult>>();
		for (int i=0; i < tasks; i++) {
			list.add(new LinPackTask(cycles));
		}
		
		sw = new StopWatch();
		
		sw.start();
		
		return list;
	}
	
	@Override
	public Double aggregate(List<? extends Serializable> results) {
		
		sw.stop();
		double timeSecs = sw.getTotalTimeSeconds();
		
		int cycleCount = 0;
		
		for (Serializable r : results) {
			if (r instanceof LinPackResult) {
				
				// We do not check for any value from result
				// as the number of floating point operations
				// are constant for each task
				
				// A better solution is to check the normRes
				// for extreme conditions and ignore
				// such results
				
				cycleCount+=cycles;
			}
		}
		
		return Linpack.calculateMFLOPS(cycleCount, timeSecs);
	}



}
