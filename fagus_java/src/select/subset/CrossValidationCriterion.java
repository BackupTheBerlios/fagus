package select.subset;

import java.util.HashMap;
import java.util.Map;

import reporting.ClassificationLogger;
import reporting.TotalErrorReporter;
import classify.Classifier;
import classify.CrossValidator;
import util.ClassDescriptor;
import util.VectorSet;

/**
 * This criterion function will run a cross validation procedure
 * using some classifier architecture to estimate the criterion value
 * of a subset of features.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class CrossValidationCriterion implements CriterionFunction {
	private VectorSet original;
	private final Classifier classifier;
	
	public CrossValidationCriterion(Classifier classifier) {
		this.classifier = classifier;
	}
	
	public double getCriterionValue() {
		/*
		 * Do a cross validation on the original data.
		 */
		return validate(original);
	}

	public double getCriterionValue(int[] features) {
		/*
		 * Extract the desired components of each vector and
		 * create a new VectorSet. This resulting set is used
		 * for cross-validation.
		 */
		Map<double[], ClassDescriptor> origData = original.getData();
		String[] origLabels = original.getFeatureLables();
		Map<double[], ClassDescriptor> newData = new HashMap<double[], ClassDescriptor>();
		String[] newLabels = new String[features.length];
		
		for(double[] v: origData.keySet()) {
			double[] v1 = new double[features.length];
			
			for(int i = 0; i < features.length; i++) {
				v1[i] = v[features[i]];
			}
			
			newData.put(v1, origData.get(v));
		}
		
		for(int i = 0; i < features.length; i++) {
			newLabels[i] = origLabels[features[i]];
		}
		
		return validate(new VectorSet(newData, newLabels));
	}

	public void initialize(int dimension, VectorSet data) {
		this.original = data;
	}
	
	
	private double validate(VectorSet vectors) {
		CrossValidator cv = new CrossValidator();
		ClassificationLogger log = new ClassificationLogger();

		cv.setClassifier(classifier);
		cv.setLogger(log);
		cv.validate(vectors);
		
		return TotalErrorReporter.getSuccessRate(log);		
	}

}
