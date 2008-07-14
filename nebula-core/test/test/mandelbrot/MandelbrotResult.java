package test.mandelbrot;

import java.io.Serializable;

public class MandelbrotResult implements Serializable {

	private static final long serialVersionUID = -83408493802551375L;

	private int start;
	private int lines;
	
	private long[][] points;
	
	public MandelbrotResult(int start, int lines, long[][] points) {
		super();
		this.start = start;
		this.lines = lines;
		this.points = points;
	}
	
	public int getStart() {
		return start;
	}
	public long[][] getPoints() {
		return points;
	}

	public int getLines() {
		return lines;
	}
	
	
	
}
