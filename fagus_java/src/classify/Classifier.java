package classify;

import util.ClassDescriptor;
import util.VectorSet;

/**
 * This provides an interface for classification algorithms. Each
 * of those algorithms first takes some training data (with known 
 * class affiliation) which is later on used to classify unknown 
 * data.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface Classifier {
	/**
	 * Do a classification for a feature vector f. This step
	 * requries, that a training set has already been set.
	 * @param f The feature vector to classify.
	 * @return A descriptor for the class that f was assigned to.
	 */
	ClassDescriptor classify(double[] f);
	
	/**
	 * Set the training data.
	 * @param trainingSet The training data to use.
	 */
	void train(VectorSet trainingSet);
	
	/**
	 * Discard training data. I.e. reset the classifier.
	 */
	void clearTrainingData();
	
	/**
	 * Provide some information on whether feature scaling
	 * is recommendet as a preprocessing step.
	 * @return True, if feature scaling is suggested.
	 */
	boolean suggestsScaling();
}
