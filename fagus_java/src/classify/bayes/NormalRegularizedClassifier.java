package classify.bayes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import math.statistics.MaximumLikelihoodEstimation;
import math.statistics.MultivariateDistribution;
import math.statistics.MultivariateNormalDistribution;

import util.ClassDescriptor;
import util.io.Export;
import util.io.ExportVisitor;
import util.io.Import;
import util.io.ModelType;

/**
 * This class provides an extension to a quadratic and linear Bayes
 * classifier. A regularization term interpolates the common covariance
 * matrix (as used for linear classification) and the class-specific
 * covariance matrices (as used in quadratic classification).
 * 
 * <br><br>
 * See Chapter 4.3.1 "Regularized Discriminant Analysis" of,
 *     T. Hastie, R. Tibshirani, and J. Friedman
 *     "The Elements of Statistical Learning"
 *     Springer, 2001
 *     
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class NormalRegularizedClassifier extends BayesClassifier {
	private final double regularization;
	private int dimension;
	
	/**
	 * Create a new regularized Bayes classifier. The regularization
	 * term is used to interpolate between a linear and a quadratic
	 * classifier.
	 * 
	 * @param regularization a value in [0,1]. 0 is equal to a linear
	 *        classifier, while 1 is equal to a quadratic one.
	 */
	public NormalRegularizedClassifier(double regularization) {
		this.regularization = regularization;
	}
	
	@Override
	protected void doTrain(Map<ClassDescriptor, List<double[]>> data, int dimension) {
		this.dimension = dimension;
		
		Map<ClassDescriptor, double[]> means = new HashMap<ClassDescriptor, double[]>();
		Map<ClassDescriptor, double[][]> covariances = new HashMap<ClassDescriptor, double[][]>();
		
		for(ClassDescriptor d: data.keySet()) {
			double[] m = MaximumLikelihoodEstimation.getMean(data.get(d), dimension);
			double[][] cov = MaximumLikelihoodEstimation.getCovariance(data.get(d), m);
			
			means.put(d, m);
			covariances.put(d, cov);
		}
		
		regularize(covariances);
		
		for(ClassDescriptor d: data.keySet()) {
			distributions.put(d, new MultivariateNormalDistribution(means.get(d), covariances.get(d)));
		}
	}

	@Export(ModelType.CLASSIFIER)
	public void export(ExportVisitor visitor) {
		/*
		 * Export the a-priori probabilities, the mean vectors, and
		 * the covariance matrices as class specific parameters. There
		 * is no need to export the regularization parameter since it
		 * is only used during the training phase.
		 */
		ExportVisitor.Parameters params = visitor.newParametersInstance();
		
		params.setParameter("type", "Bayes Regularized MLE");
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
	 * Create a new instance of a regularized Bayes classifier.
	 *
	 * @param model this is ignored for now.
	 * @param classes each class must at least contain the mean vector and covariance matrix.
	 * @return a quadratic Bayes classifier.
	 */
	@Import(ModelType.CLASSIFIER)
	public static BayesClassifier newInstance(Map<String, Object> model, Map<ClassDescriptor, Map<String, Object>> classes) {
		/*
		 * We do not instanciate a regularized classifier here, but a
		 * quadratic one. The regularization parameter is not used
		 * in the testing phase and the covariance matrices have been
		 * regularized before exporting the model file.
		 */
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

	
	private void regularize(Map<ClassDescriptor, double[][]> covariances) {
		
		double[][] commonCovariance = new double[dimension][dimension];
		
		for(ClassDescriptor c: covariances.keySet()) {
			double[][] cov = covariances.get(c);
			double p = priors.get(c);
			
			for(int i = 0; i < dimension; i++) {
				commonCovariance[i][i] += p * cov[i][i];
				for(int j = i + 1; j < dimension; j++) {
					/*
					 * Only estimate an upper triangular matrix.
					 * The common covariance matrix is symmetric. 
					 */
					commonCovariance[i][j] += p * cov[i][j];
				}
			}
		}
		
		for(ClassDescriptor c: covariances.keySet()) {
			double[][] cov = covariances.get(c);

			for(int i = 0; i < dimension; i++) {
				cov[i][i] = regularization * cov[i][i]
				            + (1.0 - regularization) * commonCovariance[i][i];
				
				for(int j = i + 1; j < dimension; j++) {
					double u = regularization * cov[i][j]
					           + (1.0 - regularization) * commonCovariance[i][j];
					
					cov[i][j] = cov[j][i] = u;
				}
			}
		}		
	}

}
