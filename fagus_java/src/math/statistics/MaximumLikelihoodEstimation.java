package math.statistics;

import java.util.ArrayList;
import java.util.List;

/**
 * Estimate parameters (mean and covariance-matrix) for a Normal distribution.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class MaximumLikelihoodEstimation {
	
	/**
	 * Get the mean of a sample set.
	 * <pre>
	 * mu = 1/n * ( x<sub>1</sub> + ... + x<sub>n</sub>)
	 * </pre>
	 * 
	 * @param vectors The samples to use for parameter estimation.
	 * @param dimension A single vector's dimension.
	 * @return A vector containing the mean of the given samples.
	 */
	public static double[] getMean(Iterable<double[]> vectors, int dimension) {
		double[] mean = new double[dimension];
		int n = 0;
		
		for(int i = 0; i < mean.length; i++) {
			mean[i] = 0.0;
		}
		
		for(double[] vector: vectors) {
			addVector(vector, mean);
			n++;
		}
		
		for(int i = 0; i < mean.length; i++) {
			mean[i] = mean[i] / n;
		}
		
		return mean;
	}
	
	
	/**
	 * Get the covariance matrix of a sample set.
	 * <pre>
	 * Sigma = 1 / (n - 1) * Sum { (x<sub>i</sub> - mu) . (x<sub>i</sub> - mu)<sup>T</sup>
	 * </pre>
	 * 
	 * @param vectors
	 * @param mean
	 * @return
	 */
	public static double[][] getCovariance(Iterable<double[]> vectors, double[] mean) {
		int dimension = mean.length;
		double[][] covariance = new double[dimension][dimension];
		List<double[]> diff = new ArrayList<double[]>();
		int n = 0;

		for(double[] v: vectors) {
			diff.add(vectorDiff(v, mean));
			n++;
		}

		for(int i = 0; i < dimension; i++) {
			double u = 0.0;
			
			for(double[] d: diff) {
				u += d[i] * d[i];
			}
			covariance[i][i] = u / (n - 1);
			
			for(int j = i + 1; j < dimension; j++) {
				u = 0.0;
				
				for(double[] d: diff) {
					u += d[i] * d[j];
				}
				covariance[i][j] = covariance[j][i] = u / (n - 1);
			}
		}
		return covariance;
	}
	

	private static void addVector(double[] v, double[] target) {
		for(int i = 0; i < target.length; i++) {
			target[i] += v[i];
		}
	}
	
	private static double[] vectorDiff(double[] v1, double[] v2) {
		double[] result = new double[v1.length];
		
		for(int i = 0; i < result.length; i++) {
			result[i] = v1[i] - v2[i];
		}
		
		return result;
	}
	
}
