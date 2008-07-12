package test.buddhabrot.twotask;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.unbounded.UnboundedGridJob;
import org.nebulaframework.core.task.GridTask;

public class BuddhabrotJob implements UnboundedGridJob<BuddhabrotResult,Serializable> {

	private static final long serialVersionUID = 8997815059325788647L;
	private static Log log = LogFactory.getLog(BuddhabrotJob.class);
	
	private Queue<BuddhabrotNormalizeTask> normalizationTasks = new LinkedList<BuddhabrotNormalizeTask>();
	
	private int width;
	private int height;
	
	private int[][] exposureRed = new int[width][height];
	private int[][] exposureGreen = new int[width][height];
	private int[][] exposureBlue = new int[width][height];
	
	private int rawJobs = 0;
	private int resultJobs = 0;
	
	
	public BuddhabrotJob(int width, int height) {
		
		super();
		
		this.width = width;
		this.height = height;
		
		exposureRed = new int[width][height];
		exposureGreen = new int[width][height];
		exposureBlue = new int[width][height];
	}

	public Serializable processResult(Serializable r) {
		
		BuddhabrotResult result = (BuddhabrotResult) r;

		if (result.getType()==BuddhabrotResult.NORMALIZED_RESULT) {
			log.debug("Normalized Job Result : Returning");
			// Nothing to do
			return result.getRGB();
		}

		
		// Update Exposures
		
		updateExposures(exposureRed, result.getExposureRed());
		updateExposures(exposureGreen, result.getExposureGreen());
		updateExposures(exposureBlue, result.getExposureBlue());
		
		
		synchronized (normalizationTasks) {
			log.debug("Raw Job Result : Enqueue");
			// Create Task for Normalization
			normalizationTasks.add(new BuddhabrotNormalizeTask(exposureRed, exposureGreen, exposureBlue, width, height));
		}
		return null;
		
//		BuddhabrotNormalizeTask task = new BuddhabrotNormalizeTask(exposureRed, exposureGreen, exposureBlue, width, height);
//		try {
//			return task.execute();
//		} catch (GridExecutionException e) {
//			e.printStackTrace();
//			return null;
//		}
//		finally {
//			resultJobs++;
//		}
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

	public GridTask<BuddhabrotResult> task() {
		
		synchronized (normalizationTasks) {
			
			// Return a Normalization Job if exists (High Priority)
			if (normalizationTasks.size()>0) {
				resultJobs++;
				log.debug("Returning Normalization Job");
				return normalizationTasks.remove();
			}
			
			// If Raw Job count exceeds too much, slow down a little

		}
		if (rawJobs - resultJobs > 2) {
			try {
				Thread.sleep((rawJobs - resultJobs) * 1000);
			} catch (InterruptedException e) {
				log.error(e);
			}
		}
		rawJobs++;
		
		log.debug("Returning Raw Job");
		
		// Return Raw Job
		return new BuddhabrotTask(width, height);
	}


}
