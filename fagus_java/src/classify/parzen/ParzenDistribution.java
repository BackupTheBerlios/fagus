package classify.parzen;

import java.util.List;

import math.statistics.MultivariateDistribution;

/**
 * The distribution function of a Parzen window classifier
 * is a smoothed function of the empirical distribution function
 * obtained from test data. The smoothing is done using a kernel
 * function.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class ParzenDistribution implements MultivariateDistribution {
	private final Kernel kernel;
	private final List<double[]> vectors;
	
	public ParzenDistribution(Kernel kernel, List<double[]> vectors) {
		this.kernel = kernel;
		this.vectors = vectors;
	}
	
	public double getDiscriminant(double[] v) {
		double result = 0.0;
		
		for(double[] vector: vectors) {
			result += kernel.getValue(vectorSub(v, vector));
		}
		
		return result / vectors.size();
	}
	
	public double getDiscriminant(double[] v, double prior) {
		/*
		 * Ignore the prior, since the non-parametric properties
		 * of the Parzen distribution will implicitly add it.
		 */
		return getDiscriminant(v);
	}
	
	public List<double[]> getVectors() {
		return vectors;
	}

	private double[] vectorSub(double[] v1, double[] v2) {
		double[] v = new double[v1.length];
		
		for(int i = 0; i < v.length; i++) {
			v[i] = v1[i] - v2[i];
		}
		
		return v;
	}
}
