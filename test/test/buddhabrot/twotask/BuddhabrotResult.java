package test.buddhabrot.twotask;

import java.io.Serializable;

public class BuddhabrotResult implements Serializable {

	private static final long serialVersionUID = 7775797172775981126L;
	
	public static final int RAW_RESULT = 0;
	public static final int NORMALIZED_RESULT = 1;
	
	private int type;
	
	// For Normalized Results
	private int[][] rgb;
	
	// For Raw Results
	private int[][] exposureRed;
	private int[][] exposureGreen;
	private int[][] exposureBlue;
	
	public BuddhabrotResult(int[][] rgb) {
		super();
		this.type = NORMALIZED_RESULT;
		this.rgb = rgb;
	}

	public BuddhabrotResult(int[][] exposureRed, int[][] exposureGreen,
			int[][] exposureBlue) {
		super();
		this.exposureRed = exposureRed;
		this.exposureGreen = exposureGreen;
		this.exposureBlue = exposureBlue;
		this.type = RAW_RESULT;
	}

	public int getType() {
		return type;
	}

	public int[][] getRGB() {
		return rgb;
	}

	public int[][] getExposureRed() {
		return exposureRed;
	}

	public int[][] getExposureGreen() {
		return exposureGreen;
	}

	public int[][] getExposureBlue() {
		return exposureBlue;
	}
	
}
