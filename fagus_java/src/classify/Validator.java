package classify;

import reporting.ClassificationLogger;
import select.FeatureSelection;
import util.VectorSet;

/**
 * This is a common interface for classification error validation.
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface Validator {
	
	/**
	 * Set the classifier which should be validated.
	 * 
	 * @param classifier
	 */
	void setClassifier(Classifier classifier);
	
	/**
	 * Set a feature selection algorithm.
	 * 
	 * @param selection
	 */
	void setFeatureSelection(FeatureSelection selection);
	
	/**
	 * Set the logger.
	 * 
	 * @param logger
	 */
	void setLogger(ClassificationLogger logger);
	
	/**
	 * Validate a set of vectors.
	 * 
	 * @param data
	 */
	void validate(VectorSet data);
}
