package classify.svm;

import java.util.Map;
import java.util.TreeMap;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import util.ClassDescriptor;
import util.VectorSet;
import util.io.ExportVisitor;
import classify.Classifier;

/**
 * This Classifier uses the LibSVM Java bindings. The classifier
 * uses an RBF (Gaussian) kernel.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class SupportVectorClassifier implements Classifier {
	private svm_model model;
	private svm_parameter param;
	private Map<Double, ClassDescriptor> classes;
	private int[] indices;
	
	/**
	 * Create a new SVM Classifier with default parameters.
	 */
	public SupportVectorClassifier() {
		this(1.0, 0);
	}
	
	
	/**
	 * Create a new SVM Classifiers with given parameters.
	 * 
	 * @param c
	 * @param gamma
	 */
	public SupportVectorClassifier(double c, double gamma) {
		param = new svm_parameter();
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		param.coef0 = 0;
		param.cache_size = 100;
		param.gamma = gamma;
		param.C = c;
		param.eps = 1e-3;
		param.shrinking = 1;
		param.probability = 0;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];
	}
	
	
	public ClassDescriptor classify(double[] f) {
		double result;
		
		svm_node[] v = new svm_node[f.length];
		
		for(int i = 0; i < f.length; i++) {
			v[i] = new svm_node();
			v[i].index = indices[i];
			v[i].value = f[i];
		}
		
		result = svm.svm_predict(model, v);
		
		return classes.get(result);
	}

	
	public void clearTrainingData() {
	}

	
	public void train(VectorSet trainingSet) {
		final Map<double[], ClassDescriptor> data = trainingSet.getData();
		final int dimension = trainingSet.getDimension();
		final int n = data.size();
		
		classes = new TreeMap<Double, ClassDescriptor>();
		for(ClassDescriptor d: trainingSet.getClassDescriptors()) {
			classes.put(new Double(d.toString()), d);
		}
		
		indices = new int[dimension];
		for(int j = 0; j < dimension; j++) {
			indices[j] = Integer.parseInt(trainingSet.getFeatureLabel(j));
		}
		
		svm_problem problem = new svm_problem();
		
		problem.l = n;
		problem.x = new svm_node[n][dimension];
		problem.y = new double[n];
		
		int i = 0;
		for(double[] v: data.keySet()) {
			for(int j = 0; j < dimension; j++) {
				problem.x[i][j] = new svm_node();
				problem.x[i][j].index = indices[j];
				problem.x[i][j].value = v[j];
			}
			
			problem.y[i] = Double.parseDouble(data.get(v).toString());
			i++;
		}
		
		if(param.gamma == 0) {
			param.gamma = 1.0 / dimension;
		}
		
		model = svm.svm_train(problem, param);
	}
	
	
	public void export(ExportVisitor visitor) {
		ExportVisitor.Parameters params = visitor.newParametersInstance();
		params.setParameter("c", Double.toString(param.C));
		params.setParameter("gamma", Double.toString(param.gamma));

		/*
		 * FIXME: LibSVM has a weird design that prevents me from accessing
		 *        the contents of the classification model. All the model's
		 *        fields are package-scoped and cannot be exported from a
		 *        class in this package. Arrrgh ...
		 */
		visitor.setModel(this.getClass().getName(), params);
	}
	
}
