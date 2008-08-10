package org.nebulaframework.benchmark.scimark2.grid.jobs;

import java.io.Serializable;
import java.util.List;

import org.nebulaframework.benchmark.scimark.grid.BenchmarkType;
import org.nebulaframework.benchmark.scimark2.grid.BenchmarkResult;
import org.nebulaframework.benchmark.scimark2.grid.CalculationSupport;

public class SciMark2FFTBenchmark extends AbstractScimark2Job {

	
	private static final long serialVersionUID = 3867011193337239400L;

	public SciMark2FFTBenchmark(int numTasks) {
		super(BenchmarkType.FFT, numTasks);
	}

	public SciMark2FFTBenchmark(long cyclesPerTask,
			int numTasks) {
		super(BenchmarkType.FFT, cyclesPerTask, numTasks);
	}

	public SciMark2FFTBenchmark() {
		super(BenchmarkType.FFT);
	}

	@Override
	public BenchmarkResult aggregate(List<? extends Serializable> results) {
		sw.stop();
		double time = sw.read();
		
		int tasks = results.size();
		
		// Calculate MFLOPS
		double mflops =	CalculationSupport.getFFT(cyclesPerTask*tasks, time);
		
		BenchmarkResult result = new BenchmarkResult(type, mflops, time);
		result.setCyclesPerTask(cyclesPerTask);
		result.setTasks(tasks);
		
		return result;
	}

}
