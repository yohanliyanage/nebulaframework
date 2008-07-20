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

package test.buddhabrot;

import java.io.IOException;
import java.io.Serializable;

import org.nebulaframework.util.compression.CompressionUtils;

public class BuddhabrotResult implements Serializable {

	private static final long serialVersionUID = 1877593399536381134L;
	private int bufferSize = 0;
	
	// Holds Compressed Results
	private byte[] exposureRed = null;
	private byte[] exposureGreen = null;
	private byte[] exposureBlue = null;
	
	// Cache values after first calculation
	private transient int[][] red_cache = null;
	private transient int[][] green_cache = null;
	private transient int[][] blue_cache = null;
	
	public BuddhabrotResult(int[][] exposureRed, int[][] exposureGreen,
			int[][] exposureBlue) {
		super();
		
		try {
			this.exposureRed = CompressionUtils.compress(exposureRed);
			this.exposureGreen = CompressionUtils.compress(exposureGreen);
			this.exposureBlue = CompressionUtils.compress(exposureBlue);
			
			this.bufferSize = calculateBuffer(exposureBlue.length, exposureBlue[0].length);
			
		} catch (IOException e) {
			throw new RuntimeException("Cannot Compress",e);
		}
	}

	/**
	 * Calculates the Buffer Size for Compression.
	 * <p>
	 * The formula for calculation is
	 * <pre>
	 *      buffer = payload + serialization overhead
	 *      
	 *      payload = width * height * 4 | { 4 == size_of_int }
	 *      overhead = 50 (constant)
	 * </pre>
	 * 
	 * @param width
	 * @param height
	 * @return
	 */
	private int calculateBuffer(int width, int height) {
		return width * height * 4 + 50;
	}


	public int[][] getExposureRed() {
		
		// If we have cached results, return it
		if (red_cache!=null) return red_cache.clone();
		
		// Else calculate and return
		try {
			return red_cache = CompressionUtils.decompress(exposureRed, bufferSize);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public  int[][] getExposureGreen() {
		
		// If we have cached results, return it
		if (green_cache!=null) return green_cache.clone();
		
		// Else calculate and return
		try {
			return green_cache = CompressionUtils.decompress(exposureGreen, bufferSize);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public int[][] getExposureBlue() {
		
		// If we have cached results, return it
		if (blue_cache!=null) return blue_cache.clone();
		
		// Else calculate and return
		try {
			return blue_cache = CompressionUtils.decompress(exposureBlue, bufferSize);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
