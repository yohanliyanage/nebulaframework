/*
 * The copyright of these materials are subject to the
 * original authors.
 * 
 * This code is distributed freely.
 */
package org.nebulaframework.benchmark.scimark2;

/**
 * Estimate Pi by approximating the area of a circle.
 * 
 * How: generate N random numbers in the unit square, (0,0) to (1,1) and see how
 * are within a radius of 1 or less, i.e.
 * 
 * <pre>
 * 
 * sqrt(x &circ; 2 + y &circ; 2) &lt; r
 * 
 * </pre>
 * 
 * since the radius is 1.0, we can square both sides and avoid a sqrt()
 * computation:
 * 
 * <pre>
 * 
 * x &circ; 2 + y &circ; 2 &lt;= 1.0
 * 
 * </pre>
 * 
 * this area under the curve is (Pi * r^2)/ 4.0, and the area of the unit of
 * square is 1.0, so Pi can be approximated by
 * 
 * <pre>
 *  # points with x&circ;2+y&circ;2 &lt; 1
 *  Pi =&tilde; 		--------------------------  * 4.0
 *  total # points
 * 
 * </pre>
 * 
 */

public class MonteCarlo {
	final static int SEED = 113;

	public static final double num_flops(long Num_samples) {
		// 3 flops in x^2+y^2 and 1 flop in random routine

		return ((double) Num_samples) * 4.0;

	}

	public static final double integrate(long Num_samples) {

		Random R = new Random(SEED);

		int under_curve = 0;
		for (int count = 0; count < Num_samples; count++) {
			double x = R.nextDouble();
			double y = R.nextDouble();

			if (x * x + y * y <= 1.0)
				under_curve++;

		}

		return ((double) under_curve / Num_samples) * 4.0;
	}

}
