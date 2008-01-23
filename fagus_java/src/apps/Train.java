package apps;

import java.io.IOException;

import select.FeatureSelection;
import select.extract.ChernoffLinearDiscriminantAnalysis;
import util.LibSVMVectorSetReader;
import util.VectorSet;
import util.VectorSetReader;
import util.io.ModelWriter;
import classify.Classifier;
import classify.bayes.NormalLinearClassifier;
import classify.bayes.NormalMLEClassifier;
import classify.knn.KNNClassifier;
import classify.parzen.ParzenWindowClassifier;
import classify.svm.SupportVectorClassifier;

/**
 * Perform the training of a classifier (and optionally a feature
 * selector). This program will create an XML file of a classification
 * model, which can later on be used to classify unknown samples or
 * to perform a test procedure.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class Train {

	private static void usage() {
		System.err.println("usage: java apps.Train [-lda n] CLASSIFIER [OPTIONS] TRAINING_DATA MODEL_FILE");
		System.err.println("Where LDA is used to reduce the dimension of the input data to n features\n");
		System.err.println("Classifiers and options: ");
		System.err.println("    knn [k]         : k-NN classifier with k neighbors");
		System.err.println("    parzen [r]      : Parzen Window classifier with radius r");
		System.err.println("    bayes [-linear] : Bayes classifier, either linear or quadratic");
		System.err.println("    svm [c gamma]   : Support-Vector-Machine with parameters c and gamma");
	}
	
	private static void export(Classifier cl, FeatureSelection selection, String outputFile) throws IOException {
		ModelWriter writer = new ModelWriter();
		writer.setClassifier(cl);
		writer.setSelection(selection);
		writer.write(outputFile);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Classifier classifier = null;
		FeatureSelection selection = null;
		int nLda = 0;
		boolean useLda = false;
		int argp = 0;
		
		if(args[argp].equals("-lda")) {
			nLda = Integer.parseInt(args[argp + 1]);
			useLda = true;
			argp += 2;
		}
		
		if(args.length - argp < 3) {
			usage();
			System.exit(1);
		}
		
		if(args[argp].equalsIgnoreCase("knn")) {
			if(args.length == argp + 3) {
				classifier = new KNNClassifier();
			} else {
				classifier = new KNNClassifier(Integer.parseInt(args[argp + 1]));
				argp++;
			}
		} else if(args[argp].equalsIgnoreCase("bayes")) {
			if(args.length == argp + 3) {
				classifier = new NormalMLEClassifier();
			} else if(args[argp + 1].equals("-linear")) {
				classifier = new NormalLinearClassifier();
				argp++;
			} else {
				usage();
				System.exit(1);				
			}
		} else if(args[argp].equalsIgnoreCase("parzen")) {
			double radius;
			if(args.length == argp + 3) {
				radius = 1.0;
			} else {
				radius = Double.parseDouble(args[argp + 1]);
				argp++;
			}
			classifier = new ParzenWindowClassifier(radius, ParzenWindowClassifier.KernelType.NORMAL);
		} else if(args[argp].equalsIgnoreCase("svm")) {
			if(args.length == argp + 3) {
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
		
		VectorSetReader r = new LibSVMVectorSetReader(args[argp]);
		argp++;
		VectorSet trainingData = null;
		
		try {
			trainingData = r.parse();
		} catch(IOException e) {
			System.err.println("Cannot read training data: " + e.getMessage());
			System.exit(1);
		}

		if(useLda) {
			if(nLda < 0) {
				nLda = trainingData.getDimension() + nLda;
			}
			
			selection = new ChernoffLinearDiscriminantAnalysis(nLda);
			selection.initialize(trainingData);
			trainingData = selection.getMappedData();
		}

		classifier.train(trainingData);
		
		
		try {
			export(classifier, selection, args[argp]);
		} catch(IOException e) {
			System.err.println("Cannot write model file: " + e.getMessage());
			System.exit(1);
		}
	}

}
