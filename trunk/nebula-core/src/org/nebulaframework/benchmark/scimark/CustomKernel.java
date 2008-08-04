/*
 * The copyright of these materials are subject to the
 * original authors.
 * 
 * This code is distributed freely.
 */
package org.nebulaframework.benchmark.scimark;

import org.nebulaframework.benchmark.scimark.grid.ExecutionResult;
import org.nebulaframework.benchmark.scimark.grid.BenchmarkType;

/**
 * Customized SciMark Kernel for Nebula Framework Benchmark.
 * 
 * @author Yohan Liyanage
 * @author <SciMark Original Authors>
 */
public class CustomKernel {

	public static final double min_time = 1.0;
	public static final int FFT_size = Constants.FFT_SIZE;
	public static final int SOR_size = Constants.SOR_SIZE;
	public static final int SPARSE_SIZE_N = Constants.SPARSE_SIZE_M;
	public static final int SPARSE_SIZE_NZ = Constants.SPARSE_SIZE_nz;
	public static final int LU_SIZE = Constants.LU_SIZE;

	public static ExecutionResult measureFFT(Random R) throws Exception {

		int N = FFT_size;

		// initialize FFT data as complex (N real/img pairs)
		double x[] = RandomVector(2 * N, R);
		long cycles = 1;
		Stopwatch sw = new Stopwatch();

		while (true) {
			sw.start();
			for (int i = 0; i < cycles; i++) {
				FFT.transform(x); // forward transform
				FFT.inverse(x); // backward transform
			}
			sw.stop();
			if (sw.read() >= min_time)
				break;

			cycles *= 2;
		}
		// approx Mflops

		final double EPS = 1.0e-10;

		// Accuracy Test
		if (FFT.test(x) / N > EPS) {
			throw new Exception("Invalid Result");
		}

		ExecutionResult result = new ExecutionResult();
		result.setType(BenchmarkType.FFT);
		result.setCalculationTime(sw.read());
		result.setCycles(cycles);

		return result;
	}

	public static ExecutionResult measureSOR(Random R) {

		int N = SOR_size;

		double G[][] = RandomMatrix(N, N, R);

		Stopwatch sw = new Stopwatch();
		int cycles = 1;

		while (true) {
			sw.start();
			SOR.execute(1.25, G, cycles);
			sw.stop();
			if (sw.read() >= min_time)
				break;

			cycles *= 2;
		}

		ExecutionResult result = new ExecutionResult();
		result.setType(BenchmarkType.SOR);
		result.setCalculationTime(sw.read());
		result.setCycles(cycles);

		return result;
	}

	public static ExecutionResult measureMonteCarlo() {
		Stopwatch sw = new Stopwatch();

		int cycles = 1;
		while (true) {
			sw.start();
			MonteCarlo.integrate(cycles);
			sw.stop();
			if (sw.read() >= min_time)
				break;

			cycles *= 2;
		}

		ExecutionResult result = new ExecutionResult();
		result.setType(BenchmarkType.MONTE_CARLO);
		result.setCalculationTime(sw.read());
		result.setCycles(cycles);

		return result;
	}

	public static ExecutionResult measureSparseMatmult(Random R) {

		int N = SPARSE_SIZE_N;
		int nz = SPARSE_SIZE_NZ;

		// initialize vector multipliers and storage for result
		// y = A*y;

		double x[] = RandomVector(N, R);
		double y[] = new double[N];

		// initialize square sparse matrix
		//
		// for this test, we create a sparse matrix wit M/nz nonzeros
		// per row, with spaced-out evenly between the begining of the
		// row to the main diagonal. Thus, the resulting pattern looks
		// like
		// +-----------------+
		// +* +
		// +*** +
		// +* * * +
		// +** * * +
		// +** * * +
		// +* * * * +
		// +* * * * +
		// +* * * * +
		// +-----------------+
		//
		// (as best reproducible with integer artihmetic)
		// Note that the first nr rows will have elements past
		// the diagonal.

		int nr = nz / N; // average number of nonzeros per row
		int anz = nr * N; // _actual_ number of nonzeros

		double val[] = RandomVector(anz, R);
		int col[] = new int[anz];
		int row[] = new int[N + 1];

		row[0] = 0;
		for (int r = 0; r < N; r++) {
			// initialize elements for row r

			int rowr = row[r];
			row[r + 1] = rowr + nr;
			int step = r / nr;
			if (step < 1)
				step = 1; // take at least unit steps

			for (int i = 0; i < nr; i++)
				col[rowr + i] = i * step;

		}

		Stopwatch sw = new Stopwatch();

		int cycles = 1;
		while (true) {
			sw.start();
			SparseCompRow.matmult(y, val, row, col, x, cycles);
			sw.stop();
			if (sw.read() >= min_time)
				break;

			cycles *= 2;
		}

		ExecutionResult result = new ExecutionResult();
		result.setType(BenchmarkType.SPARSE);
		result.setCalculationTime(sw.read());
		result.setCycles(cycles);

		return result;
	}

	public static ExecutionResult measureLU(Random R) throws Exception {

		int N = LU_SIZE;

		// compute approx Mlfops, or O if LU yields large errors
		double A[][] = RandomMatrix(N, N, R);
		double lu[][] = new double[N][N];
		int pivot[] = new int[N];

		Stopwatch sw = new Stopwatch();

		int cycles = 1;
		while (true) {
			sw.start();
			for (int i = 0; i < cycles; i++) {
				CopyMatrix(lu, A);
				LU.factor(lu, pivot);
			}
			sw.stop();
			if (sw.read() >= min_time)
				break;

			cycles *= 2;
		}

		// verify that LU is correct
		double b[] = RandomVector(N, R);
		double x[] = NewVectorCopy(b);

		LU.solve(lu, pivot, x);

		final double EPS = 1.0e-12;
		if (normabs(b, matvec(A, x)) / N > EPS) {
			throw new Exception("Invalid Result");
		}

		ExecutionResult result = new ExecutionResult();
		result.setType(BenchmarkType.LU);
		result.setCalculationTime(sw.read());
		result.setCycles(cycles);

		return result;
	}

	private static double[] NewVectorCopy(double x[]) {
		int N = x.length;

		double y[] = new double[N];
		for (int i = 0; i < N; i++)
			y[i] = x[i];

		return y;
	}

	private static double normabs(double x[], double y[]) {
		int N = x.length;
		double sum = 0.0;

		for (int i = 0; i < N; i++)
			sum += Math.abs(x[i] - y[i]);

		return sum;
	}

	private static void CopyMatrix(double B[][], double A[][]) {
		int M = A.length;
		int N = A[0].length;

		int remainder = N & 3; // N mod 4;

		for (int i = 0; i < M; i++) {
			double Bi[] = B[i];
			double Ai[] = A[i];
			for (int j = 0; j < remainder; j++)
				Bi[j] = Ai[j];
			for (int j = remainder; j < N; j += 4) {
				Bi[j] = Ai[j];
				Bi[j + 1] = Ai[j + 1];
				Bi[j + 2] = Ai[j + 2];
				Bi[j + 3] = Ai[j + 3];
			}
		}
	}

	private static double[][] RandomMatrix(int M, int N, Random R) {
		double A[][] = new double[M][N];

		for (int i = 0; i < N; i++)
			for (int j = 0; j < N; j++)
				A[i][j] = R.nextDouble();
		return A;
	}

	private static double[] RandomVector(int N, Random R) {
		double A[] = new double[N];

		for (int i = 0; i < N; i++)
			A[i] = R.nextDouble();
		return A;
	}

	private static double[] matvec(double A[][], double x[]) {
		int N = x.length;
		double y[] = new double[N];

		matvec(A, x, y);

		return y;
	}

	private static void matvec(double A[][], double x[], double y[]) {
		int M = A.length;
		int N = A[0].length;

		for (int i = 0; i < M; i++) {
			double sum = 0.0;
			double Ai[] = A[i];
			for (int j = 0; j < N; j++)
				sum += Ai[j] * x[j];

			y[i] = sum;
		}
	}

}
