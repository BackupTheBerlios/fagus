package apps;

import java.io.IOException;

import reporting.ClassificationLogger;
import reporting.ConfusionMatrixReporter;
import reporting.Reporter;
import reporting.TotalErrorReporter;
import select.FeatureSelection;

import util.FeatureScaler;
import util.LibSVMVectorSetReader;
import util.VectorSet;
import util.VectorSetReader;
import util.io.ModelReader;
import classify.Classifier;
import classify.SimpleValidator;
import classify.Validator;

/**
 * This program can be used to test an already trained classifier
 * (and, optionally, a feature selection). An existing model is
 * tested using a set of test vectors.
 *  
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class Test {

	private static void usage() {
		System.err.println("usage: java apps.Test MODEL TEST_DATA");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		VectorSet testData = null;
		Classifier classifier;

		if(args.length < 2) {
			usage();
			System.exit(1);
		}
		
		ModelReader r = new ModelReader();
		try {
			r.read(args[0]);
		} catch(IOException e) {
			System.err.println("Cannot read model file: " + e.getMessage());
			System.exit(1);
		}
		
		classifier = r.getClassifier();
		
		VectorSetReader vsr = new LibSVMVectorSetReader(args[1]);
		try {
			testData = vsr.parse();
		} catch(IOException e) {
			System.err.println("Cannot read test data: " + e.getMessage());
			System.exit(1);
		}
		
		Reporter[] reporters = new Reporter[2];
		reporters[0] = new ConfusionMatrixReporter(testData.getClassDescriptors());
		reporters[1] = new TotalErrorReporter();

		ClassificationLogger logger = new ClassificationLogger();
		Validator validator = new SimpleValidator();
		validator.setClassifier(classifier);
		validator.setLogger(logger);
		
		if(r.needsSelection()) {
			FeatureSelection selection = r.getSelection();
			selection.initialize(testData);
			testData = selection.getMappedData();
		}
		
		if(r.needsScaling()) {
			FeatureScaler scaling = r.getScaling();
			scaling.scale(testData);
		}
		
		validator.validate(testData);
		
		// create report
		for(Reporter reporter: reporters) {
			reporter.createReport(logger, System.out);
		}
	}

}
