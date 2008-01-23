package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A vector set is a map from vectors to their corresponding class
 * descriptor. This class provides further frequently needed 
 * functionalities.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class VectorSet {
	private final Map<double[], ClassDescriptor> data;
	private final int dimension;
	private final String[] labels;
	/*
	 * The inverse mapping and the classes set are
	 * created on demand.
	 */
	private Map<ClassDescriptor, List<double[]>> inverseData = null;
	private Set<ClassDescriptor> classes = null;
	
	/**
	 * Create a new vector set using a given set of preclassified vectors
	 * of a given dimension.
	 * 
	 * @param data The preclassified vectors.
	 * @param labels The labels of the features.
	 */
	public VectorSet(Map<double[], ClassDescriptor> data, String[] labels) {
		this.data = data;
		this.dimension = labels.length;
		this.labels = labels;
	}
	
	/**
	 * Remove a vector from this set.
	 * 
	 * @param vector the vector set to be removed.
	 * @return the class descriptor of the removed vector.
	 */
	public ClassDescriptor remove(double[] vector) {
		ClassDescriptor d = data.remove(vector);
		
		// clear inverted data and classes
		if(inverseData != null) {
			inverseData.get(d).remove(vector);
			
			if(inverseData.get(d).isEmpty() && classes != null) {
				classes.remove(d);
			}
		}
		
		return d;
	}
	
	/**
	 * Add a vector to this set.
	 * 
	 * @param vector the vector to be added.
	 */
	public void add(double[] vector, ClassDescriptor d) {
		data.put(vector, d);
		
		if( (classes != null) && (!classes.contains(d)) ) {
			classes.add(d);
		}
		
		if(inverseData != null) {
			inverseData.get(d).add(vector);
		}
	}
	
	/**
	 * Get the dimension of the vectors in this set.
	 * 
	 * @return
	 */
	public int getDimension() {
		return dimension;
	}
	
	/**
	 * Get a map of vectors to their corresponding class descriptors.
	 * 
	 * @return
	 */
	public Map<double[], ClassDescriptor> getData() {
		return data;
	}
	
	/**
	 * This method indicates, wheater there are only two distinct classes
	 * or more.
	 * 
	 * @return True, if the set contains only two distinct classes.
	 */
	public boolean isTwoClassProblem() {
		return (getClassDescriptors().size() == 2);
	}
	
	/**
	 * Get all class descriptors that occur in this set.
	 * 
	 * @return
	 */
	public Set<ClassDescriptor> getClassDescriptors() {
		if(classes == null) {
			if(inverseData != null) {
				classes = inverseData.keySet();
			} else {
				classes = new HashSet<ClassDescriptor>(data.values());
			}
		}
		
		return classes;
	}
	
	/**
	 * Get the label of a feature.
	 * 
	 * @param index the index of the feature in the vector.
	 * @return
	 */
	public String getFeatureLabel(int index) {
		return labels[index];
	}
	
	/**
	 * Get all feature labels.
	 * 
	 * @return
	 */
	public String[] getFeatureLables() {
		return labels;
	}
	
	/**
	 * Get a map of class descriptors to a list of all vectors in the set,
	 * that are classified to this descriptor.
	 * 
	 * @return
	 */
	public Map<ClassDescriptor, List<double[]>> getInvertedData() {
		if(inverseData == null) {
			inverseData = new HashMap<ClassDescriptor, List<double[]>>();
			
			for(double[] v: data.keySet()) {
				ClassDescriptor c = data.get(v);
				
				if(!inverseData.containsKey(c)) {
					inverseData.put(c, new ArrayList<double[]>());
				}
				
				inverseData.get(c).add(v);
			}
		}
		
		return inverseData;
	}

	/**
	 * Determine, whether the problem is ill posed. That is, if one
	 * class contains fewer training samples than the number of 
	 * features.
	 * 
	 * @return true if the problem is ill posed.
	 */
	public boolean isIllPosed() {
		Map<ClassDescriptor, List<double[]>> data = getInvertedData();
		
		int minSize = Integer.MAX_VALUE;
		
		for(ClassDescriptor c: data.keySet()) {
			int n = data.get(c).size();
			
			if(n < minSize) {
				minSize = n;
			}
		}
		
		return (minSize < dimension);
	}
}
