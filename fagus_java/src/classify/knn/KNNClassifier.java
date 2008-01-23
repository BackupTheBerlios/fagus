package classify.knn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import util.ClassDescriptor;
import util.VectorSet;
import util.io.ExportVisitor;
import classify.Classifier;

/**
 * This class provides a classifier using the k-Nearest-Neighbor
 * algorithm. A feature vector (a Double array) is classified by
 * calculating a distance to every known training vector. The 
 * distance is according to some metric - e.g. the euclidian distance.
 * In this process the k closest vectors are selected. Among this
 * selection, the most frequent class descriptor is returned.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class KNNClassifier implements Classifier {
	/** The default value for k */
	public static final int DEFAULT_K = 1;

	protected final int k;
	protected final Metrics metrics;
	protected ClassDescriptor[] classes;
	protected VectorSet trainingData;
	
	
	/**
	 * Create a new classifier for a set of class descriptors.
	 */
	public KNNClassifier() {
		this(DEFAULT_K);
	}
	
	/**
	 * Create a new classifier for a set of class descriptors
	 * and a custom value for k.
	 * @param k The value for k used within the classification
	 * algorithm.
	 */
	public KNNClassifier(int k) {
		this(k, new EuclidianDistance());
	}
	
	/**
	 * Create a new classifier for a set of class descriptors
	 * and a custom value for k.
	 * @param k The value for k used within the classification
	 * algorithm.
	 * @param metrics The metrics to use for calculating distances.
	 */	
	public KNNClassifier(int k, Metrics metrics) {
		this.k = k;
		this.metrics = metrics;
	}
	
	/**
	 * Do a classification for a feature vector f. This step
	 * requries, that a training set has already been set.
	 * @param f The feature vector to classify.
	 * @return A descriptor for the class that f was assigned to.
	 */
	public ClassDescriptor classify(double[] f) {
		/* 
		 * It's important to keep track of the actual distances
		 * while creating the selection. This is required to
		 * kick out the proper vector when inserting a new one.
		 */
		SortedMap<Double, ClassDescriptor> selection = new TreeMap<Double, ClassDescriptor>();
		
		/*
		 * Loop across the training set an calculate the distance
		 * to each of its elements.
		 */
		for(Map.Entry<double[], ClassDescriptor> trainingEntry: trainingData.getData().entrySet()) {
			double result = metrics.getDistance(f, trainingEntry.getKey());
			
			if(selection.size() < k) {
				// selection is not full so far
				selection.put(new Double(result), trainingEntry.getValue());
			} else if(result < selection.lastKey().doubleValue()) {
				// selection is full but contains at least one
				// vector with a greater distance
				selection.remove(selection.lastKey());
				selection.put(new Double(result), trainingEntry.getValue());
			}
		}
		
		return getHighestFrequency(selection);
	}
	
	/**
	 * Set the training data for this classifier.
	 * @param trainingData The training data to use.
	 */
	public void train(VectorSet trainingSet) {
		Set<ClassDescriptor> cs = trainingSet.getClassDescriptors();
		classes = new ClassDescriptor[cs.size()];
		classes = cs.toArray(classes);
		
		this.trainingData = trainingSet;
	}
	
	/**
	 * Discard training data. I.e. reset the classifier.
	 */
	public void clearTrainingData() {
		this.trainingData = null;
	}
	

	public void export(ExportVisitor visitor) {
		ExportVisitor.Parameters params = visitor.newParametersInstance();
		params.setParameter("k", Integer.toString(k));
		params.setParameter("metric", metrics.getClass().getName());
		
		visitor.setModel(this.getClass().getName(), params);
		
		Map<ClassDescriptor, List<double[]>> data = trainingData.getInvertedData();
		
		for(ClassDescriptor c: data.keySet()) {
			double[][] a = new double[data.get(c).size()][];
			a = data.get(c).toArray(a);
			
			params = visitor.newParametersInstance();
			params.setParameter("vectors", a);
			
			visitor.addClass(c.toString(), params);
		}
	}
	
	/**
	 * Reconstruct a kNN classifier from serialized data.
	 * 
	 * @param model the model parameters.
	 * @param classes the class parameters.
	 * @return
	 */
	public static KNNClassifier newInstance(Map<String, Object> model, Map<ClassDescriptor, Map<String, Object>> classes) {
		KNNClassifier classifier;
		int k = Integer.parseInt((String)model.get("k"));
		
		try {
			Class clazz = Class.forName((String)model.get("metric"));
			Metrics m = (Metrics)clazz.newInstance();
			classifier = new KNNClassifier(k, m);
		} catch(Exception e) {
			System.err.println("Cannot initialize distance metric, resorting to default metric: "
					+ e.getMessage());
			classifier = new KNNClassifier(k);
		}
		
		int dimension = 0;
		Map<double[], ClassDescriptor> data = new HashMap<double[], ClassDescriptor>();
		
		for(ClassDescriptor c: classes.keySet()) {
			double[][] a = (double[][])classes.get(c).get("vectors");
			dimension = a[0].length;
			
			for(int i = 0; i < a.length; i++) {
				data.put(a[i], c);
			}
		}

		// make dummy labels
		String[] labels = new String[dimension];
		for(int i = 0; i < dimension; i++) {
			labels[i] = Integer.toString(i);
		}

		classifier.train(new VectorSet(data, labels));
		
		return classifier;
	}

	
	/*
	 * Get the class descriptor with the most occurrences within
	 * the selections. If two or more elements have the same number
	 * of occurrences, a decision based on the sum of differences
	 * is made.
	 */
	private ClassDescriptor getHighestFrequency(Map<Double, ClassDescriptor> selection) {
		int nclasses = classes.length;
		int[] frequencies = new int[nclasses];
		double[] difference = new double[nclasses];

		// TODO: improve running time 
		// currently: O(c*k) where c is the number of classes

		//init
		for(int i = 0; i < frequencies.length; i++) {
			frequencies[i] = 0;
			difference[i] = 0.0;
		}
		
		//for every class count the occurrence of this class
		for(int i = 0; i < nclasses; i++) {
			for(Map.Entry<Double, ClassDescriptor> entry: selection.entrySet()) {
				if(entry.getValue().equals(classes[i])) {
					frequencies[i]++;
					difference[i] += entry.getKey().doubleValue(); 
				}
			}
		}
		
		int max = 0;
		
		for(int i = 1; i < nclasses; i++) {
			if(frequencies[i] > frequencies[max]) {
				max = i;
			} else if( (frequencies[i] == frequencies[max]) && 
					   (difference[i] < difference[max]) ) {
				max = i;
			}
		}
		
		return classes[max];
	}
}
