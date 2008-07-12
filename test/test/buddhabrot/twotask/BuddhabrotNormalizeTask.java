package test.buddhabrot.twotask;

import java.awt.Color;

import org.nebulaframework.core.GridExecutionException;
import org.nebulaframework.core.task.GridTask;

public class BuddhabrotNormalizeTask implements GridTask<BuddhabrotResult>{

	private static final long serialVersionUID = 5790674231451197823L;

	// max values for normalization
	private int maxexposureBlue; 
	private int maxexposureRed; 
	private int maxexposureGreen; 
	
	private int[][] exposureBlue; // ?
	private int[][] exposureRed; // ?
	private int[][] exposureGreen; // ?
	
	int width;
	int height;
	
	
	public BuddhabrotNormalizeTask(int[][] exposureRed, int[][] exposureGreen,
			int[][] exposureBlue, int width, int height) {
		super();
		this.exposureBlue = exposureBlue;
		this.exposureRed = exposureRed;
		this.exposureGreen = exposureGreen;
		
		this.width = width;
		this.height = height;
	}

	/**
	 * Find the largest exposure values for normalization
	 */
	private void findMaxExposure() {
		maxexposureBlue = maxexposureRed = maxexposureGreen = 0;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
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
	public synchronized int[][] calculateRGB() {
		findMaxExposure();

		int[][] rgb = new int[width][height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
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
		
		return rgb;
	}

	public BuddhabrotResult execute() throws GridExecutionException {
		findMaxExposure();
		return new BuddhabrotResult(calculateRGB());
	}
}
