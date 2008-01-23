package classify.parzen;

import Jama.Matrix;

/**
 * This kernel function uses the PDF of a uniform distribution.
 * The distributions mean is a zero vector. The shape and size
 * of the distribution is determined by the covariance matrix
 * and a radius parameter.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class UniformKernel implements Kernel {
	private final double radius;
	private final double density;
	private final double[][] invCovariance;
	
	/**
	 * Create a new Uniform distribution with a given covariance
	 * matrix.
	 * 
	 * @param covariance
	 * @param radius
	 */
	public UniformKernel(double[][] covariance, double radius) {
		Matrix a = new Matrix(covariance);
		this.radius = radius;
		density = getDensity(covariance.length, a.det(), radius);
		this.invCovariance = a.inverse().getArray();
	}
	
	
	public double getValue(double[] v) {
		final int dimension = v.length;
		double d = 0.0;
		
		/*
		 * Check, if v is within a certain range.
		 * If v is outside, the density is 0, otherwise
		 * it is p.
		 */
		for(int i = 0; i < dimension; i++) {
			d += v[i] * v[i] * invCovariance[i][i];
		}
		
		for(int i = 0; i < dimension - 1; i++) {
			for(int j = i + 1; j < dimension; j++) {
				d += v[i] * v[j] * invCovariance[i][j];
			}
		}
		
		if(d <= radius * radius * (dimension + 2)) {
			return density;			
		} else {
			return 0.0;
		}
	}

	
	/*
	 * The density of a uniform distribution is constant (within
	 * a certain range of the mean). This density is calculated by
	 * 
	 * p = Gamma( (n+2)/2 ) / [ pi^{n/2} * Sqrt(|A|) * r^n * (n+2)^{n/2} ]
	 * 
	 * where n is the dimension and |A| is the determinant of the
	 * covariance matrix.
	 */
	private double getDensity(int dimension, double det, double r) {
		double p = Math.PI * (dimension + 2);
		double result;
		
		if(dimension % 2 == 0) {
			/*
			 * Then Gamma(x + 1) function is simply x!
			 */
			result = 1.0;
			
			for(int i = 1; i <= dimension / 2; i++) {
				result *= i / p;
			}
		} else {
			result = 1.0 / Math.sqrt(dimension + 2);
			
			for(int i = 1; i <= (dimension - 1) / 2; i++) {
				result *= (2*i + 1) / p;
			}
		}
		
		result = result / (Math.pow(r, dimension) * Math.sqrt(det));
		
		return result;
	}
}
