package math.statistics;

/**
 * Basic probability distribution.
 *  
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface MultivariateDistribution {
	
	/**
	 * Return the discriminant of a vector. It need not return
	 * a density in [0;1], but is guaranteed to be a monotonic
	 * function of the density.
	 * 
	 * @param vector the vector.
	 * @return the vector's density.
	 */
	double getDiscriminant(double[] vector);
	
	/**
	 * Return the discriminant of a vector, assuming a given
	 * a-priori probability.
	 * 
	 * @param vector the vector.
	 * @param prior the a-priori probability of this class.
	 * @return the vector's density.
	 */
	double getDiscriminant(double[] vector, double prior);
}
