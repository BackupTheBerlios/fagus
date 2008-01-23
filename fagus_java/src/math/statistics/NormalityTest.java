package math.statistics;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Jama.Matrix;

import util.ClassDescriptor;
import util.LibSVMVectorSetReader;
import util.VectorSet;

/**
 * This is a test to check, whether or not a given set of patterns
 * is distributed according to some multivariate normal distribution.
 * The test has been suggested by K. Fukunaga.
 * 
 * <br><br>
 * See Chapter 3 "Hypothesis Testing"
 *     in K. Fukunaga
 *     "Introduction to Statistical Pattern Recognition"
 *     2nd edition
 *     Academic Press, 1990
 *     
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class NormalityTest {
	
	public static void main(String[] args) {
		VectorSet vectors = null;
		
		if(args.length < 1) {
			System.err.println("Usage: java math.statistics.NormalityTest INPUT_FILE");
			System.exit(1);
		}
		
		try {
			vectors = (new LibSVMVectorSetReader(args[0])).parse();
		} catch(IOException e) {
			System.err.println("Cannot parse input file: " + e.getMessage());
			System.exit(1);
		}
		
		Map<ClassDescriptor, double[]> result = getBounds(vectors);
		
		for(ClassDescriptor c: result.keySet()) {
			double[] d = result.get(c);
			
			System.out.printf("%s: (%.4f, %.4f)\n", c.toString(), d[0], d[1]);
		}
	}
	
	/*
	 * Each vector X is mapped to scalar value using the following mapping function
	 * 
	 * zeta(X) = 1/(N-1) (X - M)^T . Sigma^-1 . (X - M)
	 * 
	 * where M is the mean vector, Sigma is the covariance matrix, and N is the
	 * number of samples.
	 * 
	 * If the patterns are normally distributed, zeta should match a beta distribution
	 * 
	 * Beta((n-2)/2, (N-n-3)/2)
	 * 
	 * where n is the vector dimension.
	 */
	public static Map<ClassDescriptor, double[]> getBounds(VectorSet vectors) {
		int n = vectors.getDimension();
		Map<ClassDescriptor, double[]> result = new HashMap<ClassDescriptor, double[]>();
		
		for(ClassDescriptor c: vectors.getInvertedData().keySet()) {
			List<double[]> l = vectors.getInvertedData().get(c);
			int m = l.size();
			
			double[] v = new double[l.size()];
			double[] mean = MaximumLikelihoodEstimation.getMean(l, n);
			double[][] covariance = MaximumLikelihoodEstimation.getCovariance(l, mean);
			
			double[][] invCov = (new Matrix(covariance)).inverse().getArray();
			
			for(int i = 0; i < l.size(); i++) {
				v[i] = mapVector(l.get(i), mean, invCov) / (m - 1);
			}
			
			result.put(c, KolmogorovSmirnovTest.test(v, new NumericalCDF(new BetaPDF(m,n))));
		}
		
		return result;
	}
	
	
	private static double mapVector(double[] x, double[] mean, double[][] invCov) {
		double result = 0.0;
		int n = x.length;
		
		for(int i = 0; i < n - 1; i++) {
			double u = 0.0;
			
			for(int j = i + 1; j < n; j++) {
				u += (x[j] - mean[j]) * invCov[i][j];
			}
			
			result += u * (x[i] - mean[i]);
		}

		result = result * 2;
		
		for(int i = 0; i < n; i++) {
			result += (x[i] - mean[i]) * (x[i] - mean[i]) * invCov[i][i];
		}
		
		return result;
	}
	
	private static class BetaPDF implements PDF {
		private final double alpha;
		private final double beta;
		private final double coeff;
		
		public BetaPDF(int m, int n) {
			alpha = (n - 2.0)/2.0;
			beta = (m - n - 3.0)/2.0;
			coeff = getCoeff(m, n);
		}
		
		public double getValue(double x) {
			return coeff * Math.pow(x, alpha) * Math.pow(1-x, beta);
		}
		
		private double getCoeff(int m, int n) {
			double result;
			
			/*
			 * coeff = Gamma( (m-1)/2 ) / [ Gamma(n/2) * Gamma( (m-n-1)/2 ) ]
			 */
			if(n % 2 == 0) {
				result = (m - n - 1) / 2.0;
				int d1 = m - 1;
				int d2 = n;
				
				for(int i = 1; i <= n/2 - 1; i++) {
					d1 -= 2;
					d2 -= 2;
					
					result *= (double)d1 / d2;
				}
			} else if(m % 2 == 0) {
				result = n / 2.0;
				int d1 = m - 1;
				int d2 = 0;
				
				for(int i = 1; i < (m - n - 3)/2; i++) {
					d1 -= 2;
					d2 += 2;
					
					result *= (double)d1 / d2;
				}
			} else {
				result = 1 / Math.PI;
				int d1 = 0;
				int d2 = -1;
				
				for(int i = 1; i <= (n-1)/2; i++) {
					d1 += 2;
					d2 += 2;
					
					result *= (double)d1 / d2;
				}
				
				d1 = m - 1;
				d2 = -1;
				
				for(int i = 1; i <= (m-n-2)/2; i++) {
					d1 -= 2;
					d2 += 2;
					
					result *= (double)d1 / d2;
				}				
			}
			return result;
		}
	}
	
}
