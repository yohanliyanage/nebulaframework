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

package test.buddhabrot.sequential;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;

public class Buddhabrot {

	public static final long PLOT_SIZE = 10000L;
	private static final int RED_DWELL = 50000;
	private static final int GREEN_DWELL = 5000;
	private static final int BLUE_DWELL = 500;

	// exposure counters for each pixel & color
	private int[][] exposureBlue;	// ?
	private int[][] exposureRed;	// ?
	private int[][] exposureGreen;	// ? 

	// max values for normalization
	private int maxexposureBlue;	// OK
	private int maxexposureRed;		// OK
	private int maxexposureGreen;	// OK

	// number of actual exposures
	private int exposures = 0;		// OK

	
	int fullWidth;
	int fullHeight;
	int startX;
	int startY;
	int processWidth;
	int processHeight;
	
	private ResultListener resultListener;
	
	private AtomicInteger count = new AtomicInteger();
	
	public Buddhabrot(int fullWidth, int fullHeight, ResultListener listener, int startX, int startY, int processWidth, int processHeight) {
		exposureBlue = new int[fullWidth][fullHeight];
		exposureRed = new int[fullWidth][fullHeight];
		exposureGreen = new int[fullWidth][fullHeight];

		this.fullWidth = fullWidth;
		this.fullHeight = fullHeight;
		this.startX = startX;
		this.startY = startY;
		this.processWidth = processWidth;
		this.processHeight = processHeight;
		this.resultListener = listener;	
		
	}


	/**
	 * Generates another round of sample exposures to add to the image
	 * 
	 * @param samples
	 *            number of samples to take
	 */
	void plot(long samples) {
		
		double x, y;
		// iterate through some plots
		for (int n = 0; n < samples; n++) {
			// Choose a random point in same range

			
			x = random(-2.0, 1.0);
			y = random(-1.5, 1.5);
			
			if (iterate(x, y,  BLUE_DWELL)) {
				iterateAndDraw(x, y, BLUE_DWELL, exposureBlue);
				exposures++;
			}
			if (iterate(x, y, GREEN_DWELL)) {
				iterateAndDraw(x, y, GREEN_DWELL, exposureGreen);
				exposures++;
			}
			if (iterate(x, y, RED_DWELL)) {
				iterateAndDraw(x, y, RED_DWELL, exposureRed);
				exposures++;
			}
		}
		
		refresh();
		System.gc();
	}


	/**
	 * Pick a random value between min and max.
	 */
	final double random(double min, double max) {
		return min + (Math.random() * Math.abs(max - min));
	}

	
	
//  Iterate the Mandelbrot and return TRUE if the point exits
//  Also handle the drawing of the exit points
	
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
				ix = (int) (fullHeight * (xnew + 2.0) / 3.0);
				iy = (int) (fullHeight * (ynew + 1.5) / 3.0);
				if (ix >= 0 && iy >= 0 && ix < fullHeight && iy < fullWidth) {
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
	
	/**
	 * Find the largest exposure values for normalization
	 */
	private void findMaxExposure() {
		maxexposureBlue = maxexposureRed = maxexposureGreen = 0;
		
		for (int x = 0; x <fullWidth; x++) {
			for (int y = 0; y < fullHeight; y++) {
				maxexposureBlue = Math.max(maxexposureBlue, exposureBlue[x][y]);
				maxexposureRed = Math.max(maxexposureRed, exposureRed[x][y]);
				maxexposureGreen = Math.max(maxexposureGreen,
											exposureGreen[x][y]);
			}
		}
	}

	/**
	 * Update screen bitmap with latest results
	 */
	public synchronized void refresh() {
		findMaxExposure();
		
		int[][] rgb = new int[fullWidth][fullHeight];
		
		
		for (int x = 0; x < fullWidth; x++) {
			for (int y = 0; y < fullHeight; y++) {
				double blue = exposureBlue[x][y] / (maxexposureBlue / 2.5);
				if (blue > 1) {
					blue = 1;
				}
				double red = exposureRed[x][y] / (maxexposureRed / 2.5);
				if (red > 1) {
					red = 1;
				}
				double green = exposureGreen[x][y] / (maxexposureGreen / 2.5);
				if (green > 1) {
					green = 1;
				}
				Color c = new Color((int) (red * 255), (int) (green * 255),
						(int) (blue * 255));
				rgb[x][y] = c.getRGB();
			}
		}
		System.out.println("[Buddhabrot] " + count.incrementAndGet());
//		
//		int x=0,y=0;
//		for(int[] arr : rgb) {
//			y=0;
//			for (int v : arr) {
//				if (v!=0) {
//					Color c = new Color(v);
//					if (!c.equals(Color.BLACK)) {
//						System.out.println("(" + x + "," + y + ") : " + new Color(v).toString());
//					}
//				}
//				y++;
//			}
//			x++;
//		}
		
		sendResult(rgb);
	}

	private void sendResult(final int[][] rgb) {
		new Thread(new Runnable() {

			public void run() {
				resultListener.onResult(rgb);
			}
			
		}).start();
	}



}
