package org.nebulaframework.benchmark.scimark2.grid;

import java.io.Serializable;

import org.nebulaframework.benchmark.scimark.grid.BenchmarkType;

public class BenchmarkResult implements Serializable {

	private static final long serialVersionUID = -3382429183275774012L;
	
	private double mflops;
	private double time;
	private long cyclesPerTask;
	private long tasks;
	private BenchmarkType type;
	
	public BenchmarkResult(BenchmarkType type, double mflops, double time) {
		super();
		this.type = type;
		this.mflops = mflops;
		this.time = time;
	}

	public double getMflops() {
		return mflops;
	}

	public void setMflops(double mflops) {
		this.mflops = mflops;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public long getCyclesPerTask() {
		return cyclesPerTask;
	}

	public void setCyclesPerTask(long cyclesPerTask) {
		this.cyclesPerTask = cyclesPerTask;
	}

	public long getTasks() {
		return tasks;
	}

	public void setTasks(long tasks) {
		this.tasks = tasks;
	}
	
	public BenchmarkType getType() {
		return type;
	}

	@Override
	public String toString() {
		return type + " " + mflops + " MFLOPS ( Cycles : " + (cyclesPerTask * tasks) + " | Time : " + time + ")"; 
	}

	
}
