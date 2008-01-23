package select.extract;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

import math.statistics.MaximumLikelihoodEstimation;
import math.statistics.Scatter;
import util.ClassDescriptor;
import util.VectorSet;

/**
 * This is an extension to LDA which uses the Chernoff criterion.
 * The algorithm makes better use of discrimination information
 * provided in the covariance matrices. Moreover, it can create
 * any result dimension.
 * 
 * <br><br>
 * See M. Loog and P.W. Duin
 *     "Linear Dimensionality Reduction via a Heteroscedastic
 *      Extension of LDA: The Chernoff Criterion"
 *     in Transactions on Pattern Analysis and Machine Intelligence
 *     vol.26 no.6 pp.732--739
 *     IEEE 2004
 *     
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class ChernoffLinearDiscriminantAnalysis extends 
		LinearDiscriminantAnalysis {

	/**
	 * Create a new Chernoff LDA with a given result dimension.
	 *  
	 * @param resultDimension
	 */
	public ChernoffLinearDiscriminantAnalysis(int resultDimension) {
		this.resultDimension = resultDimension;
	}
	
	@Override
	public void initialize(VectorSet orig) {
		this.orig = orig;
		this.ldaMatrix = getLDAMatrix();
	}
	
	/**
	 * Initialize LDA. This will create the transformation
	 * matrix.
	 */
	private double[][] getLDAMatrix() {
		final int k = orig.getClassDescriptors().size();
		final int n = orig.getDimension();
		
		Map<ClassDescriptor, List<double[]>> data = orig.getInvertedData();
		
		/*
		 * Estimate the covariance matrix, the mean, and the a priori
		 * propability of each class.
		 */
		double[][][] cov = new double[k][][];
		double[][] mean = new double[k][];
		double[] p = new double[k];
		
		int cl = 0;
		int totalVectors = orig.getData().size();
		
		for(ClassDescriptor c: data.keySet()) {
			mean[cl] = MaximumLikelihoodEstimation.getMean(data.get(c), n);
			cov[cl] = MaximumLikelihoodEstimation.getCovariance(data.get(c), mean[cl]);
			p[cl] = (double)data.get(c).size() / totalVectors;
			cl++;
		}
		
		/*
		 * Now create the transformation matrix. We first create
		 * an n x n matrix A and apply the eigenvalue decomposition 
		 * to it. Then we look for the largest eigenvalues, and take
		 * the corresponding eigenvectors as the rows of the
		 * transformation matrix.
		 */
		double[][] a = Scatter.getChernoffMatrix(cov, mean, p);
		
		EigenvalueDecomposition eig = new EigenvalueDecomposition(new Matrix(a));
		double[] eigenvalues = eig.getRealEigenvalues();
		double[][] eigenvectors = eig.getV().transpose().getArray();
		
		// Create a matrix of the eigenvectors with the largest
		// eigenvalues.
		int[] maxEigenvalues = getMaxValues(eigenvalues, resultDimension);
		double[][] map = new double[resultDimension][];
		for(int j = 0; j < resultDimension; j++) {
			map[j] = eigenvectors[maxEigenvalues[j]];
		}
		
		return map;
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
