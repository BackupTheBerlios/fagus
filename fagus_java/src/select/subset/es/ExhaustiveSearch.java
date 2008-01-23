package select.subset.es;

import java.util.Observable;

import math.Binomial;

import select.subset.CriterionFunction;
import select.subset.SelectionAlgorithm;
import util.VectorSet;

/**
 * Exhaustively search for the best subset of features.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class ExhaustiveSearch extends Observable implements SelectionAlgorithm {
	private int dimension;
	private int targetDimension;
	private int[] bestFeatures;
	private long numberOfSubsets;
	private long subsetsProcessed;
	private double bound;
	private CriterionFunction criterion;
	
	public int[] getFeatureVector() {
		return bestFeatures;
	}

	public void run(VectorSet vectors, CriterionFunction f, int dropNrFeatures) {
		dimension = vectors.getDimension();
		targetDimension = dimension - dropNrFeatures;
		
		numberOfSubsets = Binomial.binomial(dimension, targetDimension);
		subsetsProcessed = 0L;
		
		criterion = f;
		criterion.initialize(dimension, vectors);
		
		int[] candidate = new int[targetDimension];
		bestFeatures = new int[targetDimension];
		
		bound = Double.NEGATIVE_INFINITY;

		/*
		 * We require that the feature at index i is always
		 * greater than the one at index (i-1). Therefore, the
		 * first index can only grow up to dimension - targetDimension
		 */
		for(int i = 0; i <= dimension - targetDimension; i++) {
			candidate[0] = i;
			search(1, candidate);
		}
	}
	
	private void search(int index, int[] candidate) {
		if(index == targetDimension) {
			// stop recursion -> evaluate candidate
			double value = criterion.getCriterionValue(candidate);
			
			if(value > bound) {
				bound = value;
				System.arraycopy(candidate, 0, bestFeatures, 0, targetDimension);
			}
			
			subsetsProcessed++;
			setChanged();
			notifyObservers((double)subsetsProcessed/numberOfSubsets);
			
		} else {
			// For all possible values of this index search all
			// the childs.
			for(int i = candidate[index - 1] + 1; i <= dimension - targetDimension + index; i++) {
				candidate[index] = i;
				search(index + 1, candidate);
			}
		}
	}

}
