package select.subset;

import java.util.List;
import java.util.Map;

import Jama.Matrix;

import math.statistics.MaximumLikelihoodEstimation;
import math.statistics.Scatter;
import util.ClassDescriptor;
import util.VectorSet;

public class ChernoffCriterion implements CriterionFunction {
	private int dimension;
	private int nClasses;
	private double[] p;
	private double[][] mean;
	private double[][][] covariance;
	private int[] allFeatures;
	
	public double getCriterionValue() {
		return getCriterionValue(allFeatures);
	}

	public double getCriterionValue(int[] features) {
		double[][] mean1;
		double[][][] cov1;
		
		if(features.length < dimension) {
			mean1 = new double[nClasses][features.length];
			cov1 = new double[nClasses][features.length][features.length];
			
			for(int c = 0; c < nClasses; c++) {
				for(int i = 0; i < features.length; i++) {
					mean1[c][i] = mean[c][features[i]];
					cov1[c][i][i] = covariance[c][i][i];
					
					for(int j = i + 1; j < features.length; j++) {
						cov1[c][i][j] = cov1[c][j][i] = covariance[c][i][j];
					}
				}
			}
		} else {
			mean1 = mean;
			cov1 = covariance;
		}
		double[][] chernoffMatrix = Scatter.getChernoffMatrix(cov1, mean1, p);
		
		return (new Matrix(chernoffMatrix)).trace();
	}

	public void initialize(int dimension, VectorSet data) {
		this.dimension = dimension;
		this.nClasses = data.getClassDescriptors().size();
		int totalVectors = data.getData().size();
		Map<ClassDescriptor, List<double[]>> d = data.getInvertedData();
		
		p = new double[nClasses];           // class propabilities
		mean = new double[nClasses][];    // means
		covariance = new double[nClasses][][]; // covariance matrices

		int i = 0;
		for(ClassDescriptor c: d.keySet()) {
			p[i] = (double)d.get(c).size() / totalVectors;
			mean[i] = MaximumLikelihoodEstimation.getMean(d.get(c), dimension);
			covariance[i] = MaximumLikelihoodEstimation.getCovariance(d.get(c), mean[i]);
			i++;
		}
		
		allFeatures = new int[dimension];
		for(i = 0; i < dimension; i++) {
			allFeatures[i] = i;
		}
	}

}
