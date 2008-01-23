package classify.bayes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import math.statistics.MultivariateDistribution;
import math.statistics.MaximumLikelihoodEstimation;
import math.statistics.MultivariateNormalDistribution;

import util.ClassDescriptor;
import util.io.ExportVisitor;

/**
 * This is a simple bayesian classifier for gaussian data, that is
 * restricted to linear class boundaries.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class NormalLinearClassifier extends BayesClassifier {

	private int dimension;
	
	/**
	 * The training phase adjusts the distribution's parameter. They
	 * are calculated using Maximum likelihood estimation.
	 * 
	 * @param data Provides training vectors for every known class.
	 * @param dimension A training vector's dimension.
	 */
	@Override
	public void doTrain(Map<ClassDescriptor, List<double[]>> data, int dimension) {
		this.dimension = dimension;
		Map<ClassDescriptor, double[]> means = new HashMap<ClassDescriptor, double[]>();

		/*
		 * Get the average of all covariance matrices.
		 * 
		 * Sigma = sum_i=1^C p_i Sigma_i
		 * 
		 * where p_i is the a-priori probability of class i.
		 */
		double[][] covariance = new double[dimension][dimension];

		for(ClassDescriptor d: data.keySet()) {
			double p = priors.get(d);
			double[] mean = MaximumLikelihoodEstimation.getMean(data.get(d), dimension);
			double[][] cov = MaximumLikelihoodEstimation.getCovariance(data.get(d), mean);
			
			for(int i = 0; i < dimension; i++) {
				covariance[i][i] += p * cov[i][i];
				for(int j = i + 1; j < dimension; j++) {
					double u = p * cov[i][j];
					covariance[i][j] += u;
					covariance[j][i] += u;
				}
			}
			
			means.put(d, mean);
		}
		
		for(ClassDescriptor d: data.keySet()) {
			MultivariateDistribution dist = new MultivariateNormalDistribution(means.get(d), covariance);

			// add to superclass' distributions set
			distributions.put(d, dist);			
		}
	}

	
	public void export(ExportVisitor visitor) {
		/*
		 * The covariance matrix is exported as a model parameter,
		 * since it is the same for all classes. A-priori probabilities
		 * and mean vectors are expored as class-specific parameters.
		 */
		ExportVisitor.Parameters params;
		
		double[][] covariance = null;
		
		for(ClassDescriptor c: distributions.keySet()) {
			MultivariateNormalDistribution dist = (MultivariateNormalDistribution)distributions.get(c);
			
			if(covariance == null) {
				covariance = dist.getCovariance();
			}
			
			params = visitor.newParametersInstance();
			params.setParameter("prior", Double.toString(priors.get(c)));
			params.setParameter("mean", dist.getMean());

			visitor.addClass(c.toString(), params);
		}		

		params = visitor.newParametersInstance();
		
		params.setParameter("type", "Bayes Linear MLE");
		params.setParameter("dimension", Integer.toString(dimension));
		params.setParameter("covariance", covariance);
		
		visitor.setModel(this.getClass().getName(), params);
		
	}

	
	/**
	 * Create a new instance of this classifier using a given model.
	 * 
	 * @param model this map must contain the covariance matrix.
	 * @param classes each class must contain its mean vector.
	 * @return a linear Bayes classifier.
	 */
	public static BayesClassifier newInstance(Map<String, Object> model, Map<ClassDescriptor, Map<String, Object>> classes) {
		BayesClassifier classifier = new NormalMLEClassifier();
		double[][] cov = (double[][])model.get("covariance");
		
		for(ClassDescriptor c: classes.keySet()) {
			double[] mean = (double[])classes.get(c).get("mean");
			Double prior = new Double((String)classes.get(c).get("prior"));
			
			MultivariateDistribution d = new MultivariateNormalDistribution(mean, cov);
			classifier.distributions.put(c, d);
			classifier.priors.put(c, prior);
		}
		
		return classifier;
	}
}
