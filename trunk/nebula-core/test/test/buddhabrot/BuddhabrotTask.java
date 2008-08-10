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

import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.grid.GridExecutionException;

public class BuddhabrotTask implements GridTask<BuddhabrotResult> {

	private static final long serialVersionUID = -8080885595821455485L;

	private static final long PLOT_SIZE = 10000L;
	private static final int RED_DWELL = 20000;
	private static final int GREEN_DWELL = 6000;
	private static final int BLUE_DWELL = 20;

	// exposure counters for each pixel & color
	private transient int[][] exposureBlue = null;
	private transient int[][] exposureRed = null;
	private transient int[][] exposureGreen = null;

	// number of actual exposures

	int width;
	int height;

	public BuddhabrotTask(int width, int height) {
		this.width = width;
		this.height = height;
	}

	void plot(long samples) {

		double x, y;

		// iterate through some plots
		for (int n = 0; n < samples; n++) {
			
			// Choose a random point in same range
			x = random(-2.0, 1.0);
			y = random(-1.5, 1.5);

			if (iterate(x, y, BLUE_DWELL)) {
				iterateAndDraw(x, y, BLUE_DWELL, exposureBlue);
			}
			if (iterate(x, y, GREEN_DWELL)) {
				iterateAndDraw(x, y, GREEN_DWELL, exposureGreen);
			}
			if (iterate(x, y, RED_DWELL)) {
				iterateAndDraw(x, y, RED_DWELL, exposureRed);
			}
		}
	}

	/**
	 * Pick a random value between min and max.
	 */
	final double random(double min, double max) {
		return min + (Math.random() * Math.abs(max - min));
	}

	// Iterate the Mandelbrot and return TRUE if the point exits
	// Also handle the drawing of the exit points

	/**
	 * Test a single coordinate against a given dwell value.
	 * 
	 * @param x0
	 *            random x coordinate
	 * @param y0
	 *            random y coordinate
	 * @param drawIt
	 *            if true, we fill in values
	 * @param dwell
	 *            the dwell (bailout) value
	 * @param expose
	 *            exposure array to fill in results
	 * @return true if we escaped before bailout
	 */
	private boolean iterate(double x0, double y0, int dwell) {
		double x = 0;
		double y = 0;
		double xnew, ynew;

		for (int i = 0; i < dwell; i++) {
			xnew = x * x - y * y + x0;
			ynew = 2 * x * y + y0;

			if ((xnew * xnew + ynew * ynew) > 4) {
				return true; // escapes
			}
			x = xnew;
			y = ynew;
		}
		return false; // does not escape
	}

	private void iterateAndDraw(double x0, double y0, int dwell, int[][] expose) {
		double x = 0;
		double y = 0;
		double xnew, ynew;
		int ix, iy;

		for (int i = 0; i < dwell; i++) {
			xnew = x * x - y * y + x0;
			ynew = 2 * x * y + y0;
			if (i > 3) {
				ix = (int) (height * (xnew + 2.0) / 3.0);
				iy = (int) (height * (ynew + 1.5) / 3.0);
				if (ix >= 0 && iy >= 0 && ix < height && iy < width) {
					expose[iy][ix]++; // rotate and expose point
				}
			}
			if ((xnew * xnew + ynew * ynew) > 4) {
				return; // escape
			}
			x = xnew;
			y = ynew;
		}
	}

	public BuddhabrotResult execute() throws GridExecutionException {
		createArrays();
		plot(PLOT_SIZE);
		return new BuddhabrotResult(exposureRed, exposureGreen, exposureBlue);
	}

	private void createArrays() {
		if (exposureBlue==null)		exposureBlue = new int[width][height];
		if (exposureGreen==null) 	exposureGreen = new int[width][height];
		if (exposureRed==null)		exposureRed = new int[width][height];
	}

}
