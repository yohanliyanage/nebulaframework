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
package org.nebulaframework.benchmark.linpack;

import java.io.Serializable;

/**
 * LinPack benchmark result wrapper class.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */ 
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
