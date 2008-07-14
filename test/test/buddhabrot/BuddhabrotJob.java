package test.buddhabrot;

import java.awt.Color;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.annotations.ProcessingSettings;
import org.nebulaframework.core.job.unbounded.UnboundedGridJob;
import org.nebulaframework.core.task.GridTask;

@ProcessingSettings(maxTasksInQueue=5,reductionFactor=1000, stopOnNullTask=false, mutuallyExclusiveTasks=true)
public class BuddhabrotJob implements UnboundedGridJob<BuddhabrotResult,Serializable> {

	private static final long serialVersionUID = 8997815059325788647L;
	private static Log log = LogFactory.getLog(BuddhabrotJob.class);
	
	private int width;
	private int height;
	
	private int[][] exposureRed = new int[width][height];
	private int[][] exposureGreen = new int[width][height];
	private int[][] exposureBlue = new int[width][height];
	
	// max values for normalization
	private int maxexposureBlue; 
	private int maxexposureRed; 
	private int maxexposureGreen; 
	
	public BuddhabrotJob(int width, int height) {
		
		super();
		
		this.width = width;
		this.height = height;
		
		exposureRed = new int[width][height];
		exposureGreen = new int[width][height];
		exposureBlue = new int[width][height];
	}

	public GridTask<BuddhabrotResult> task() {
		
		log.debug("Returning Raw Job");
		
		// Return Raw Job
		return new BuddhabrotTask(width, height);
	}
	
	public Serializable processResult(Serializable r) {
		
		BuddhabrotResult result = (BuddhabrotResult) r;

		// Update Exposures
		updateExposures(exposureRed, result.getExposureRed());
		updateExposures(exposureGreen, result.getExposureGreen());
		updateExposures(exposureBlue, result.getExposureBlue());
		
		findMaxExposure();
		
		return calculateRGB();
		
	}

	private void updateExposures(int[][] exposureRed, int[][] newExposureRed) {
		
		int i=0;
		int j=0;
		for (i=0; i < exposureRed.length; i++) {
			for (j=0; j < exposureRed[0].length; j++) {
				exposureRed[i][j] = exposureRed[i][j] + newExposureRed[i][j];
			}
		}
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
}
