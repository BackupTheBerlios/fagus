package select.subset;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

import math.statistics.MaximumLikelihoodEstimation;

import util.ClassDescriptor;
import util.VectorSet;

/**
 * This criterion function is an implementation of the Bhattacharyya
 * Distance. This distance can be used to obtain an upper bound for the
 * Bayes error for quadratic classifiers. Note, that this criterion is 
 * only applicable for two class problems.
 * 
 * <br><br>
 * See Chapter 3.4 "Upper Bounds on the Bayes Error" 
 *     in K. Fukunaga
 *     "Introduction to statistical pattern recognition"
 *     2nd edition
 *     Academic Press 1990
 *     
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class BhattacharyyaDistance implements RecursiveCriterionFunction {

	private int dimension;
	private double[] mean; // mean2 - mean1
	private double[][] covariance1;
	private double[][] covariance2;
	private double[][] covarianceMixture; // (cov1 + cov2) / 2;
	
	private int[] allFeatures;
	
	/**
	 * Get the criterion value. Higher values indicate better
	 * class separability.
	 * @return the Bhattacharyya distance
	 */
	public double getCriterionValue() {
		return getCriterionValue(allFeatures);
	}
	
//	/**
//	 * Get the criterion value for a subset of features.
//	 */
//	public double getCriterionValue1(int[] features) {
//		double mu1 = 0.0;
//		double mu2 = 0.0;
//		double lnCovMixDet, lnCov1Det, lnCov2Det;
//		double[][] covMix, cov1, cov2, invCovMix;
//		
//		if(features.length < dimension) {
//			covMix = getSubMatrix(features, covarianceMixture);
//			cov1 = getSubMatrix(features, covariance1);
//			cov2 = getSubMatrix(features, covariance2);
//		} else {
//			covMix = covarianceMixture;
//			cov1 = covariance1;
//			cov2 = covariance2;
//		}
//		
//		invCovMix = new double[features.length][features.length];
//		lnCovMixDet = getInverseAndLogDeterminant(covMix, invCovMix);
//
//		lnCov1Det = getLogDeterminant(cov1);
//		lnCov2Det = getLogDeterminant(cov2);
//		
//		for(int i = 0; i < features.length; i++) {
//			mu1 += mean[features[i]] * mean[features[i]] * invCovMix[i][i];
//		}
//		
//		mu1 = mu1 / 2.0;
//		
//		for(int i = 0; i < features.length - 1; i++) {
//			for(int j = i + 1; j < features.length; j++) {
//				mu1 += mean[features[i]] * mean[features[j]] * invCovMix[i][j];
//			}
//		}
//		
//		mu1 = mu1 / 4.0;
//		
//		mu2 = (lnCov1Det + lnCov2Det) / 2.0;
//		mu2 = (lnCovMixDet - mu2) / 2.0;
//		
//		return mu1 + mu2;
//	}
	
	public double getCriterionValue(int[] features) {
		double mu = 0;
		EigenvalueDecomposition eig;
		double[] eigenvalues;
		double[][] eigenvectors;
		
		/*
		 * The distance is given by
		 * 
		 * mu = 1/8 * (mean2 - mean1)^T . ((Sigma1 + Sigma2)/2)^-1 . (mean2 - mean1)
		 *      +  1/2 * Log( |(Sigma1 + Sigma2)/2| / Sqrt(|Sigma1| * |Sigma2|) )
		 * 
		 * See K. Fukunaga "Introduction to Statistical Pattern Recognition"
		 *     Academic Press, 1990
		 *     
		 * This can be further simplified to
		 * 
		 * mu = 1/2 *( 
		 *        Log|(Sigma1 + Sigma2)/2| + 1/2 * ( 
		 *          1/2 * (mean2 - mean1)^T . ((Sigma1 + Sigma2)/2)^-1 . (mean2 - mean1)
		 *          - Log|Sigma1| - Log|Sigma2|
		 *        )
		 *      )
		 */
		
		/*
		 * Create mixture matrix
		 *  
		 * SigmaMix = (Sigma1 + Sigma2) / 2
		 */
		double[][] covMix;
		double[][] cov1;
		double[][] cov2;
		
		if(features.length < dimension) {
			covMix = getSubMatrix(features, covarianceMixture);
			cov1 = getSubMatrix(features, covariance1);
			cov2 = getSubMatrix(features, covariance2);
		} else {
			covMix = covarianceMixture;
			cov1 = covariance1;
			cov2 = covariance2;
		}
		
		/*
		 * Calculate the log of the determinant and the inverse of
		 * the mixture matrix. The log of the determinant is the sum of
		 * the logarithms of the eigenvalues. The inverse is obtained by
		 * 
		 * SigmaMix^-1 = Sum_1^n (phi_i . phi_i^T) / lambda_i
		 * 
		 * where phi_i and lambda_i is the i-th eigenvector and eigenvalue,
		 * respectively. The inverse is not calculate explicitely. Instead,
		 * one calculates the elements of
		 * 
		 * mean^T . SigmaMix^-1 . mean
		 * 
		 * The result of this matrix multiplication is split into two sums.
		 * One handles the diagonal elements of Sigma^-1 while the other
		 * handles the off-diagonal elements.
		 */
		double lnCovMixDet = 0;
		double diagonal = 0;
		double offDiagonal = 0;
		
		eig = new EigenvalueDecomposition(new Matrix(covMix));
		eigenvectors = eig.getV().getArray();
		eigenvalues = eig.getRealEigenvalues();
		
		for(int i = 0; i < features.length; i++) {
			// last diagonal element is not handled by the following loop
			// so we initialize u1 appropriately
			double u1 = eigenvectors[features.length-1][i] * mean[features[features.length-1]];
			u1 = u1 * u1;
			
			double u2 = 0;
			
			for(int j = 0; j < features.length - 1; j++) {
				double u;
				
				u = eigenvectors[j][i] * mean[features[j]];
				u1 += u * u;  
				
				u = 0;
				
				for(int k = j + 1; k < features.length; k++) {
					u += eigenvectors[k][i] * mean[features[k]];
				}
				
				u2 += u * mean[features[j]] * eigenvectors[j][i];
			}
			
			diagonal += u1 / eigenvalues[i];
			offDiagonal += u2 / eigenvalues[i];
			
			// Note, that all eigenvalues are guaranteed to be positive
			lnCovMixDet += Math.log(eigenvalues[i]);
		}
		
		/*
		 * mu = (mu + Sum_(i=1)^n mean_i^2 SigmaMix^(-1)_ii)/2
		 * mu = mu + Sum_(i=1)^(n-1) Sum_(j=i)^n mean_i * mean_j * SigmaMix^(-1)_ij
		 */
		mu = (mu + diagonal) / 2.0 + offDiagonal;

		/*
		 * mu = mu - ln|Sigma1| - ln|Sigma2|
		 */
		mu -= getLogDeterminant(cov1);
		mu -= getLogDeterminant(cov2);

		/*
		 * mu = (mu / 2 + Log|SigmaMix|) / 2
		 */
		return ((mu / 2.0) + lnCovMixDet)/ 2.0;
		
	}
	
	public void initialize(int dimension, VectorSet data) {
		this.dimension = dimension;
		allFeatures = new int[dimension];
		Map<ClassDescriptor, List<double[]>> d = data.getInvertedData();
		
		for(int i = 0; i < dimension; i++) {
			allFeatures[i] = i;
		}
		
		Iterator<ClassDescriptor> it = d.keySet().iterator();	
		ClassDescriptor c1 = it.next();
		ClassDescriptor c2 = it.next();
		
		double[] m1 = MaximumLikelihoodEstimation.getMean(d.get(c1), dimension);
		double[] m2 = MaximumLikelihoodEstimation.getMean(d.get(c2), dimension);
		
		mean = new double[dimension];
		for(int i = 0; i < dimension; i++) {
			mean[i] = m2[i] - m1[i];
		}
		
		covariance1 = MaximumLikelihoodEstimation.getCovariance(d.get(c1), m1);
		covariance2 = MaximumLikelihoodEstimation.getCovariance(d.get(c2), m2);
		
		// SigmaMix = (Sigma1 + Sigma2) / 2
		covarianceMixture = new double[dimension][dimension];
		for(int i = 0; i < dimension; i++) {
			covarianceMixture[i][i] = (covariance1[i][i] + covariance2[i][i]) / 2.0;
			
			for(int j = i + 1; j < dimension; j++) {
				covarianceMixture[i][j] = covarianceMixture[j][i] = (covariance1[i][j] + covariance2[i][j]) / 2.0;
			}
		}
	}

	
	/**
	 * Get the initial state for recursive Branch &amp; Bound.
	 */
	public RecursiveCriterionFunction.State getCriterionState() {
		return new BhattacharyyaState();
	}
	
	
	/**
	 * Get a successor state for recursive Branch &amp; Bound.
	 */
	public RecursiveCriterionFunction.State getCriterionState(int feature, RecursiveCriterionFunction.State parentState) {
		return new BhattacharyyaState(feature, (BhattacharyyaState)parentState);
	}
	
	
	/*
	 * Get a matrix from an original matrix with all elements not in
	 * the feature vector left out. 
	 */
	private double[][] getSubMatrix(int[] features, double[][] orig) {
		double[][] subMatrix = new double[features.length][features.length];
		
		for(int i = 0; i < features.length; i++) {
			subMatrix[i][i] = orig[features[i]][features[i]];
			
			for(int j = i + 1; j < features.length; j++) {
				subMatrix[i][j] = subMatrix[j][i] = orig[features[i]][features[j]];
			}			
		}
		
		return subMatrix;
	}
	
	
	/*
	 * Calculate the inverse and the logarithm of the determinant of a matrix
	 * simultanously. Note, that the inverse must be initialized and be set to
	 * zero. The original and the inverse are assumed to have the same dimension
	 * n x n and be symmetric.
	 */
	private double getInverseAndLogDeterminant(double[][] orig, double[][] inverse) {
		EigenvalueDecomposition eig = new EigenvalueDecomposition(new Matrix(orig));
		double[] eigenvalues = eig.getRealEigenvalues();
		double[][] eigenvectors = eig.getV().getArray();
		
		double lnDet = 0.0;
		
		for(int i = 0; i < orig.length; i++) {
			lnDet += Math.log(eigenvalues[i]);
			
			for(int j = 0; j < orig.length; j++) {
				inverse[j][j] += eigenvectors[j][i] * eigenvectors[j][i] / eigenvalues[i];
				
				for(int k = j + 1; k < orig.length; k++) {
					double u = eigenvectors[j][i] * eigenvectors[k][i] / eigenvalues[i];
					inverse[j][k] += u;
					inverse[k][j] += u;
				}
			}
		}
		
		return lnDet;
	}

	/*
	 * Get the logarithm of the determinant of a positive
	 * (semi-)definite symmetric matrix.
	 */
	private double getLogDeterminant(double[][] a) {
		return Math.log((new Matrix(a)).det());
	}
	
	
	/*
	 * This is the state class for recursive Branch & Bound. The inverse
	 * can be calculated from the inverse of the parent node. The determinant
	 * can be calculated from the inverse of the current node, the determinant
	 * of the parent node, and the original matrix.
	 * 
	 * See Chapter 10 "Feature Extraction and Linar Mapping for Classification"
	 *     in K. Fukunaga
	 *     "Introduction to statistical pattern recognition"
	 *     2nd edition
	 *     Academic Press 1990
	 *     page 498ff
	 */
	private class BhattacharyyaState implements RecursiveCriterionFunction.State {
		private final int[] config;
		private final double[][] inverseCovMix;
		private final double lnCovMixDet;
		private final double[][] inverseCov1;
		private final double[][] inverseCov2;
		private final double lnCov1Det;
		private final double lnCov2Det;
		private final double value;
		private final int feature;
		
		/*
		 * Get the state for the root node (with all features present).
		 */
		public BhattacharyyaState() {
			this.feature = -1;
			this.config = allFeatures;
			
			inverseCovMix = new double[dimension][dimension];
			lnCovMixDet = getInverseAndLogDeterminant(covarianceMixture, inverseCovMix);
			
			inverseCov1 = new double[dimension][dimension];
			lnCov1Det = getInverseAndLogDeterminant(covariance1, inverseCov1);
			
			inverseCov2 = new double[dimension][dimension];
			lnCov2Det = getInverseAndLogDeterminant(covariance2, inverseCov2);
			
			value = getBhattacharyyaDistance();
		}
		
		
		/*
		 * Get the state for a child node.
		 */
		public BhattacharyyaState(int feature, BhattacharyyaState parentState) {
			this.feature = feature;
			
			final int n = parentState.config.length - 1;
			config = new int[n];
			int indexInParent = 0;
			int[] newIndex = new int[n];
			
			for(int i = 0; i < n; i++) {
				if(parentState.config[i] == feature) {
					indexInParent = i;
					
					for(; i < n; i++) {
						newIndex[i] = i + 1;
						config[i] = parentState.config[i + 1];
					}
				} else {
					newIndex[i] = i;
					config[i] = parentState.config[i];
				}
			}
			
			// for every matrix, calculate the child inverse and determinant from it's parent
			inverseCovMix = getChildInverse(parentState.inverseCovMix, indexInParent, newIndex);
			inverseCov1 = getChildInverse(parentState.inverseCov1, indexInParent, newIndex);
			inverseCov2 = getChildInverse(parentState.inverseCov2, indexInParent, newIndex);
			
			lnCovMixDet = getChildDeterminant(inverseCovMix, covarianceMixture, parentState.lnCovMixDet);
			lnCov1Det = getChildDeterminant(inverseCov1, covariance1, parentState.lnCov1Det);
			lnCov2Det = getChildDeterminant(inverseCov2, covariance2, parentState.lnCov2Det);
		
			value = getBhattacharyyaDistance();
		}
		
		/*
		 * Get the criterion value.
		 */
		public double getValue() {
			return value;
		}
		
		public int[] getConfig() {
			return config;
		}
		
		public int getRemovedFeature() {
			return feature;
		}
		
		private double getBhattacharyyaDistance() {
			double mu1 = 0.0;
			double mu2 = 0.0;
			
			for(int i = 0; i < config.length; i++) {
				mu1 += mean[config[i]] * mean[config[i]] * inverseCovMix[i][i];
			}
			
			mu1 = mu1 / 2.0;
			
			for(int i = 0; i < config.length - 1; i++) {
				for(int j = i + 1; j < config.length; j++) {
					mu1 += mean[config[i]] * mean[config[j]] * inverseCovMix[i][j];
				}
			}
			
			mu1 = mu1 / 4.0;
			
			mu2 = (lnCov1Det + lnCov2Det) / 2.0;
			mu2 = (lnCovMixDet - mu2) / 2.0;
			
			return mu1 + mu2;			
		}
		
		private double[][] getChildInverse(double[][] parentInverse, int indexInParent, int[] newIndex) {
			double[][] a = getSubMatrix(newIndex, parentInverse);
			
			for(int i = 0; i < config.length; i++) {
				a[i][i] -= (parentInverse[indexInParent][newIndex[i]] * parentInverse[indexInParent][newIndex[i]]) / parentInverse[indexInParent][indexInParent];
				
				for(int j = i + 1; j < config.length; j++) {
					double u = (parentInverse[indexInParent][newIndex[i]] * parentInverse[indexInParent][newIndex[j]]) / parentInverse[indexInParent][indexInParent];
					a[i][j] -= u;
					a[j][i] -= u;
				}
			}
			
			return a;
		}
		
		private double getChildDeterminant(double[][] childInverse, double[][] orig, double parentDet) {
			double d = 0.0;
			
			for(int i = 0; i < config.length - 1; i++) {
				for(int j = i + 1; j < config.length; j++) {
					d += orig[feature][config[i]] * orig[feature][config[j]] * childInverse[i][j];
				}
			}
			
			d = d * 2;
			
			for(int i = 0; i < config.length; i++) {
				d += orig[feature][config[i]] * orig[feature][config[i]] * childInverse[i][i];
			}
			
			return parentDet - Math.log(orig[feature][feature] - d);
		}
	}
}
