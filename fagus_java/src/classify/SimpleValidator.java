package classify;

import java.util.Map;

import reporting.ClassificationLogger;
import select.FeatureSelection;
import util.ClassDescriptor;
import util.VectorSet;

/**
 * This Validator can be used for validating a simple test set.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class SimpleValidator implements Validator {
	private Classifier classifier;
	private FeatureSelection selection;
	private ClassificationLogger logger;
	
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	public void setFeatureSelection(FeatureSelection selection) {
		this.selection = selection;
	}
	
	public void setLogger(ClassificationLogger logger) {
		this.logger = logger;
	}

	public void validate(VectorSet testData) {
		Map<double[], ClassDescriptor> data = testData.getData();
		
		for(double[] v: data.keySet()) {
			ClassDescriptor c;
			
			if(selection != null) {
				c = classifier.classify(selection.mapVector(v));
			} else {
				c = classifier.classify(v);
			}
			
			if(logger != null) {
				logger.log(v, data.get(v), c);
			}
		}
	}

}
