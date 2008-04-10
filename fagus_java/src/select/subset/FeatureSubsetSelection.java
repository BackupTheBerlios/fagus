package select.subset;

import java.util.HashMap;
import java.util.Map;

import select.FeatureSelection;
import util.ClassDescriptor;
import util.VectorSet;
import util.io.Export;
import util.io.ExportVisitor;
import util.io.Import;
import util.io.ModelType;

/**
 * This class is used to integrate feature subset selection with
 * the classification and cross-validation process.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class FeatureSubsetSelection implements FeatureSelection {
	private int[] indices;
	private SelectionAlgorithm algorithm;
	private int dropNFeatures;
	private CriterionFunction criterion;
	private VectorSet trainingData;
	
	public FeatureSubsetSelection(SelectionAlgorithm alg, CriterionFunction f, int dropNFeatures) {
		this.algorithm = alg;
		this.criterion = f;
		this.dropNFeatures = dropNFeatures;
	}
	
	/**
	 * Create a new feature subset selection with a given subset.
	 * 
	 * @param indices the indices of the features select (i.e. to keep).
	 */
	protected FeatureSubsetSelection(int[] indices) {
		this.indices = indices;
	}
	
	/**
	 * Search for the best feature subset with respect to the
	 * criterion function and the training data.
	 */
	public void initialize(VectorSet trainingData) {
		this.trainingData = trainingData;
		
		algorithm.run(trainingData, criterion, dropNFeatures);
		indices = algorithm.getFeatureVector();
	}
	
	public VectorSet getMappedData() {
		Map<double[], ClassDescriptor> data = new HashMap<double[], ClassDescriptor>();
		String[] labels = new String[indices.length];
		
		for(int i = 0; i < indices.length; i++) {
			labels[i] = trainingData.getFeatureLabel(indices[i]);
		}
		
		Map<double[], ClassDescriptor> orig = trainingData.getData();
		
		for(double[] v: orig.keySet()) {
			double[] v1 = mapVector(v);
			data.put(v1, orig.get(v));
		}
		
		return new VectorSet(data, labels);
	}
	
	/**
	 * Select the relevant features from a vector.
	 */
	public double[] mapVector(double[] original) {
		double[] result = new double[indices.length];
		
		for(int i = 0; i < result.length; i++) {
			result[i] = original[indices[i]];
		}
		
		return result;
	}
	
	@Export(ModelType.FEATURE_SELECTION)
	public void export(ExportVisitor exporter) {
		ExportVisitor.Parameters params = exporter.newParametersInstance();
		params.setParameter("subset", indices);
		exporter.setSelection(this.getClass().getName(), params);
	}

	/**
	 * This is used for recostruction of an exported feature
	 * subset selection instance.
	 * 
	 * @param params
	 * @return
	 */
	@Import(ModelType.FEATURE_SELECTION)
	public static FeatureSubsetSelection newInstance(Map<String, Object> params) {
		int[] indices = (int[])params.get("subset");
		
		return new FeatureSubsetSelection(indices);
	}

}
