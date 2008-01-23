package select.extract;

import java.util.HashMap;
import java.util.Map;

import select.FeatureSelection;
import util.ClassDescriptor;
import util.VectorSet;
import util.io.ExportVisitor;

/**
 * This is an implementation of a Linear Discriminant Analysis (LDA).
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class LinearDiscriminantAnalysis implements FeatureSelection {
	protected VectorSet orig;
	protected int resultDimension;
	protected double[][] ldaMatrix;
	
	
	protected LinearDiscriminantAnalysis() {
		// to prevent from external initialization
	}
	
	/**
	 * Initialize a new Linear Discriminant Analysis for given
	 * training data. This will create the projection matrix. Note,
	 * that this method does not do anything and should be overwritten
	 * by all subclasses.
	 * 
	 * @param orig The original training set.
	 */
	public void initialize(VectorSet orig) {
		
	}
	
	/**
	 * Map the original vector set to a vector set with lower dimension.
	 * The resulting vectors have dimension C-1, where C is the number of
	 * classes.
	 * @return return The mapped training set.
	 */
	public VectorSet getMappedData() {
		Map<double[], ClassDescriptor> newData = new HashMap<double[], ClassDescriptor>();
		Map<double[], ClassDescriptor> data = orig.getData();
		
		// y = A x
		for(double[] x: data.keySet()) {
			newData.put(mapVector(x), data.get(x));
		}

		String[] labels = new String[resultDimension];
		for(int i = 0; i < resultDimension; i++) {
			labels[i] = Integer.toString(i + 1);
		}

		return new VectorSet(newData, labels);
	}
	
	
	public double[] mapVector(double[] x) {
		int originalDimension = ldaMatrix[0].length;
		double[] y = new double[resultDimension];
		
		for(int i = 0; i < resultDimension; i++) {
			y[i] = 0.0;
			
			for(int j = 0; j < originalDimension; j++) {
				y[i] += ldaMatrix[i][j] * x[j];
			}
		}
		
		return y;
	}
	

	public void export(ExportVisitor exporter) {
		ExportVisitor.Parameters params = exporter.newParametersInstance();
		
		params.setParameter("lda", ldaMatrix);

		exporter.setSelection(this.getClass().getName(), params);
	}
	
	/**
	 * Initialize a new Linear Discriminant Analysis from some
	 * previously exported data.
	 * 
	 * @param params the map must at least contain the key "lda".
	 * @return
	 */
	public static LinearDiscriminantAnalysis newInstance(Map<String, Object> params) {
		LinearDiscriminantAnalysis lda = new LinearDiscriminantAnalysis();
		
		lda.ldaMatrix = (double[][])params.get("lda");
		lda.resultDimension = lda.ldaMatrix.length;
		
		return lda;
	}
}
