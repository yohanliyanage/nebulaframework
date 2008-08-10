package org.nebulaframework.benchmark.scimark2.grid;

import org.nebulaframework.benchmark.scimark.grid.BenchmarkType;

public enum StandardBenchmarkCycles {

	FFT (14),
	SOR (15),
	MONTE_CARLO (27),
	SPARSE (17),
	LU (12);
	
	private long value = 0;
	
	private StandardBenchmarkCycles(double power) {
		this.value = (long) Math.pow(2, power);
	}
	public long value() {
		return value;
	}
	public static long getCycles(BenchmarkType type) {
		
		if (type == BenchmarkType.FFT) {
			return FFT.value();
		}
		else if (type == BenchmarkType.SOR) {
			return SOR.value();
		}
		else if (type == BenchmarkType.MONTE_CARLO) {
			return MONTE_CARLO.value();
		}
		else if (type == BenchmarkType.LU) {
			return LU.value();
		}
		
		else if (type == BenchmarkType.SPARSE) {
			return SPARSE.value();
		}
		else {
			return 0;
		}
	}
	
}
