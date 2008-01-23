package math.statistics;

import math.MatrixManipulation;
import Jama.Matrix;


/**
 * This class holds static methods for computing scatter matrices.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class Scatter {
	
	/**
	 * Get the within-class scatter. This is the sum of all covariance
	 * matrices multiplied with their a-priori probability.
	 * 
	 * @param covariance an array of covariance matrices.
	 * @param p an array of a-priori probabilities.
	 * @return the within-class scatter.
	 */
	public static double[][] getWithinClassScatter(double[][][] covariance, double[] p) {
		final int n = covariance[0].length; // dimension of vector space
		final int m = covariance.length;    // number of classes
		double[][] result = new double[n][n];
		
		for(int i = 0; i < n; i++) {
			result[i][i] = 0.0;
			for(int k = 0; k < m; k++) {
				result[i][i] += p[k] * covariance[k][i][i];
			}
			
			for(int j = i + 1; j < n; j++) {
				result[i][j] = result[j][i] = 0.0;
				
				for(int k = 0; k < m; k++) {
					double u = p[k] * covariance[k][i][j];
					result[i][j] += u;
					result[j][i] += u;
				}
			}
		}
		
		return result;
	}
	
	
	/**
	 * The between-class scatter is the average deviation
	 * of class means from the global mean.
	 * 
	 * @param mean an array of mean vectors.
	 * @param p an array of a-priori probabilities.
	 * @return the between-class scatter matrix.
	 */
	public static double[][] getBetweenClassScatter(double[][] mean, double[] p) {
		final int n = mean[0].length; // dimension of vector space
		final int m = mean.length;    // number of classes
		double[] globalMean = new double[n];
		double[][] result = new double[n][n];
		
		// average of all mean vectors
		for(int k = 0; k < m; k++) {
			for(int i = 0; i < n; i++) {
				globalMean[i] += p[k] * mean[k][i];
			}
		}
		
		for(int k = 0; k < m; k++) {
			for(int i = 0; i < n; i++) {
				result[i][i] += p[k] * (mean[k][i] - globalMean[i]) * (mean[k][i] - globalMean[i]);
				
				for(int j = i + 1; j < n; j++) {
					double u = p[k] * (mean[k][i] - globalMean[i]) * (mean[k][j] - globalMean[j]);
					result[i][j] += u;
					result[j][i] += u;
				}
			}
		}
		
		return result;
	}
	

	public static double[][] getChernoffMatrix(double[][][] cov, double[][] mean, double[] p) {
		if(cov.length == 2) {
			return getChernoffMatrix2Classes(cov, mean, p);
		} else {
			return getChernoffMatrixNClasses(cov, mean, p);
		}
	}
	
	public static double[][] getCoClassScatter(double[][][] covariance, double[] p, int c1, int c2) {
		final int n = covariance[0].length; // dimension of vector space
		double[][] result = new double[n][n];

		double u = p[c1] + p[c2];
		
		for(int i = 0; i < n; i++) {
			result[i][i] = (p[c1] * covariance[c1][i][i] + p[c2] * covariance[c2][i][i])/u; 

			for(int j = i + 1; j < n; j++) {
				result[i][j] = result[j][i] = 
					(p[c1] * covariance[c1][i][j] + p[c2] * covariance[c2][i][j])/u;
			}
		}
		
		return result;
	}

	/*
	 * Get the matrix of (m_1 - m_2) . (m_1 - m_2)^T
	 */
	private static double[][] meanMatrix(double[][] mean, int c1, int c2) {
		final int n = mean[0].length;
		double[][] m = new double[n][n];
		
		for(int i = 0; i < n; i++) {
			m[i][i] = (mean[c1][i] - mean[c2][i]) * (mean[c1][i] - mean[c2][i]);
			
			for(int j = i + 1; j < n; j++) {
				m[i][j] = m[j][i] = (mean[c1][i] - mean[c2][i]) * (mean[c1][j] - mean[c2][j]);
			}
		}
		
		return m;
	}
	
	
	private static double[][] getChernoffMatrix2Classes(double[][][] cov, double[][] mean, double[] p) {
		final int n = cov[0].length; // dimension of vector space
		
		/*
		 * In the 2 classes case, the matrix 
		 * 
		 * sW^(-1/2) . s_ij . sW^(-1/2) = I
		 * 
		 * Therefore, we need fewer matrix multiplications than in
		 * the six classes case.
		 */
		double[][] sW = getWithinClassScatter(cov, p);
		double[][] sWInv = (new Matrix(sW)).inverse().getArray();
		double[][] sWInvSqrt = MatrixManipulation.sqrtMatrix(sWInv);
		
		double[][] s1 = MatrixManipulation.logMatrix(MatrixManipulation.triSymmetricMult(sWInvSqrt, cov[0]));
		double[][] s2 = MatrixManipulation.logMatrix(MatrixManipulation.triSymmetricMult(sWInvSqrt, cov[1]));
		
		double[][] s = MatrixManipulation.triSymmetricMult(sWInvSqrt, meanMatrix(mean, 0, 1));
		
		for(int i = 0; i < n; i++) {
			s[i][i] -=  s1[i][i] / p[1] + s2[i][i] / p[0];
			
			for(int j = i + 1; j < n; j++) {
				double u = s1[i][j] / p[1] + s2[i][j] / p[0];
				s[i][j] -= u;
				s[j][i] -= u;
			}
		}
		
		double[][] b = MatrixManipulation.triSymmetricMult(MatrixManipulation.sqrtMatrix(sW), s);
		
		double[][] result = new double[n][n];
		
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				double u = 0.0;
				
				for(int k = 0; k < n; k++) {
					u += sWInv[i][k] * b[j][k];
				}
				
				result[i][j] = u * p[0] * p[1]; 
			}
		}
		
		return result;
	}
	
	
	private static double[][] getChernoffMatrixNClasses(double[][][] cov, double[][] mean, double[] p) {
		final int n = cov[0].length; // dimension of vector space
		final int k = cov.length;    // number of classes
		
		double[][] sW = getWithinClassScatter(cov, p);
		double[][] sWInv = (new Matrix(sW)).inverse().getArray();
		double[][] sWInvSqrt = MatrixManipulation.sqrtMatrix(sWInv);
		double[][] sWSqrt = MatrixManipulation.sqrtMatrix(sW);
		
		double[][] result = new double[n][n];
		
		for(int c1 = 0; c1 < k - 1; c1++) {
			for(int c2 = c1 + 1; c2 < k; c2++) {
				double[][] co = MatrixManipulation.triSymmetricMult(sWInvSqrt, getCoClassScatter(cov, p, c1, c2));
				double[][] coInvSqrt = MatrixManipulation.sqrtMatrix((new Matrix(co)).inverse().getArray());
				
				double[][] s1 = MatrixManipulation.logMatrix(MatrixManipulation.triSymmetricMult(sWInvSqrt, cov[c1]));
				double[][] s2 = MatrixManipulation.logMatrix(MatrixManipulation.triSymmetricMult(sWInvSqrt, cov[c2]));
				double[][] s3 = MatrixManipulation.logMatrix(co);
				
				double[][] r1 = MatrixManipulation.triSymmetricMult(sWInvSqrt, meanMatrix(mean, c1, c2));
				double[][] r = MatrixManipulation.triSymmetricMult(coInvSqrt, r1);
				
				for(int i = 0; i < n; i++) {
					r[i][i] += (s3[i][i] * (p[c1] + p[c2]) - s1[i][i] * p[c1] - s2[i][i] * p[c2]) / (p[c1] * p[c2]);
					
					for(int j = i + 1; j < n; j++) {
						double u = (s3[i][j] * (p[c1] + p[c2]) - s1[i][j] * p[c1] - s2[i][j] * p[c2]) / (p[c1] * p[c2]);
						r[i][j] += u;
						r[j][i] += u;
					}
				}
				
				double[][] b = MatrixManipulation.triSymmetricMult(sWSqrt, r);
				
				for(int i = 0; i < n; i++) {
					for(int j = 0; j < n; j++) {
						double u = 0.0;
						
						for(int l = 0; l < n; l++) {
							u += sWInv[i][l] * b[j][l];
						}
						
						result[i][j] += u * p[c1] * p[c2]; 
					}
				}
			}
		}
		
		return result;
	}

}
