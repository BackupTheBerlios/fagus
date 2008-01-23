package classify.knn;

/**
 * This is the very basic - though not absolute correct - 
 * implementation of a distance - namely, the euclidian distance. 
 * The distance of two vectors a and b
 * ||a - b|| 
 * is given by 
 * sqrt( (a_1 - b_1)^2 + (a_2 - b_2)^2 ...) 
 * or
 * sqrt( (a - b) . (a - b) )
 * where '.' denotes the inner product.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class EuclidianDistance implements Metrics {

	private double square(Double x) {
		return x.doubleValue() * x.doubleValue();
	}
	
	/**
	 * Get the distance of two vectors.
	 * @param f1 The first vector.
	 * @param f2 The second vector.
	 * @return The absolute distance.
	 */
	public double getDistance(double[] f1, double[] f2) {
		double squareSum = 0.0;
		double restSum = 0.0;
		
		if(f1.length != f2.length) {
			//return dummy value - valid distance is never negative
			return -1;
		}
		
		/*
		 * ||a-b||^2 is computed by a.a + b.b - 2 a.b
		 * for numerical reasons ('.' denotes the inner product)
		 */
		for(int i = 0; i < f1.length; i++) {
			squareSum += square(f1[i]) + square(f2[i]);
			restSum += f1[i] * f2[i];
		}
		
		return Math.sqrt(squareSum - 2 * restSum);
	}

}
