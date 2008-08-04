package org.nebulaframework.benchmark.linpack;

import java.io.Serializable;

public class LinPackResult implements Serializable {

	private static final long serialVersionUID = -1269937958582617155L;

	private double cycles = 1;
	private double mflops;
	private double time;
	private double normRes;
	private double precision;

	
	public double getCycles() {
		return cycles;
	}

	public void setCycles(double cycles) {
		this.cycles = cycles;
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

	public double getNormRes() {
		return normRes;
	}

	public void setNormRes(double normRes) {
		this.normRes = normRes;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	@Override
	public String toString() {
		return "MFLOPS : " + mflops + "\nCycles : " + cycles + "\nTime : " + time
				+ "\nNorm Res : " + normRes + "\nPrecision : " + precision;
	}

}
