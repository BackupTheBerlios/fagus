package classify.knn;

/**
 * This is an implementation of a very simple metric. The 
 * difference of two vectors is given by the sum of the 
 * absolute differences of their components.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class ManhattenDistance implements Metrics {

	/**
	 * Get the distance of two vectors.
	 * @param f1 The first vector.
	 * @param f2 The second vector.
	 * @return The absolute distance.
	 */
	public double getDistance(double[] f1, double[] f2) {
		double distance = 0.0;
		
		if(f1.length != f2.length) {
			//return dummy value - valid distance is never negative
			return -1;
		}

		for(int i = 0; i < f1.length; i++) {
			distance += Math.abs(f1[i] - f2[i]);
		}
		
		return distance;
	}

}
