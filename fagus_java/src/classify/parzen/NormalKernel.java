package classify.parzen;

import math.statistics.MultivariateNormalDistribution;

/**
 * This is a kernel function which uses the PDF of a multivariate 
 * normal distribution. Basically this kernel is the density of a
 * mulitvariate normal distribution with the zero vector as its mean
 * and some covariance matrix. The size of the covariance matrix
 * is determined by a radius.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class NormalKernel implements Kernel {
	private final MultivariateNormalDistribution dist;
	
	/**
	 * Create a new normal kernel that uses the identity matrix
	 * as its covariance matrix.
	 * 
	 * @param dimension the dimension of the covariance matrix.
	 * @param radius
	 */
	public NormalKernel(int dimension, double radius) {
		double[] mean = new double[dimension];
		double[][] cov = new double[dimension][dimension];
		final double r2 = radius * radius;
		
		for(int i = 0; i < dimension; i++) {
			cov[i][i] = r2;
		}
		
		dist = new MultivariateNormalDistribution(mean, cov);
	}
	
	
	/**
	 * Create a normal kernel with a given covariance matrix.
	 * 
	 * @param covariance
	 * @param radius
	 */
	public NormalKernel(double[][] covariance, double radius) {
		final int dimension = covariance.length;
		
		double[] mean = new double[dimension];
		
		double r2 = radius * radius;
		
		for(int i = 0; i < dimension; i++) {
			covariance[i][i] *= r2;
			
			for(int j = i + 1; j < dimension; j++) {
				covariance[i][j] = covariance[j][i] = covariance[i][j] * r2;
			}
		}
		
		dist = new MultivariateNormalDistribution(mean, covariance);
	}
	
	
	public double getValue(double[] v) {
		return dist.getDiscriminant(v);
	}

}
