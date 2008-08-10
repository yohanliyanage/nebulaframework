package org.nebulaframework.benchmark.scimark2.grid;

import org.nebulaframework.benchmark.scimark.grid.BenchmarkType;
import org.nebulaframework.benchmark.scimark.grid.ExecutionResult;
import org.nebulaframework.benchmark.scimark2.Random;
import org.nebulaframework.benchmark.scimark2.ScimarkGridKernel;
import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.grid.GridExecutionException;

public class SciMark2Task implements GridTask<ExecutionResult>{

	private static final long serialVersionUID = -317999336993652074L;

	private BenchmarkType type;
	private long cycles;
	private Random random;
	
	public SciMark2Task(BenchmarkType type, long cycles, Random random) {
		super();
		this.type = type;
		this.cycles = cycles;
		this.random = random;
	}


	@Override
	public ExecutionResult execute() throws GridExecutionException {
		
		try {
			if (type==BenchmarkType.FFT) {
				return ScimarkGridKernel.measureFFT(random, cycles); 
			}
			else if (type==BenchmarkType.SOR) {
				return ScimarkGridKernel.measureSOR(random, cycles); 
			}
			else if (type==BenchmarkType.MONTE_CARLO) {
				return ScimarkGridKernel.measureMonteCarlo(cycles); 
			}
			else if (type==BenchmarkType.SPARSE) {
				return ScimarkGridKernel.measureSparseMatmult(random, cycles); 
			}
			else if (type==BenchmarkType.LU) {
				return ScimarkGridKernel.measureLU(random, cycles); 
			}			
			else {
				throw new GridExecutionException("Unsupported Benchmark Type");
			}
		
		} catch (Exception e) {
			throw new GridExecutionException(e);
		}
		
	}

}
