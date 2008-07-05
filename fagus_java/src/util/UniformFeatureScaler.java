package util;

import java.util.Map;

import util.io.Export;
import util.io.ExportVisitor;
import util.io.Import;
import util.io.ModelType;

/**
 * This class is the most basic feature scaling algorithm. It
 * scales all features uniformly to some given interval. Usually,
 * this is [0,1] or [-1, 1].
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class UniformFeatureScaler implements FeatureScaler {
	private final int dimension;
	private final double[] offsets;
	private final double[] scales;
	
	/*
	 * This constructor is supposed to be used by the import
	 * function below.
	 */
	private UniformFeatureScaler(double[] scales, double[] offsets) {
		assert(scales.length == offsets.length);
		
		this.dimension = scales.length;
		this.scales = scales;
		this.offsets = offsets;
	}
	
	/**
	 * Create a new UniformFeatureScaler for some vector set. This
	 * function will calculate the extrema for each dimension and 
	 * derive the necessary data to scale future data.
	 * 
	 * @param vectors The set of vectors to use for scaling.
	 * @param lower The lower bound of the interval.
	 * @param upper The upper bound of the interval.
	 */
	public UniformFeatureScaler(VectorSet vectors, double lower, double upper) {
		this.dimension = vectors.getDimension();
		final double[][] extrema = getExtrema(vectors.getData().keySet());
		
		offsets = getOffsets(extrema, lower, upper);
		scales = getScales(extrema, lower, upper);
	}
	
	/**
	 * Scale a set of vectors. This method works in place and will
	 * modify the vector set.
	 */
	public void scale(VectorSet vectors) {
		for(double[] v: vectors.getData().keySet()) {
			for(int i = 0; i < dimension; i++) {
				v[i] = v[i] * scales[i] + offsets[i];
			}
		}
	}

	/**
	 * Scale a single vector. This method works in place and will
	 * modify the given vector.
	 */
	public void scale(double[] vector) {
		for(int i = 0; i < dimension; i++) {
			vector[i] = vector[i] * scales[i] + offsets[i];
		}
	}

	@Export(ModelType.FEATURE_SCALING)
	public void export(ExportVisitor exporter) {
		ExportVisitor.Parameters params = exporter.newParametersInstance();
		params.setParameter("scales", scales);
		params.setParameter("offsets", offsets);
		exporter.setScaling(this.getClass().getName(), params);
	}
	
	@Import(ModelType.FEATURE_SCALING)
	public static FeatureScaler newInstance(Map<String, Object> params) {
		double[] scales = (double[])params.get("scales");
		double[] offsets = (double[])params.get("offsets");
		
		return new UniformFeatureScaler(scales, offsets);
	}

	/*
	 * Get the maximum and minimum of each feature.
	 */
	private double[][] getExtrema(Iterable<double[]> data) {
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
	private double[] getOffsets(double[][] extrema, double lower, double upper) {
		double[] offsets = new double[dimension];
		
		for(int i = 0; i < dimension; i++) {
			offsets[i] = (lower * extrema[i][1] - upper * extrema[i][0])/(extrema[i][1] - extrema[i][0]);
		}
		
		return offsets;
	}
	
	private double[] getScales(double[][] extrema, double lower, double upper) {
		double[] scales = new double[dimension];

		for(int i = 0; i < dimension; i++) {
			scales[i] = (upper - lower) / (extrema[i][1] - extrema[i][0]);
		}

		return scales;
	}
}
