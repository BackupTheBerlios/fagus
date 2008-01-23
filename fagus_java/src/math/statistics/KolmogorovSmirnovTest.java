package math.statistics;

import java.util.Arrays;

import math.Interpolate;

/**
 * This is an implementation of the Kolmogorov-Smirnov Test, which compares
 * an empirical and a continuous distribution.
 * 
 * <br><br>
 * See Chapter 3.3.1. "General Test Procedures for Studying Random Data"
 *     in D. Knuth
 *     "The Art of Computer Programming" 
 *     Volume 2
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class KolmogorovSmirnovTest {
	
	/*
	 * The probability, where the k-Values can be obtained directly from
	 * the below table.
	 */
	private static final double[] P = {0.01, 0.05, 0.25, 0.5, 0.75, 0.95, 0.99};
	
	
	/*
	 * The table of k-values for different numbers of samples.
	 */
	private static final double[][] KS_TABLE = {
		{0.01000, 0.05000, 0.2500, 0.5000, 0.7500, 0.9500, 0.9900}, // n = 1
		{0.01400, 0.06749, 0.2929, 0.5176, 0.7071, 1.0980, 1.2728}, // n = 2
		{0.01699, 0.07919, 0.3112, 0.5147, 0.7539, 1.1017, 1.3589}, // n = 3
		{0.01943, 0.08789, 0.3202, 0.5110, 0.7642, 1.1304, 1.3777}, // n = 4
		{0.02152, 0.09471, 0.3249, 0.5245, 0.7674, 1.1392, 1.4024}, // n = 5
		{0.02336, 0.1002, 0.3272, 0.5319, 0.7702, 1.1463, 1.4144},  // n = 6
		{0.02501, 0.1048, 0.3280, 0.5364, 0.7755, 1.1537, 1.4226},  // n = 7
		{0.02650, 0.1086, 0.3280, 0.5392, 0.7797, 1.1586, 1.4327},  // n = 8
		{0.02786, 0.1119, 0.3274, 0.5411, 0.7825, 1.1624, 1.4388},  // n = 9
		{0.02912, 0.1147, 0.3297, 0.5426, 0.7845, 1.1648, 1.4440},  // n = 10
		{0.03028, 0.1172, 0.3330, 0.5439, 0.7863, 1.1688, 1.4484},  // n = 11
		{0.03137, 0.1193, 0.3357, 0.5453, 0.7880, 1.1714, 1.4521},  // n = 12
		{0.03240, 0.1213, 0.3380, 0.5469, 0.7898, 1.1737, 1.4554},  // n = 13
		{0.03335, 0.1230, 0.3398, 0.5487, 0.7913, 1.1756, 1.4582},  // n = 14
		{0.03424, 0.1244, 0.3412, 0.5500, 0.7926, 1.1773, 1.4606},  // n = 15
		{0.03510, 0.1258, 0.3426, 0.5513, 0.7939, 1.1790, 1.4629},  // n = 16
		{0.03590, 0.1270, 0.3437, 0.5524, 0.7949, 1.1804, 1.4649},  // n = 17
		{0.03667, 0.1281, 0.3446, 0.5533, 0.7959, 1.1817, 1.4668},  // n = 18
		{0.03739, 0.1291, 0.3455, 0.5541, 0.7968, 1.1829, 1.4684},  // n = 19
		{0.03807, 0.1298, 0.3461, 0.5547, 0.7975, 1.1839, 1.4698},  // n = 20
		{0.04354, 0.1351, 0.3509, 0.5605, 0.8036, 1.1916, 1.4801}   // n = 30
	};

	/*
	 * Values for n > 30 are obtained by
	 * 
	 * yp - 1 / (6 * sqrt(n))
	 */
	private static final double[] YP = {0.07089, 0.1601, 0.3793, 0.5878, 0.8326, 1.2239, 1.5174};
	
	
	/*
	 * Get the value of k in the table.
	 */
	private static double getKSTableValue(int n, int p) {
		double value;
		
		if(n <= 20) {
			// can be obtained directly from the above
			// table
			value = KS_TABLE[n - 1][p];
		} else if (n <= 30) {
			// linearly interpoate n = 20 and n = 30
			double u1 = KS_TABLE[19][p]; // n = 20
			double u2 = KS_TABLE[20][p]; // n = 30
			
			value = (u2 - u1) * (n - 20)/ 10.0 + u1; 
		} else {
			// calculate value
			value = YP[p] - 1 / (6 * Math.sqrt(n));
		}
		
		return value;
	}

	/*
	 * Calculate a value for an arbitrary p. This is done
	 * by creating an interpolation polynomial from the known 
	 * k-values of P.
	 */
	static double getK(int n, double p) {
		double value;
		
		if(n < 30) {
			double[] ks = new double[P.length];

			for(int i = 0; i < ks.length; i++) {
				ks[i] = getKSTableValue(n, i);
			}
		
			value = Interpolate.polynomialInterpolate(P, ks, p);
		} else {
			value = Math.sqrt(0.5 * Math.log(1.0/(1.0-p))) - 1.0/(6.0 * Math.sqrt(n));
		}
		
		return value;
	}
	
	
//	/*
//	 * This is an inverse table lookup to search for p for a given
//	 * k. The interpolation polynomial is substracted by k and then
//	 * the root (there is exactly one root) is searched for using
//	 * Newton's method.
//	 */
//	private static double getP(final int n, final double k) {
//		double value;
//		final double[] ks = new double[P.length];
//		
//		for(int i = 0; i < ks.length; i++) {
//			ks[i] = getKSTableValue(n, i);
//		}
//		
//		if(k < ks[0]) {
//			// 0%
//			value = 0;
//		} else if(k > ks[ks.length - 1]) {
//			// 100%
//			value = 1;
//		} else {
//			Function f = new Function() {
//					public double getValue(double x) {
//						return getK(n, x, ks) - k;
//					}
//			};
//			
//			value = RootSearch.newton(f, 0.5, 0.001);
//		}
//		
//		return value;
//	}

	/*
	 * Inverse table lookup to search for a given k. Since the
	 * function f(p) = k is monotonically increasing, we simply
	 * interpolate its inverse g(k) = p and evaluate it for the
	 * desired k.
	 */
	static double getP(final int n, final double k) {
		double value;
		
		if(n > 30) {
			// this is an approximate value with error O(1/n)
			value = 1 - Math.exp(-(12*k*(3*k*n + Math.sqrt(n)) + 1)/(18.0*n));
		} else {
			final double[] ks = new double[P.length];
		
			for(int i = 0; i < ks.length; i++) {
				ks[i] = getKSTableValue(n, i);
			}
		
			if(k < ks[0]) {
				// 0%
				value = 0;
			} else if(k > ks[ks.length - 1]) {
				// 100%
				value = 1;
			} else {
				value = Interpolate.polynomialInterpolate(ks, P, k);
			}
		}
		
		return value;
	}
	
	/**
	 * Test if the empirical distribution function of a given data
	 * matches the CDF of a target distribution. The test will return
	 * an upper and lower bound.
	 * 
	 * @param data the empirical data.
	 * @param cdf the target CDF.
	 * @return a pair of probabilities.
	 */
	public static double[] test(double[] data, CDF cdf) {
		Arrays.sort(data);
		final int n = data.length;
		
		double kp = Double.NEGATIVE_INFINITY;
		double kn = Double.NEGATIVE_INFINITY;
		
		for(int i = 0; i < n; i++) {
			double p = cdf.getValue(data[i]);
			double up = (double)(i + 1.0) / n - p;
			double un = p - (double)i/n;
			
			if(up > kp) {
				kp = up;
			}
			
			if(un > kn) {
				kn = un;
			}
		}
		
		kn = kn * Math.sqrt(n);
		kp = kp * Math.sqrt(n);
		
		double[] result = {getP(n, kn), getP(n, kp)};
		return result;
	}
}
