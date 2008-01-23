package apps;

import classify.Classifier;
import classify.CrossValidator;
import classify.Validator;
import classify.bayes.NormalLinearClassifier;
import classify.bayes.NormalMLEClassifier;
import classify.bayes.NormalRegularizedClassifier;
import classify.knn.KNNClassifier;
import classify.parzen.ParzenWindowClassifier;
import classify.svm.SupportVectorClassifier;
import reporting.ClassificationLogger;
import reporting.ConfusionMatrixReporter;
import reporting.ProgressClassificationLogger;
import reporting.Reporter;
import reporting.TotalErrorReporter;
import select.FeatureSelection;
import select.extract.ChernoffLinearDiscriminantAnalysis;
import select.extract.FisherLinearDiscriminantAnalysis;
import util.LibSVMVectorSetReader;
import util.VectorSet;

/**
 * Test a classification algorithm for a given data set.
 * The set is fetched from CSV files in a base directory,
 * which is expected as a command line argument. The 
 * report is again in CSV and printed to stdout.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class CrossValidation {

	private static void usage() {
		System.err.println("Usage: java apps.CrossValidation [-lda n] CLASSIFIER [OPTIONS] INPUT_FILE");
		System.err.println("Where LDA is used to reduce the dimension of the input data to n features\n");
		System.err.println("Classifiers and options: ");
		System.err.println("    knn [k]         : k-NN classifier with k neighbors");
		System.err.println("    parzen [r]      : Parzen Window classifier with radius r");
		System.err.println("    bayes [-linear] : Bayes classifier, either linear or quadratic");
		System.err.println("    svm [c gamma]   : Support-Vector-Machine with parameters c and gamma");
	}


	public static void main(String[] args) throws Exception {
		VectorSet trainingSet = null;
		Classifier classifier = null;
		int argp = 0;
		boolean useLda = false;
		int nLda = 0;
		
		if(args.length < 2) {
			usage();
			System.exit(1);
		}
		
		if(args[argp].equals("-lda")) {
			useLda = true;
			nLda = Integer.parseInt(args[argp + 1]);
			argp += 2;
		}
		
		if(args[argp].equalsIgnoreCase("knn")) {
			if(args.length == argp + 2) {
				classifier = new KNNClassifier();
			} else {
				classifier = new KNNClassifier(Integer.parseInt(args[argp + 1]));
				argp++;
			}
		} else if(args[argp].equalsIgnoreCase("bayes")) {
			if(args.length == argp + 2) {
				classifier = new NormalMLEClassifier();
			} else if(args[argp + 1].equals("-linear")) {
				classifier = new NormalLinearClassifier();
				argp++;
			} else if(args[argp + 1].equals("-regularize")) {
				double alpha = Double.parseDouble(args[argp + 2]);
				classifier = new NormalRegularizedClassifier(alpha);
				argp += 2;
			} else {
				usage();
				System.exit(1);				
			}
		} else if(args[argp].equalsIgnoreCase("parzen")) {
			double radius;
			if(args.length == argp + 2) {
				radius = 1.0;
			} else {
				radius = Double.parseDouble(args[argp + 1]);
				argp++;
			}
			classifier = new ParzenWindowClassifier(radius, ParzenWindowClassifier.KernelType.GAUSSIAN_PRODUCT);
		} else if(args[argp].equalsIgnoreCase("svm")) {
			if(args.length == argp + 2) {
				classifier = new SupportVectorClassifier();
			} else {
				double c = Double.parseDouble(args[argp + 1]);
				double gamma = Double.parseDouble(args[argp + 2]);
				classifier = new SupportVectorClassifier(c, gamma);
				
				argp += 2;
			}
		} else {
			usage();
			System.exit(1);
		}
		
		argp++;
		trainingSet = (new LibSVMVectorSetReader(args[argp])).parse();
		
		// create some reporters
		Reporter[] reporters = new Reporter[2];
		reporters[0] = new ConfusionMatrixReporter(trainingSet.getClassDescriptors());
		reporters[1] = new TotalErrorReporter();
		
		ClassificationLogger logger = new ProgressClassificationLogger(trainingSet.getData().size());//new ClassificationLogger();
		Validator validator = new CrossValidator();
		
		if(useLda) {
			/*
			 * Create a new cross validator which performes a Linear Discriminant Analysis
			 * (LDA) before doing the actual classification. LDA is used to reduce
			 * the dimension of the input data. Note, that not the standard (Fisher) LDA 
			 * procedure is used, but an extension which utilizes the Chernoff criterion. 
			 */
			if(nLda < 0) {
				nLda = trainingSet.getDimension() + nLda;
			}
			
			FeatureSelection selection;
			if(trainingSet.isIllPosed()) {
				System.err.println("WARNING: problem is ill posed");
				//selection = new GeneralizedDiscriminantAnalysis();
				selection = new FisherLinearDiscriminantAnalysis();
			} else {
				selection = new ChernoffLinearDiscriminantAnalysis(nLda);
			}
			
			validator.setFeatureSelection(selection);	
		}
		
		validator.setClassifier(classifier);
		validator.setLogger(logger);
		
		// run evaluation
		validator.validate(trainingSet);
		
		System.out.println();
		
		// create report
		for(Reporter reporter: reporters) {
			reporter.createReport(logger, System.out);
		}
	}
		
}
