package select.subset.greedy;

import java.util.ArrayList;

import select.subset.CriterionFunction;

/**
 * This is a greedy algorithm for feature subset selection. It 
 * starts with an empty subset and subsequently adds the feature,
 * that improves the criterion by the highest amount.
 * 
 * See Chapter 10.5 "Feature Subset Selection" 
 *     in K. Fukunaga
 *     "Introduction to statistical pattern recognition"
 *     2nd edition
 *     Academic Press, 1990.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class ForwardSelection extends NestedSubsetAlgorithm {
	
	@Override
	protected void doRun(CriterionFunction f, Iterable<Integer> features, int dimension, int targetSize) {
		GreedySelection.IndexValue best = null;
		if(candidate == null) {
			candidate = new ArrayList<Integer>(targetSize);
			// no need to reduce feature space, since the candidate is empty
		}
		
		for(int i = candidate.size(); i < targetSize; i++) {
			best = GreedySelection.getBestFeature(f, features, candidate, selectionComparator);
			candidateValue = best.value;
			candidate.add(best.index);
			removeFromFeatureSpace(best.index);
		}

	}
}
