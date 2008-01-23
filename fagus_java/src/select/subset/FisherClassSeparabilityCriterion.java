package select.subset;

import java.util.List;
import java.util.Map;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

import math.statistics.MaximumLikelihoodEstimation;
import math.statistics.Scatter;

import util.ClassDescriptor;
import util.VectorSet;

/**
 * The Fisher criterion gives some hints about class separability. High 
 * criterion values indicate good (linear) separability of classes. The
 * criterion is basically the ratio of between-class scatter and
 * within-class scatter.
 * 
 * <br><br>
 * See Chapter 10 "Feature Extraction and Linear Mapping for Classification"
 *     in K. Fukunaga
 *     "Introduction to statistical pattern recognition"
 *     2nd edition
 *     Academic Press, 1990
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class FisherClassSeparabilityCriterion implements CriterionFunction {
	private double[][] scatterWithin;
	private double[][] scatterBetween;
	private int dimension;
	private int[] allFeatures;
	
	/**
	 * Get the criterion value for all features used in the
	 * initialization phase.
	 */
	public double getCriterionValue() {
		return getCriterionValue(allFeatures);
	}

	/**
	 * Get the criterion value for a subset of features.
	 */
	public double getCriterionValue(int[] features) {
		double[][] sw, sb;
		
		if(features.length < dimension) {
			// create new scatter matrices, including the desired features only
			sw = new double[features.length][features.length];
			sb = new double[features.length][features.length];
			
			for(int i = 0; i < features.length; i++) {
				sw[i][i] = scatterWithin[features[i]][features[i]];
				sb[i][i] = scatterBetween[features[i]][features[i]];
				
				for(int j = i + 1; j < features.length; j++) {
					sw[i][j] = sw[j][i] = scatterWithin[features[i]][features[j]];
					sb[i][j] = sb[j][i] = scatterBetween[features[i]][features[j]];
				}
			}
		} else {
			sw = scatterWithin;
			sb = scatterBetween;
		}

		/*
		 * The citerion is
		 * 
		 * J = trace(sw^(-1) . sb)
		 * 
		 * We use an alternative computation suggested by Martinez and Zhu
		 * which does not require an invertation of the within-class scatter
		 * matrix, and is thus applicable to singular problems. The algoithm
		 * performs an eigenvalue decomposition on both scatter matrices.
		 * 
		 * Let lambdaB_i and lambdaW_j be the i-th eigenvalue of the
		 * between-class scatter and the j-th eigenvalue of the within-class
		 * scatter, respectively, and let vB_i and vW_j be the respective 
		 * eigenvectors. Then the above trace can be written as
		 * 
		 * sum_i=1^q sum_j=1^p (vW_j^T vB_i)^2 * lambdaB_i / lambdaW_j
		 * 
		 * where q and p are the ranks of the respective scatter matrices.
		 * 
		 * See Theorem 2 of A.M. Martinez and M. Zhu
		 *     "Where are Linear Feature Extraction Methods Applicable?"
		 *     IEEE Transactions on Pattern Analysis and Machine Intelligence
		 *     Vol 27. No 12. pp 1934-1944
		 *     IEEE, 2005
		 */
		Matrix w = new Matrix(sw);
		Matrix b = new Matrix(sb);
		
		EigenvalueDecomposition ew = new EigenvalueDecomposition(w);
		EigenvalueDecomposition eb = new EigenvalueDecomposition(b);
		
		double[] eigenvaluesW = ew.getRealEigenvalues();
		double[] eigenvaluesB = eb.getRealEigenvalues();
		
		/*
		 * Note, that the eigenvectors are the columns (not rows) of
		 * Jama's V matrix.
		 */
		double[][] eigenvectorsW = ew.getV().getArray();
		double[][] eigenvectorsB = eb.getV().getArray();
		
		double result = 0.0;
		
		for(int i = 0; i < eigenvaluesB.length; i++) {
			for(int j = 0; j < eigenvaluesW.length; j++) {
				/*
				 * Since we want to use this algorithm for singular
				 * problems, we must check the eigenvalues of the
				 * within-class scatter to be different from zero. Note,
				 * that negative eigenvalues cannot appear due to the
				 * positive semi-definiteness of the scatter matrix.
				 */
				if(eigenvaluesW[j] >= 10e-10) {
					double v = 0.0;
				
					for(int k = 0; k < features.length; k++) {
						v += eigenvectorsW[k][j] * eigenvectorsB[k][i];
					}
				
					result += v * v * eigenvaluesB[i] / eigenvaluesW[j];
				}
			}
		}
		
		return result;
	}

	/**
	 * Initialize scatter matrices.
	 */
	public void initialize(int dimension, VectorSet data) {
		this.dimension = dimension;
		int nClasses = data.getClassDescriptors().size();
		int totalVectors = data.getData().size();
		Map<ClassDescriptor, List<double[]>> d = data.getInvertedData();
		
		double[] p = new double[nClasses];           // class propabilities
		double[][] mean = new double[nClasses][];    // means
		double[][][] cov = new double[nClasses][][]; // covariance matrices

		int i = 0;
		for(ClassDescriptor c: d.keySet()) {
			p[i] = (double)d.get(c).size() / totalVectors;
			mean[i] = MaximumLikelihoodEstimation.getMean(d.get(c), dimension);
			cov[i] = MaximumLikelihoodEstimation.getCovariance(d.get(c), mean[i]);
			i++;
		}
		
		scatterWithin = Scatter.getWithinClassScatter(cov, p);
		scatterBetween = Scatter.getBetweenClassScatter(mean, p);
		
		allFeatures = new int[dimension];
		for(i = 0; i < dimension; i++) {
			allFeatures[i] = i;
		}
	}

}
