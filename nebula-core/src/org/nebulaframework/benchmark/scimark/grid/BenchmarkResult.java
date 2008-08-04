package org.nebulaframework.benchmark.scimark.grid;

import java.io.Serializable;

public class BenchmarkResult implements Serializable {

	private static final long serialVersionUID = 7437300621771918367L;
	
	private double fft;
	private double sor;
	private double monteCarlo;
	private double lu;
	private double sparse;

	public double getFFT() {
		return fft;
	}

	public void setFFT(double fft) {
		this.fft = fft;
	}

	public double getSOR() {
		return sor;
	}

	public void setSOR(double sor) {
		this.sor = sor;
	}

	public double getMonteCarlo() {
		return monteCarlo;
	}

	public void setMonteCarlo(double monteCarlo) {
		this.monteCarlo = monteCarlo;
	}

	public double getLU() {
		return lu;
	}

	public void setLU(double lu) {
		this.lu = lu;
	}

	public double getSparse() {
		return sparse;
	}

	public void setSparse(double sparse) {
		this.sparse = sparse;
	}

	public double finalMFLOPS() {
		return ((fft + lu + sor + monteCarlo + sparse) / 5);
	}
}
