package select.subset.greedy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Observable;

import select.subset.CriterionFunction;
import select.subset.SelectionAlgorithm;
import util.VectorSet;

/**
 * This class serves as an abstraction for all nested subset
 * algorithms. Nested subset methods subsequently change some
 * candidate solutions by adding new features or removing old
 * ones. Every time such an add or remove operation is perfomed,
 * some other parts of the system can be notified. This can help
 * to adjust the feature search space manually, to output the
 * candidate solution, or to keep track of the search progress.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public abstract class NestedSubsetAlgorithm extends Observable implements
		SelectionAlgorithm {

	protected FeatureSpace featureSpace;
	protected Collection<Integer> candidate;
	protected double candidateValue;
	
	protected SelectionComparator selectionComparator;
	
	public int[] getFeatureVector() {
		int[] features = new int[candidate.size()];
		int i = 0;
		for(Integer f: candidate) {
			features[i++] = f;
		}
		
		Arrays.sort(features);
		
		return features;
	}
	
	public double getBestValue() {
		return candidateValue;
	}

	/**
	 * Set the feature space manually. This can be used for
	 * manual adjustment of the feature space on every add or
	 * remove operation.
	 * 
	 * @param available The feature space to be used.
	 */
	public void setFeatureSpace(FeatureSpace featureSpace) {
		this.featureSpace = featureSpace;
		addObserver(featureSpace);
	}
	
	/**
	 * Set a custom SelectionComparator.
	 * 
	 * @param selectionComparator
	 */
	public void setSelectionComparator(SelectionComparator selectionComparator) {
		this.selectionComparator = selectionComparator;
	}
	
	public void run(VectorSet vectors, CriterionFunction f, int dropNrFeatures) {
		final int n = vectors.getDimension();
		final int targetSize = n - dropNrFeatures;

		f.initialize(n, vectors);

		if(selectionComparator == null) {
			selectionComparator = new DefaultSelectionComparator();
		}
		
		if(featureSpace == null) {
			setFeatureSpace(createDefaultFeatureSpace(vectors.getDimension()));
		}
		
		doRun(f, featureSpace, n, targetSize);
	}

	/**
	 * Get the current candidate solution;
	 * 
	 * @return
	 */
	public Iterable<Integer> getCandidate() {
		return candidate;
	}
	
	public double getCandidateValue() {
		return candidateValue;
	}
	
	/**
	 * Set the candidate subset to start with.
	 * 
	 * @param features
	 */
	public void setInitialCandidate(Collection<Integer> features) {
		candidate = features;

		/*
		 * Remove all candidate elements from the feature space.
		 */
		for(Integer f: candidate) {
			featureSpace.update(this, new Operation(false, f));
		}
	}
	
	/**
	 * Remove some feature from the feature space.
	 * 
	 * @param feature
	 */
	protected void removeFromFeatureSpace(Integer feature) {
		setChanged();
		notifyObservers(new Operation(false, feature));
	}
	
	/**
	 * Add some feature to the feature space.
	 * 
	 * @param feature
	 */
	protected void addToFeatureSpace(Integer feature) {
		setChanged();
		notifyObservers(new Operation(true, feature));
	}

	protected FeatureSpace createDefaultFeatureSpace(int nfeatures) {
		return new DefaultFeatureSpace(nfeatures);
	}
	
	/**
	 * Run the actual algorithm. This method has to fill the
	 * bestFeatures array. Each time the candidate solution is
	 * modified, the search space must be adjusted using the methods
	 * removeFromFeatureSpace and addToFeatureSpace, respectively.
	 * 
	 * @param f
	 * @param available
	 * @param dimension
	 * @param targetSize
	 * @return
	 */
	protected abstract void doRun(CriterionFunction f, Iterable<Integer> available, int dimension, int targetSize);

	/**
	 * This is a class that describes an operation in nested
	 * subset methods. It is used by the observer design to notify
	 * clients on modification of the candidate solution.
	 * 
	 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
	 */
	public class Operation {
		public final Integer feature;
		public final boolean op;
		
		private Operation(boolean op, Integer feature) {
			this.op = op;
			this.feature = feature;
		}
		
		/** Return, whether this was an add operation. */
		public boolean isAdd() { return op; }
	
		/** Return, whether this was a remove operation. */
		public boolean isRemove() { return !op; }
	
		/** Return the index subject to this operation. */
		public Integer getFeature() { return feature; }
	}
}
