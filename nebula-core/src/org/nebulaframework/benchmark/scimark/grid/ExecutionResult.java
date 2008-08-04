package org.nebulaframework.benchmark.scimark.grid;

import java.io.Serializable;

import org.nebulaframework.core.task.ExecutionTimeAware;

public class ExecutionResult implements Serializable, ExecutionTimeAware {

	private static final long serialVersionUID = 8766052176725712967L;
	
	private BenchmarkType type;
	private double calculationTime;
	private double executionTime;
	private long cycles;

	public BenchmarkType getType() {
		return type;
	}

	public void setType(BenchmarkType type) {
		this.type = type;
	}

	public double getCalculationTime() {
		return calculationTime;
	}

	public void setCalculationTime(double calculationTime) {
		this.calculationTime = calculationTime;
	}

	public long getCycles() {
		return cycles;
	}

	public void setCycles(long cycles) {
		this.cycles = cycles;
	}

	public double getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(long executionTime) {
		this.executionTime = ((double)executionTime) / 1000;
	}

}
