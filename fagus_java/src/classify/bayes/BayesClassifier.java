package classify.bayes;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import math.statistics.MultivariateDistribution;

import util.ClassDescriptor;
import util.VectorSet;

import classify.Classifier;

/**
 * This is a basic implementation of a bayesian classifier.
 * For every known class, a distribution function is generated
 * during the training phase. A classification uses these functions
 * and yields the class descriptor with the highest density.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public abstract class BayesClassifier implements Classifier {
	/**
	 * For every class there is a distribution function which is
	 * used for classification. This map should be constructed 
	 * in the training phase of derived classes.
	 */
	protected Map<ClassDescriptor, MultivariateDistribution> distributions;
	/**
	 * The a priori probabilities of each class. They are estimated
	 * using the provided training data.
	 */
	protected Map<ClassDescriptor, Double> priors;
	
	private final boolean usePriors;
	
	/**
	 * Create a new BayesClassifier.
	 */
	protected BayesClassifier() {
		distributions = new HashMap<ClassDescriptor, MultivariateDistribution>();
		priors = new HashMap<ClassDescriptor, Double>();
	
		/*
		 * Load properties file and check, whether or not a priori
		 * probabilities should be considered.
		 */
		boolean b;
		try {
			Properties props = new Properties();
			props.load(new FileInputStream("resources/Bayes.properties"));
			
			b = Boolean.parseBoolean(props.getProperty("useAPrioriProbabilities"));
		} catch(Exception e) {
			b = true;
		}
		
		usePriors = b;
	}
	
	/**
	 * Classify a feature vector. Among all class distributions,
	 * the highest density is evaluated and the appropriate class
	 * descriptor is returned.
	 * @param f the vector to classify.
	 * @return a class descriptor for the class f was assigned to.
	 */
	public ClassDescriptor classify(double[] f) {
		double maxDensity = Double.NEGATIVE_INFINITY;
		ClassDescriptor d = null;
		
		for(ClassDescriptor cl: distributions.keySet()) {
			double density;
			
			if(usePriors) {
				density = distributions.get(cl).getDiscriminant(f, priors.get(cl));
			} else {
				density = distributions.get(cl).getDiscriminant(f);
			}

			/*
			 * Check if we have some numerical trouble. This is frequently
			 * observed when some nonsense vectors or outliers are given.
			 */
			if(Double.isNaN(density)) {
				throw new ArithmeticException("Computation of density for class " 
						+ cl + " contains numerical errors");
			}
			
			if(density > maxDensity) {
				maxDensity = density;
				d = cl;
			}
		}
		
		return d;
	}
	
	
	/**
	 * Perform the training phase, which adjusts the distribution parameters
	 * using the given training set.
	 * 
	 * @param trainingSet The data to use for training data.
	 */
	public void train(VectorSet trainingSet) {
		int dimension = trainingSet.getDimension();
		int n = trainingSet.getData().size();
		
		/*
		 * Init prior values.
		 */
		Map<ClassDescriptor, List<double[]>> data = trainingSet.getInvertedData();
		
		for(ClassDescriptor d: data.keySet()) {
			priors.put(d, (double)data.get(d).size()/n);
		}

		doTrain(trainingSet.getInvertedData(), dimension);
	}
	 
	protected abstract void doTrain(Map<ClassDescriptor, List<double[]>> data, int dimension);

	/**
	 * Clear available training information.
	 */
	public void clearTrainingData() {
		distributions.clear();
	}
		
}
