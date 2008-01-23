package classify;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import reporting.ClassificationLogger;
import select.FeatureSelection;

import util.ClassDescriptor;
import util.VectorSet;


/**
 * This is an cross validator to test a classification algorithm
 * using the Leave-One-Out method for error estimation.
 * For every element in the training set, all other elements
 * are used as training set, and the element is classified. 
 * The results are logged to a Reporter.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class CrossValidator implements Validator {
	private Classifier classifier;
	private FeatureSelection selection;
	private ClassificationLogger logger;
	
	
	/**
	 * Set the Classifier for which the error should be validated.
	 */
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	public void setFeatureSelection(FeatureSelection selection) {
		this.selection = selection;
	}
	
	public void setLogger(ClassificationLogger logger) {
		this.logger = logger;
	}
	
	/**
	 * Run the actual iteration and log data.
	 */
	public void validate(VectorSet trainingSet) {
		Map<double[], ClassDescriptor> data = trainingSet.getData();
		
		/*
		 * Note that we have to copy the keySet to prevent from
		 * ConcurrentModificationException.
		 */
		Set<double[]> vectors = new HashSet<double[]>(data.keySet());

		for(double[] vector: vectors) {
			ClassDescriptor descr = trainingSet.remove(vector);
			ClassDescriptor result;
			
			if(selection != null) {
				selection.initialize(trainingSet);
				VectorSet extractedTrainingSet = selection.getMappedData();
				classifier.train(extractedTrainingSet);
				result = classifier.classify(selection.mapVector(vector));
			} else {
				classifier.train(trainingSet);
				result = classifier.classify(vector);
			}
			
			if(logger != null) {
				logger.log(vector, descr, result);
			}
			
			classifier.clearTrainingData();
			trainingSet.add(vector, descr);
		}
	}
}
