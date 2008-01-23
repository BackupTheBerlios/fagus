package select.extract;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import math.statistics.MaximumLikelihoodEstimation;
import math.statistics.Scatter;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import util.ClassDescriptor;
import util.VectorSet;

/**
 * The Fisher Linear Discriminant Analysis (LDA) takes the within 
 * (S<sub>W</sub>) and between class scatter (S<sub>B</sub>) and 
 * calculates a matrix A of C - 1 eigenvectors of 
 * <pre>
 * S<sub>W</sub><sup>-1</sup> S<sub>B</sub>
 * </pre>
 * with the highest eigenvalues (C is the number of distinct class 
 * descriptors). The resulting matrix is used to map the original
 * feature vectors x<sub>1</sub> ... x<sub>n</sub>
 * <pre>
 * y = A<sup>T</sup> x
 * </pre>
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class FisherLinearDiscriminantAnalysis extends
		LinearDiscriminantAnalysis {

	public FisherLinearDiscriminantAnalysis() {
	}
	
	
	@Override
	public void initialize(VectorSet orig) {
		this.orig = orig;
		this.resultDimension = orig.getClassDescriptors().size() - 1;
		this.ldaMatrix = getLDAMatrix();
	}
	
	
	private double[][] getLDAMatrix() {
		int dimension = orig.getDimension();
		int nClasses = orig.getClassDescriptors().size();
		int totalVectors = orig.getData().size();
		Map<ClassDescriptor, List<double[]>> d = orig.getInvertedData();
		
		double[] p = new double[nClasses];           // class propabilities
		double[][] mean = new double[nClasses][];    // means
		double[][][] cov = new double[nClasses][][]; // covariance matrices

		int i = 0;
		for(ClassDescriptor c: d.keySet()) {
			p[i] = (double)d.get(c).size() / totalVectors;
			mean[i] = MaximumLikelihoodEstimation.getMean(d.get(c), orig.getDimension());
			cov[i] = MaximumLikelihoodEstimation.getCovariance(d.get(c), mean[i]);
			i++;
		}
		
		Matrix sw = new Matrix(Scatter.getWithinClassScatter(cov, p));
		Matrix sb = new Matrix(Scatter.getBetweenClassScatter(mean, p));

		/*
		 * Now compute the base matrix for the LDA matrix. It is defined by
		 * 
		 * A = S_w^-1 S_b
		 * 
		 * We use an alternative computation proposed by Matrinez and Zhu,
		 * which does not require a matrix inversion and is thus applicable
		 * to singluar problems.
		 * 
		 * Let lambdaB_i and lambdaW_j be the i-th eigenvalue of the
		 * between-class scatter and the j-th eigenvalue of the within-class
		 * scatter, respectively, and let vB_i and vW_j be the respective 
		 * eigenvectors. Then the above matrix can be computed as
		 * 
		 * sum_i=1^q sum_j=1^p  ( lambdaB_i / lambdaW_j ) * (vW_j^T vB_i) * (vW_j vB_i^T)
		 * 
		 * where q and p are the ranks of the respective scatter matrices.
		 * 
		 * See Theorem 4 of A.M. Martinez and M. Zhu
		 *     "Where are Linear Feature Extraction Methods Applicable?"
		 *     IEEE Transactions on Pattern Analysis and Machine Intelligence
		 *     Vol 27. No 12. pp 1934-1944
		 *     IEEE, 2005
		 */
		/*
		 * TODO: This code is mostly copy-pasted from 
		 *       select.subset.FisherClassSeparabilityCriterion. Can we extract
		 *       this code to some other location?
		 */
		Matrix a;// = sw.inverse().times(sb);
		EigenvalueDecomposition ew = new EigenvalueDecomposition(sw);
		EigenvalueDecomposition eb = new EigenvalueDecomposition(sb);
		
		double[] eigenvaluesW = ew.getRealEigenvalues();
		double[] eigenvaluesB = eb.getRealEigenvalues();
		
		/*
		 * Note, that the eigenvectors are the columns (not rows) of
		 * Jama's V matrix.
		 */
		double[][] eigenvectorsW = ew.getV().getArray();
		double[][] eigenvectorsB = eb.getV().getArray();
		
		/*
		 * Let C be the number of classes. Then S_b has only C - 1 non-zero
		 * eigenvalues.
		 */
		int[] eigenvaluesBIndices = getMaxValues(eigenvaluesB, nClasses - 1);
		
		double[][] tmp = new double[dimension][dimension];
		for(i = 0; i < eigenvaluesBIndices.length; i++) {
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
					
					for(int k = 0; k < dimension; k++) {
						v += eigenvectorsW[k][j] * eigenvectorsB[k][eigenvaluesBIndices[i]];
					}
					
					v = v * eigenvaluesB[eigenvaluesBIndices[i]] / eigenvaluesW[j];
					
					for(int k = 0; k < dimension; k++) {
						for(int l = 0; l < dimension; l++) {
							tmp[k][l] += v * eigenvectorsW[k][j] * eigenvectorsB[l][eigenvaluesBIndices[i]];
						}
					}
				}
			}
		}
		a = new Matrix(tmp);
		
		EigenvalueDecomposition e = new EigenvalueDecomposition(a);
		double[] eigenvalues = e.getRealEigenvalues();
		double[][] eigenvectors = e.getV().transpose().getArray();
		
		// Create a matrix of the eigenvectors with the largest
		// eigenvalues.
		int[] maxEigenvalues = getMaxValues(eigenvalues, resultDimension);
		double[][] lda = new double[resultDimension][];
		for(i = 0; i < resultDimension; i++) {
			lda[i] = eigenvectors[maxEigenvalues[i]];
		}

		return lda;
	}

	
	private int[] getMaxValues(double[] values, int n) {
		/*
		 * Remember maximal values and indices of maximal values.
		 * If a new maximal value is found, all smaller values 
		 * (and the corresponding indices) must be shifted to the
		 * right.
		 */
		List<Double> maxValues = new LinkedList<Double>();
		List<Integer> maxIndices = new LinkedList<Integer>();
		int[] result = new int[n];
		
		for(int i = 0; i < n; i++) {
			maxValues.add(Double.NEGATIVE_INFINITY);
		}
		
		for(int i = 0; i < values.length; i++) {
			for(int j = 0; j < n; j++) {
				if(maxValues.get(j) < values[i]) {
					maxValues.add(j, values[i]);
					maxIndices.add(j, i);
					break;
				}
			}
		}
		
		for(int i = 0; i < n; i++) {
			result[i] = maxIndices.get(i);
		}
		
		return result;
	}
}
