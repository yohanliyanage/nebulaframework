package org.nebulaframework.benchmark.scimark.grid;

import org.nebulaframework.benchmark.scimark.CustomKernel;
import org.nebulaframework.benchmark.scimark.FFT;
import org.nebulaframework.benchmark.scimark.LU;
import org.nebulaframework.benchmark.scimark.MonteCarlo;
import org.nebulaframework.benchmark.scimark.SOR;
import org.nebulaframework.benchmark.scimark.SparseCompRow;


public class CalculationSupport {
	
	public static double getFFT(long cycles, double time) {
		return FFT.num_flops(CustomKernel.FFT_size) * cycles / time * 1.0e-6;
	}
	
	public static double getSOR(long cycles, double time) {
		return SOR.num_flops(CustomKernel.SOR_size, 
		                     CustomKernel.SOR_size, cycles) / time * 1.0e-6;
	}
	
	public static double getMonteCarlo(long cycles, double time) {
		return MonteCarlo.num_flops(cycles) / time * 1.0e-6;
	}
	
	public static double getLU(long cycles, double time) {
		return LU.num_flops(CustomKernel.LU_SIZE) * cycles / time * 1.0e-6;
		
	}
	
	public static double getSparse(long cycles, double time) {
		return SparseCompRow.num_flops(CustomKernel.SPARSE_SIZE_N, 
		                               CustomKernel.SPARSE_SIZE_NZ, 
		                               cycles) / time * 1.0e-6;
	}
}
