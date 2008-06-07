package util;

/**
 * This class provides an interface for feature scaling
 * methods.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface FeatureScaler {
	/**
	 * Scale some vectors to the interval [lower, upper]
	 * 
	 * @param vectors The vectors to scale. This vector set
	 *        is modified by the scale method.
	 * @param lower The lower scaling bound.
	 * @param upper The upper scaling bound.
	 */
	void scale(VectorSet vectors, double lower, double upper);

	/**
	 * Scale a single vector to the interval [lower, upper]
	 * 
	 * @param vector The vector to scale. This vector is 
	 *        modified by the scale method.
	 * @param lower The lower scaling bound.
	 * @param upper The upper scaling bound.
	 */
	void scale(double[] vector, double lower, double upper);
}
