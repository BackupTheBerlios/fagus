package math;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class MatrixManipulation {
	/**
	 * Calculate sqrt(A). This means that an eigenvalue decomposition
	 * is performed on it, yielding the eigenvalues lambda_1 .. lambda_n and
	 * the eigenvector matrix Phi. The result is
	 * 
	 * Phi . diag(sqrt(lambda_1) ... sqrt(lambda_n)) . Phi<sup>-1</sup>
	 * 
	 * @param a a symmetric matrix.
	 * @return the square root of the matrix.
	 */
	public static double[][] sqrtMatrix(double[][] a) {
		EigenvalueDecomposition eig = new EigenvalueDecomposition(new Matrix(a));
		double[][] v = eig.getV().getArray();
		final int n = a.length;
		
		double[][] diagMatrix = eig.getD().getArray();
		for(int i = 0; i < n; i++) {
			diagMatrix[i][i] = Math.sqrt(diagMatrix[i][i]);
		}
		
		/*
		 * Note, that the eigenvectormatrix V is orthogonal, that is
		 * V^T = V^-1
		 */
		return triSymmetricMult(v, diagMatrix);
	}
	
	
	/**
	 * Calculate log(A). This means that an eigenvalue decomposition
	 * is performed on it, yielding the eigenvalues lambda_1 .. lambda_n and
	 * the eigenvector matrix Phi. The result is
	 * 
	 * Phi . diag(log(lambda_1) ... log(lambda_n)) . Phi<sup>-1</sup>
	 * 
	 * @param a a symmetric matrix.
	 * @return log(A)
	 */
	public static double[][] logMatrix(double[][] a) {
		EigenvalueDecomposition eig = new EigenvalueDecomposition(new Matrix(a));
		double[][] v = eig.getV().getArray();
		final int n = a.length;
		
		double[][] diagMatrix = eig.getD().getArray();
		for(int i = 0; i < n; i++) {
			diagMatrix[i][i] = Math.log(diagMatrix[i][i]);
		}
		
		/*
		 * Note, that the eigenvectormatrix V is orthogonal, that is
		 * V^T = V^-1
		 */
		return triSymmetricMult(v, diagMatrix);
	}

	
	/**
	 * Calculate A . B . A^T where B is symmetric matrix and A is
	 * a square matrix of dimension n x n. The resulting matrix is 
	 * again symmetric.
	 * 
	 * @param a a symmetric matrix.
	 * @param b a symmetric matrix.
	 * @return the result of A . B . A^T
	 */
	public static double[][] triSymmetricMult(double[][] a, double[][] b) {
		final int n = a.length;
		double[][] tmp = new double[n][n];
		double[][] result = new double[n][n];
		
		for(int k = 0; k < n; k++) {
			for(int l = 0; l < n; l++) {
				tmp[k][l] = 0.0;
				
				for(int i = 0; i < n; i++) {
					tmp[k][l] += a[l][i] * b[k][i];
				}
			}
		}
		
		for(int k = 0; k < n; k++) {
			result[k][k] = 0.0;
			
			for(int i = 0; i < n; i++) {
				result[k][k] += a[k][i] * tmp[i][k];
			}
			
			for(int l = k + 1; l < n; l++) {
				double u = 0.0;
				
				for(int i = 0; i < n; i++) {
					u += a[k][i] * tmp[i][l];
				}
				
				result[k][l] = result[l][k] = u;
			}
		}
		
		return result;

//		final int n = a.length;
//		double[][] result = new double[n][n];
//	
//		for(int k = 0; k < n; k++) {
//			double diag = 0.0;
//			double offDiag = 0.0;
//			
//			/*
//			 * The diagonal elements of the resulting matrix are given by.
//			 * 
//			 * sum_(i=1)^n a_ki * a_ki * b_ii + 
//			 *      + 2 * sum_(i=1)^(n-1) sum_(j=i+1)^n a_ki * a_kj * b_ij
//			 */
//			for(int i = 0; i < n - 1; i++) {
//				diag += a[k][i] * a[k][i] * b[i][i];
//				
//				for(int j = i + 1; j < n; j++) {
//					offDiag += a[k][i] * a[k][j] * b[i][j];
//				}
//			}
//			
//			result[k][k] = diag + a[k][n-1] * a[k][n-1] * b[n-1][n-1] + 2 * offDiag;
//
//			/*
//			 * The off diagonal elements are given by
//			 * 
//			 * sum_(i=1)^n a_ki * a_li * b_ii +
//			 *      + sum_(i=1)^(n-1) sum_(j=i+1)^n (a_ki * a_lj + a_kj * a_li) * b_ij
//			 */
//			for(int l = k + 1; l < n; l++) {
//				diag = 0.0;
//				offDiag = 0.0;
//
//				for(int i = 0; i < n - 1; i++) {
//					diag += a[k][i] * a[l][i] * b[i][i];
//					
//					for(int j = i + 1; j < n; j++) {
//						offDiag += (a[k][i]*a[l][j] + a[k][j]*a[l][i]) * b[i][j];
//					}
//				}
//				
//				result[k][l] = result[l][k] = diag + a[k][n-1] * a[l][n-1] * b[n-1][n-1] + offDiag;
//			}
//		}
//		
//		return result;
	}
}
