package math.statistics;

import java.util.Arrays;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
//import Jama.QRDecomposition;

/**
 * This is an implementation of a multivariate normal distribution.
 * This particular type of distribution is defined by its parameters
 * mu and Sigma, where mu is the mean vector and Sigma is the
 * covariance matrix.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class MultivariateNormalDistribution implements MultivariateDistribution {
	protected final int dimension;
	protected final double[] mean;
	protected double[][] covarianceMatrix;
	protected double lnCovarianceDeterminant;
	protected double[][] inverseCovariance;
	
	protected MultivariateNormalDistribution(double[] mean) {
		this.mean = mean;
		this.dimension = mean.length;
	}

	
	/**
	 * Create a new NormalDistribution with given parameters.
	 * @param mean the mean vector for this distribution.
	 * @param covarianceMatrix the covariance matrix for this distribution.
	 */
	public MultivariateNormalDistribution(double[] mean, double[][] covarianceMatrix) {
		this(mean);
		this.covarianceMatrix = covarianceMatrix;
		init();
	}
	
	/**
	 * Return the discriminant of this distribution.
	 * It is evaluated by:
	 * <pre>
	 * g_i(x) := b - (a + c + log(|Sigma|))/ 2
	 * a := x<sup>T</sup> Sigma<sup>-1</sup> x
	 * b := Sigma<sup>-1</sup> mu x
	 * c := mu<sup>T</sup> Sigma<sup>-1</sup> mu
	 * </pre>
	 * <br><br>
	 * See Chapter 2.8 "Discriminant Functions for the Normal Density"
	 *      of R. Duda and P. Hart. "Pattern Classification and Scene Analysis". 
	 *      John Wiley &amp; Sons. 1973
	 * 
	 * @param vector evalutate descriminant at this point.
	 * @return the result.
	 */
	public double getDiscriminant(double[] vector) {
		double a = 0.0; // x^T (Sigma^-1) x
		double b = 0.0; // w x
		double c = 0.0; // mu^T w
		
		for(int i = 0; i < dimension; i++) {
			double tmp = 0.0; // update to a
			double w = 0.0; // Sigma^-1 mu
			
			for(int j = 0; j < dimension; j++) {
				tmp += inverseCovariance[i][j] * vector[j];
				w += inverseCovariance[i][j] * mean[j];
			}
			a += tmp * vector[i];
			b += w * vector[i];
			c += w * mean[i];
		}
		
		return b - (a + c + lnCovarianceDeterminant) / 2;
	}
	
	/**
	 * Get the discrimiant value, assuming that the class has
	 * a given a-priori probability. The 
	 * <pre>
	 * log(prior)
	 * </pre>
	 * is added to the standard discriminant function.
	 */
	public double getDiscriminant(double[] vector, double prior) {
		return getDiscriminant(vector) + Math.log(prior);
	}
	
	/**
	 * Get the mean vector of this distribution.
	 * @return
	 */
	public double[] getMean() {
		return mean;
	}

	/**
	 * Get the covariance matrix of this distribution.
	 * @return
	 */
	public double[][] getCovariance() {
		return covarianceMatrix;
	}
	
	/**
	 * Adjust eigenvalues according to a dynamic bound. Small eigenvalues
	 * can cause severe numerical errors when calculating a determinant
	 * or the inverse matrix. Therefore, these values are replaced with the
	 * average of all unsignificant eigenvalues. The sum of unsignificant
	 * eigenvalue is
	 * 
	 * lambda_avg / 10^8
	 * 
	 * where lambda_avg is the arithmetic mean of all eigenvalues.
	 * 
	 * See Chapter 2 "Random Vectors and their Properties"
	 *     of K. Fukunaga, "Introduction to statistical pattern recognition".
	 *     2nd edition. Academic Press. 1990 
	 *     
	 * @param eigenvalues the eigenvalues. They might be changed by this
	 *        procedure.
	 */
	protected void adjustEigenvalues(double[] eigenvalues) {
		// get the sum of eigenvalues
		double eigensum = 0.0;
		for(int i = 0; i < eigenvalues.length; i++) {
			eigensum += eigenvalues[i];
		}
		
		// Get an ascending list of eigenvalues. Note that Arrays.sort()
		// works in place. We therefore copy the eigenvalue array first.
		double[] sortedEigenvalues = new double[eigenvalues.length];
		System.arraycopy(eigenvalues, 0, sortedEigenvalues, 0, eigenvalues.length);
		Arrays.sort(sortedEigenvalues);
		
		
		// Find the unsignificant eigenvalues.
		double unsignificantSum = 0.0;
		int n = 0; // number of unsignificant eigenvalues
		
		while(unsignificantSum < eigensum / (eigenvalues.length * 10E8)) {
			//TODO: 10E8 is an empirical value. Use some more reasonable one.
			
			unsignificantSum += sortedEigenvalues[n];
			n++;
		}
		
		// Update the original eigenvalue array.
		if(n > 1) {
			double eigenvalueBound = sortedEigenvalues[n - 1];
			double average = unsignificantSum / n;
			
			for(int i = 0; i < eigenvalues.length; i++) {
				if(eigenvalues[i] <= eigenvalueBound) {
					// replace with average of all unsignificant eigenvalues
					eigenvalues[i] = average;
				}
			}
		}
	}

	protected void init() {
		/*
		 * In this step we need to calculate the covariance matrix's determinant
		 * and its inverse. Both can be derived from its eigenvalues and eigenvectors.
		 * We therefore perform an eigenvalue decomposition as a preprocessing step.
		 * 
		 * The formulas for both, the covariance matrix's determinant and the inverse
		 * covariance matrix, can be found in 
		 *     Chapter 2 "Random Vectors and their Properties" 
		 *     of K. Fukunaga, "Introduction to statistical pattern recognition".
		 *     2nd edition. Academic Press. 1990 
		 */
		Matrix cm = new Matrix(covarianceMatrix);
		
//		// Get logarithm of determinant
//		lnCovarianceDeterminant = Math.log(cm.det());
//		
//		/*
//		 * Get inverse matrix:
//		 * 
//		 * For the sake of numerical stability, we use QR
//		 * decomposition instead of gaussian elimination.
//		 */
//		Matrix id = Matrix.identity(dimension, dimension);
//		QRDecomposition qr = new QRDecomposition(cm);
//		inverseCovariance = qr.solve(id).getArray();
		
		EigenvalueDecomposition eig = cm.eig();
		double[] eigenvalues = eig.getRealEigenvalues();
		Matrix eigenvectors = eig.getV();

		/*
		 * Get the natural logarithm of the covariance-matrix's determinant.
		 * Since the covariance matrix is guaranteed to be symetric, the determinant
		 * is equal to the product of its eigenvalues.
		 * 
		 * |Sigma| = lambda_1 * ... * lambda_n
		 * 
		 * The determinant's logarithm is therefore equal to the sum of the 
		 * eigenvalues' logarithms
		 * 
		 * ln |Sigma| = ln(lambda_1) + ... + ln(lambda_n)
		 * 
		 * A near-singular matrix, i.e. a matrix with some eigenvalues close to zero,
		 * may cause severe numerical errors. We therefore adjust eigenvalues according
		 * to some dynamic bound.
		 */
		
		//TODO: find some way to automatically turn eigenvalue adjustment
		//      on or off
		//adjustEigenvalues(eigenvalues);
		lnCovarianceDeterminant = 0.0;
		
		for(int i = 0; i < dimension; i++) {
			lnCovarianceDeterminant += Math.log(eigenvalues[i]);
		}
		
		/*
		 * Get the inverse of the covariance matrix.
		 * 
		 * Sigma^-1 = (phi_1 . phi_1^T) / lambda_1 + ... + (phi_n . phi_n^T) / lambda_n
		 * 
		 * where phi_i is the ith eigenvector of Sigma and lambda_i is the ith
		 * eigenvalue of Sigma.
		 */
		inverseCovariance = new double[dimension][dimension];
		// Java initializes this array with zeros
		
		for(int i = 0; i < dimension; i++) {
			double lambda = eigenvalues[i];
			
			for(int j = 0; j < dimension; j++) {
				// diagonal element
				inverseCovariance[j][j] += eigenvectors.get(j,i) * eigenvectors.get(j,i)/lambda;
				
				// other elements - matrix is symetric
				for(int k = 0; k < j; k++) {
					double u = eigenvectors.get(j,i) * eigenvectors.get(k,i)/lambda;
					inverseCovariance[j][k] += u;
					inverseCovariance[k][j] += u;
				}
			}
		}
	}
}
