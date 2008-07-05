package util;

/**
 * This class provides an interface for feature scaling
 * methods.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface FeatureScaler {
	/**
	 * Scale some vectors.
	 * 
	 * @param vectors The vectors to scale. This vector set
	 *        is modified by the scale method.
	 */
	void scale(VectorSet vectors);

	/**
	 * Scale a single vector.
	 * 
	 * @param vector The vector to scale. This vector is 
	 *        modified by the scale method.
	 */
	void scale(double[] vector);
}
