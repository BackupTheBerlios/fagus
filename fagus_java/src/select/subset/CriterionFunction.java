package select.subset;

import util.VectorSet;

/**
 * This is an implementation for a criterion function for use in
 * a feature selection algorithm.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface CriterionFunction {

	/**
	 * Initialize with parameters. This will discared previously stored
	 * parmeters.
	 * 
	 * @param dimension the dimension of input vectors.
	 * @param data a map containing sample data.
	 */
	void initialize(int dimension, VectorSet data);
	
	/**
	 * Get the criterion value for this function and given initialization.
	 * @return the criterion value.
	 */
	double getCriterionValue();
	
	/**
	 * Get the criterion for a subset of features.
	 * @param features the indices of the features in this subset.
	 * @return the criterion value.
	 */
	double getCriterionValue(int[] features);
}
