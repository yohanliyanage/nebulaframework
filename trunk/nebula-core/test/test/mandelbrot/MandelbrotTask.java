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

package test.mandelbrot;

import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.grid.GridExecutionException;

public class MandelbrotTask implements GridTask<MandelbrotResult> {

	private static final long serialVersionUID = 8435497590730632638L;

	private static final long MAX_ITERATIONS = 10000L;

	// region for which application is computing Mandelbrot
	private double x1; // starting x1,y1
	private double y1; 
	private double x2; // ending x2,y2
	private double y2; 

	// slice of the region this task computes
	private int start; // starting scan line
	private int width; // width of scan line in image
	private int height; // number of scan lines in image
	private int lines; // number of scan lines per task
	
	
	
	public MandelbrotTask(double x1, double y1, double x2, double y2,
			int start, int width, int height, int lines) {
		super();
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.start = start;
		this.width = width;
		this.height = height;
		this.lines = lines;
	}

	public MandelbrotResult execute() throws GridExecutionException {
		
		try {
			
			// Check for pre-conditions
			doAssertions();
			
			// Do Execution
			return processMandelbrot();
			
		} catch (Exception e) {
			throw new GridExecutionException(e);
		}
	}

	private MandelbrotResult processMandelbrot() {
	       double x, y, xx, a, b;
	        int end = start + lines;

	        double da = x2/width;
	        double db = y2/height;

	        b = y1;

	        long[][] points = new long[width][lines];

	        for (int i = 0; i < start; i++) {
	            b = b + db;
	        }

	        int k = 0;

	        for (int i = start; i < end; i++, k++) {
	            a = x1;
	            for (int j = 0; j < width; j++) {
	                long n = 0;
	                x = 0.0;
	                y = 0.0;
	                while ( (n < MAX_ITERATIONS) && ( (x*x)+(y*y) < 4.0) ) {
	                    xx = x * x - y * y + a;
	                    y = 2 * x * y + b;
	                    x = xx;
	                    n++;
	                }
	                points[j][k] = n;
	                a = a + da;
	            }
	            b = b + db;
	        }
	        return new MandelbrotResult(start,lines, points);
	}

	private void doAssertions() throws GridExecutionException {
		try {
			if (start<0) throw new AssertionError("start");
			if (width<=0) throw new AssertionError("width");
			if (height<=0) throw new AssertionError("height");
			if (lines<=0) throw new AssertionError("lines");
		}
		catch(AssertionError ae) {
			throw new GridExecutionException("GridTask Assertions Failed");
		}
	}

}
