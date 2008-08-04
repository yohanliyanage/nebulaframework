package org.nebulaframework.benchmark.scimark.grid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//TODO Remove
@SuppressWarnings("unused")
public class MFLOPSCalculator {

	private static final Log log = LogFactory.getLog(MFLOPSCalculator.class);
	
	private int tasks;
	private double overalltime;
	
	private double ffttime;
	private long fftcycles;
	private double fftExecTime;
	
	private double sortime;
	private long sorcycles;
	private double sorExecTime;
	
	private double montetime;
	private long montecycles;
	private double monteExecTime;
	
	private double sparsetime;
	private long sparsecycles;
	private double sparseExecTime;
	
	private double lutime;
	private long lucycles;
	private double luExecTime;

	
	public MFLOPSCalculator(int tasks, double overalltime) {
		super();
		this.tasks = tasks;
		this.overalltime = overalltime;
	}

	public void addResult(ExecutionResult result) {
		if (result.getType()==BenchmarkType.FFT) {
			fftcycles += result.getCycles();
			ffttime += result.getCalculationTime();
			fftExecTime += result.getExecutionTime();
		}
		else if (result.getType()==BenchmarkType.SOR) {
			sorcycles += result.getCycles();
			sortime += result.getCalculationTime();
			sorExecTime += result.getExecutionTime();
		}
		else if (result.getType()==BenchmarkType.MONTE_CARLO) {
			montecycles += result.getCycles();
			montetime += result.getCalculationTime();
			monteExecTime += result.getExecutionTime();
			
		}
		else if (result.getType()==BenchmarkType.SPARSE) {
			sparsecycles += result.getCycles();
			sparsetime += result.getCalculationTime();
			sparseExecTime += result.getExecutionTime();
		}
		else if (result.getType()==BenchmarkType.LU) {
			lucycles += result.getCycles();
			lutime += result.getCalculationTime();
			luExecTime += result.getExecutionTime();
		}
	}
	
	public double getFFT() {
		log.trace(CalculationSupport.getFFT(fftcycles, ffttime / tasks));
		return CalculationSupport.getFFT(fftcycles, ffttime / tasks);
	}
	
	public double getSOR() {
		log.trace(CalculationSupport.getSOR(sorcycles, sortime / tasks));
		return CalculationSupport.getSOR(sorcycles, sortime / tasks);
	}
	public double getMonteCarlo() {
		log.trace(CalculationSupport.getMonteCarlo(montecycles, montetime / tasks));
		return CalculationSupport.getMonteCarlo(montecycles, montetime / tasks);
	}
	
	public double getSparse() {
		log.trace(CalculationSupport.getSparse(sparsecycles, sparsetime / tasks));
		return CalculationSupport.getSparse(sparsecycles, sparsetime / tasks);
	}
	
	public double getLU() {
		log.trace(CalculationSupport.getLU(lucycles, lutime / tasks));
		return CalculationSupport.getLU(lucycles, lutime / tasks);
	}
	
	
	public double getGridMLFLOPS() {
		return (getFFT() + getSOR() + getMonteCarlo() + getSparse() + getLU()) / 5;
	}
	
}
