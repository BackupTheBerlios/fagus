package select.subset;

import util.VectorSet;

/**
 * Common algorithm for all selection algorithms.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface SelectionAlgorithm {
	/**
	 * Run the algorithm for a given dataset and try to get rid of
	 * a given number of features.
	 * 
	 * @param vectors the dataset to use.
	 * @param f a criterion function.
	 * @param dropNrFeatures the number of features to get rid of.
	 */
	public abstract void run(VectorSet vectors, CriterionFunction f, int dropNrFeatures);

	/**
	 * Get the best feature vector.
	 * @return
	 */
	public abstract int[] getFeatureVector();

}
