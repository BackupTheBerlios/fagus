package select.subset.bnb;

import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;

import math.Binomial;

import select.subset.CriterionFunction;
import select.subset.SelectionAlgorithm;
import util.VectorSet;

/**
 * Common interface for Branch &amp; Bound algorithms.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public abstract class BranchAndBound extends Observable implements SelectionAlgorithm {
	protected int totalEvaluations;        // number of evaluations of criterion
	protected double bound;                // maximum value of some subset
	protected int[] bestFeatures;          // indices of best subset of features
	protected int dimension;               // number of original features
	protected int targetSize;              // desired size of feature subset
	protected Set<Integer> controlSet;     // used to maintain search tree topology
	protected CriterionFunction criterion;

	private long numberLeaves;
	private long leavesProcessed;
	
	/**
	 * Get the criterion value for the best feature vector. 
	 * @return
	 */	
	public double getBound() {
		return bound;
	}
	
	/**
	 * Get the number of evaluations of the criterion function during
	 * the search.
	 * @return
	 */
	public int getNumberOfEvaluations() {
		return totalEvaluations;
	}

	public int[] getFeatureVector() {
		return bestFeatures;
	}
	
	public void run(VectorSet vectors, CriterionFunction f, int dropNrFeatures) {
		dimension = vectors.getDimension();
		targetSize = dimension - dropNrFeatures;
		numberLeaves = Binomial.binomial(dimension, targetSize);
		leavesProcessed = 0L;
		
		criterion = f;
		criterion.initialize(dimension, vectors);
		controlSet = new TreeSet<Integer>();
		
		// initially all vector elements are used
		for(int i = 0; i < dimension; i++) {
			controlSet.add(i);
		}
		
		/*
		 * A criterion function gives higher values for better
		 * feature vectors. So we start at -infinity.
		 */
		bound = Double.NEGATIVE_INFINITY;
		
		totalEvaluations = 0;
		initBnb();
	}
	
	protected abstract void initBnb();

	protected void updateProgress() {
		leavesProcessed ++;
		setChanged();
		notifyObservers((double)leavesProcessed/numberLeaves);
	}
	
	protected void updateProgress(int level) {
		final int n = controlSet.size();
		final int k = dimension - targetSize - level - 1;
		
		leavesProcessed += Binomial.binomial(n, k);
		setChanged();
		notifyObservers((double)leavesProcessed/numberLeaves);
	}
}
