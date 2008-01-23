package math.statistics;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * This is an implementation of a multivariate normal distribution,
 * paritcularly suited for small sample sizes. A small sample size in
 * this contex is a samle size smaller than the dimension of the
 * vector space.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class SmallSampleSizeNormalDistribution extends MultivariateNormalDistribution {

	public SmallSampleSizeNormalDistribution(double[] mean, double[][] samples) {
		super(mean);
		int m = samples.length;
		
		/*
		 * This algorithm works by first calculating the eigenvalues and
		 * eigenvectors of the autocorrelation matrix. The logarithm of
		 * the covariance matrix's determinant can be obtained from the 
		 * logarithm of the autocorrelation marix's determinant. Similarly,
		 * the inverse covariance matrix can be obtained from the
		 * inverse autocorrelation matrix.
		 */
		
		// create a sample matrix U
		Matrix ut = new Matrix(samples);
		Matrix u = ut.transpose();
		
		/*
		 * Get the eigenvalues and eigenvectors for (U^T.U)
		 * Note, that this is an m x m matrix and therefore
		 * the eigenvalue matrix Phi' and the eigenvector
		 * matrix Lambda are also m x m. 
		 */
		EigenvalueDecomposition eig = (ut.times(u)).eig();
		
		/*
		 * Get the eigenvalues of the autocorrelation matrix. Note,
		 * that there are only m eigenvalues, since the rank of the 
		 * autocorrelation matrix is m. The other eigenvalues are all
		 * zero and their corresponding eigenvectors are undefined.
		 */
		double[] eigenvalues = eig.getRealEigenvalues();
		//adjustEigenvalues(eigenvalues);
		
		/*
		 * Get the autocorrelation matrix's eigenvectors. These
		 * are calculated by 
		 * 
		 * phi_j = 1 / sqrt(m_j * lambda_j) U . phi'_j
		 * 
		 * where phi'_j is the jth eigenvector of the above
		 * eigenvalue decomposition. 
		 */
		double[][] eigenvectors = new double[dimension][m];
		
		for(int j = 0; j < m; j++) {
			double q = Math.sqrt(m * eigenvalues[j]);
			
			for(int i = 0; i < dimension; i++) {
				eigenvectors[i][j] = 0;
				
				for(int k = 0; k < m; k++) {
					eigenvectors[i][j] += u.get(i, k) * eig.getV().get(k, j);
				}
				
				// make orthonormal eigenvecors
				eigenvectors[i][j] = eigenvectors[i][j] / q;
			}
		}
		
		
		/*
		 * The inverse autocorrelation matrix S^-1 can be obtained by 
		 * the eigenvalues and eigenvectors.
		 * 
		 * S =  phi_1 . phi_1^T / lambda_1 + ... + phi_m . phi_m^T / lambda_m
		 * 
		 * Note, that only m eigenvalues are non-zero and therefore only m
		 * eigenvectors are defined.
		 */
		double[][] inverseAutocorrelation = new double[dimension][dimension];
		for(int i = 0; i < m; i++) {
			double lambda = eigenvalues[i];
			
			for(int j = 0; j < dimension; j++) {
				// diagonal element
				inverseAutocorrelation[j][j] += eigenvectors[j][i] * eigenvectors[j][i]/lambda;
				
				// other elements - matrix is symetric
				for(int k = 0; k < j; k++) {
					double tmp = eigenvectors[j][i] * eigenvectors[k][i]/lambda;
					inverseAutocorrelation[j][k] += tmp;
					inverseAutocorrelation[k][j] += tmp;
				}
			}
		}
		
		double lambda1 = 0.0;
		double[] t = new double[dimension];
		for(int i = 0; i < dimension; i++) {
			t[i] = 0.0;
			for(int j = 0; j < dimension; j++) {
				t[i] += inverseAutocorrelation[i][j] * mean[j];
			}
			lambda1 += t[i] * mean[i];
		}
		
		/*
		 * Get the logarithm of the covariance determinant. The autocorrelation
		 * matrix's S determinant is the product of its eigenvalues, and therefore
		 * 
		 * ln|S| = ln(lambda_1) + ... + ln(lambda_m)
		 * 
		 * The covarinace matrix's determinant can be obtained from
		 * 
		 * |Sigma| = |S| * ( 1 - lambda1)
		 * 
		 * and therefore
		 * 
		 * ln|Sigma| = ln|S| + ln(1 - lambda1)
		 */
		double lnAutocorrellationDeterminant = 0.0;
		for(int i = 1; i < m; i++) {
			lnAutocorrellationDeterminant += Math.log(eigenvalues[i]);
		}
		lnCovarianceDeterminant = Math.log(1 - lambda1) + lnAutocorrellationDeterminant;
		
		
		/*
		 * Now, obtain the inverse covariance matrix from the inverse
		 * autocorrelation matrix.
		 * 
		 * Sigma^-1 = S^-1 + S^-1 . (M^T.M) . S^-1 / (1 + lambda1)
		 * 
		 * The term S^-1 . (M^T.M) . S^-1 yields a symetric matrix
		 * 
		 * t_1 * t_1  ...  t_1 * t_n
		 * .               .
		 * .               .
		 * .               .
		 * t_n * t_1  ...  t_n * t_n
		 * 
		 * where
		 * 
		 * t_i = s_i1 * m_1 + ... + s_in * m_n
		 * 
		 * and s_ij is an autocorrelation element and m_i is the ith element
		 * of the mean.
		 */
		inverseCovariance = new double[dimension][dimension];
		for(int i = 0; i < dimension; i++) {
			inverseCovariance[i][i] = inverseAutocorrelation[i][i] + t[i] * t[i] / (1 + lambda1);
			for(int j = 0; j < i; j++) {
				inverseCovariance[i][j] = inverseCovariance[j][i] = 
					inverseAutocorrelation[i][j] + t[i] * t[j] / (1 + lambda1); 
			}
		}
		
	}

}
