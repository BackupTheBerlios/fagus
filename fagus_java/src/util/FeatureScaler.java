package util;

/**
 * This class provides scaling for feature vectors. All 
 * features are scaled to some user-defined interval. 
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class FeatureScaler {
	
	public void scale(VectorSet vectors, double lower, double upper) {
		final int n = vectors.getDimension();
		double[][] extrema = getExtrema(vectors.getData().keySet(), n);

		double[] scales = new double[n];
		double[] offsets = new double[n];

		/*
		 * Suppose that our original interval of a feature is [a,b].
		 * Then we can use the CDF of the uniform distribution to get
		 * a mapping to [0,1]
		 * 
		 *   F(x) = (x - a) / (b - a)
		 * 
		 * We can now use the inverse CDF to get a mapping to some
		 * interval [lower,upper]
		 * 
		 *   y = (upper - lower) * (x - a) / (b - a) + lower
		 *     = x * (upper - lower) / (b - a) + (lower * b - upper * a) / (b - a)
		 * 
		 * For every feature two constants are introduced. The scale
		 * is a constant to be multiplied with x.
		 * 
		 *   scale = (upper - lower) / (b - a)
		 * 
		 * The other parameter is an offset that is added to the result.
		 * 
		 *   offset = (lower * b - upper * a) / (b - a)
		 */
		for(int i = 0; i < n; i++) {
			scales[i] = (upper - lower) / (extrema[i][1] - extrema[i][0]);
			offsets[i] = (lower * extrema[i][1] - upper * extrema[i][0])/(extrema[i][1] - extrema[i][0]);
		}
		
		for(double[] v: vectors.getData().keySet()) {
			for(int i = 0; i < n; i++) {
				v[i] = v[i] * scales[i] + offsets[i];
			}
		}
	}
	
	/*
	 * Get the maximum and minimum of each feature.
	 */
	private double[][] getExtrema(Iterable<double[]> data, int dimension) {
		double[][] result = new double[dimension][2];
		
		for(int i = 0; i < dimension; i++) {
			result[i][0] = Double.POSITIVE_INFINITY;
			result[i][1] = Double.NEGATIVE_INFINITY;
		}
		
		for(double[] v: data) {
			for(int i = 0; i < dimension; i++) {
				if(v[i] < result[i][0]) {
					result[i][0] = v[i];
				}
				
				if(v[i] > result[i][1]) {
					result[i][1] = v[i];
				}
			}
		}
		
		return result;
	}
	
	
}
