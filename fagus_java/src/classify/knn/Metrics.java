package classify.knn;

/**
 * This is a basic interface for metrics. The only usefull
 * operation is to calculate the distance of two vectors.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface Metrics {
	/**
	 * Get the distance of two vectors.
	 * @param f1 The first vector.
	 * @param f2 The second vector.
	 * @return The absolute distance.
	 */
	double getDistance(double[] f1, double[] f2);
}
