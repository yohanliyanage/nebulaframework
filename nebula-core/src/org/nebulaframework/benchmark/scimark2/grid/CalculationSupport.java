/*
 * Copyright (C) 2008 Yohan Liyanage. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.nebulaframework.benchmark.scimark2.grid;

import org.nebulaframework.benchmark.scimark.FFT;
import org.nebulaframework.benchmark.scimark.LU;
import org.nebulaframework.benchmark.scimark.MonteCarlo;
import org.nebulaframework.benchmark.scimark.SOR;
import org.nebulaframework.benchmark.scimark.SparseCompRow;
import org.nebulaframework.benchmark.scimark2.ScimarkGridKernel;

/**
 * SciMark Result Calculation Support Class.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class CalculationSupport {
	
	public static double getFFT(long cycles, double time) {
		return FFT.num_flops(ScimarkGridKernel.FFT_size) * cycles / time * 1.0e-6;
	}
	
	public static double getSOR(long cycles, double time) {
		return SOR.num_flops(ScimarkGridKernel.SOR_size, 
		                     ScimarkGridKernel.SOR_size, cycles) / time * 1.0e-6;
	}
	
	public static double getMonteCarlo(long cycles, double time) {
		return MonteCarlo.num_flops(cycles) / time * 1.0e-6;
	}
	
	public static double getLU(long cycles, double time) {
		return LU.num_flops(ScimarkGridKernel.LU_SIZE) * cycles / time * 1.0e-6;
		
	}
	
	public static double getSparse(long cycles, double time) {
		return SparseCompRow.num_flops(ScimarkGridKernel.SPARSE_SIZE_N, 
		                               ScimarkGridKernel.SPARSE_SIZE_NZ, 
		                               cycles) / time * 1.0e-6;
	}
}
