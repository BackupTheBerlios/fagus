package classify.bayes;

import java.util.Map;
import java.util.List;

import math.statistics.MultivariateDistribution;
import math.statistics.MaximumLikelihoodEstimation;
import math.statistics.MultivariateNormalDistribution;
import math.statistics.SmallSampleSizeNormalDistribution;

import util.ClassDescriptor;
import util.io.Export;
import util.io.ExportVisitor;
import util.io.Import;
import util.io.ModelType;

/**
 * This classifier is a very basic type of a Bayesian classifier.
 * Each class is assumed to have a normal distribution describing
 * its class membership. The training phase is used to adjust the
 * distribution's parameters: the mean vector and covariance matrix.
 * Maximum Likelihood Estimation (MLE) is used for this purpose.
 * This Classifier is able to create quadratic class boundaries.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class NormalMLEClassifier extends BayesClassifier {

	private int dimension;
	
	/**
	 * The training phase adjusts the distribution's parameter. They
	 * are calculated using Maximum likelihood estimation.
	 * 
	 * @param data Provides training vectors for every known class.
	 * @param dimension A training vector's dimension.
	 */
	@Override
	protected void doTrain(Map<ClassDescriptor, List<double[]>> data, int dimension) {
		this.dimension = dimension;

		for(ClassDescriptor d: data.keySet()) {
			double[] mean = MaximumLikelihoodEstimation.getMean(data.get(d), dimension);
			
			List<double[]> samples = data.get(d);
			MultivariateDistribution dist;
			
			if(dimension <= samples.size()) {
				double[][] covariance = MaximumLikelihoodEstimation.getCovariance(samples, mean);
				dist = new MultivariateNormalDistribution(mean, covariance);
			} else {
				double[][] s = new double[samples.size()][dimension];
				s = samples.toArray(s);
				dist = new SmallSampleSizeNormalDistribution(mean, s);
			}

			// add to superclass' distributions set
			distributions.put(d, dist);
		}
	}
	
	@Export(ModelType.CLASSIFIER)
	public void export(ExportVisitor visitor) {
		/*
		 * Export the a-priori probabilities, the mean vectors, and
		 * the covariance matrices as class specific parameters.
		 */
		ExportVisitor.Parameters params = visitor.newParametersInstance();
		
		params.setParameter("type", "Bayes Quadratic MLE");
		params.setParameter("dimension", Integer.toString(dimension));
		
		visitor.setModel(this.getClass().getName(), params);
		
		for(ClassDescriptor c: distributions.keySet()) {
			MultivariateNormalDistribution dist = (MultivariateNormalDistribution)distributions.get(c);
			
			params = visitor.newParametersInstance();
			params.setParameter("prior", Double.toString(priors.get(c)));
			params.setParameter("mean", dist.getMean());
			params.setParameter("covariance", dist.getCovariance());
			
			visitor.addClass(c.toString(), params);
		}
	}
	
	/**
	 * Create a new instance of a quadratic Bayes classifier.
	 *
	 * @param model this is ignored for now.
	 * @param classes each class must at least contain the mean vector and covariance matrix.
	 * @return a quadratic Bayes classifier.
	 */
	@Import(ModelType.CLASSIFIER)
	public static BayesClassifier newInstance(Map<String, Object> model, Map<ClassDescriptor, Map<String, Object>> classes) {
		BayesClassifier classifier = new NormalMLEClassifier();
		
		for(ClassDescriptor c: classes.keySet()) {
			double[] mean = (double[])classes.get(c).get("mean");
			double[][] cov = (double[][])classes.get(c).get("covariance");
			Double prior = new Double((String)classes.get(c).get("prior"));
			
			MultivariateDistribution d = new MultivariateNormalDistribution(mean, cov);
			classifier.distributions.put(c, d);
			classifier.priors.put(c, prior);
		}
		
		return classifier;
	}

}
