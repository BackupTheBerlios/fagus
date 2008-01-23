package select.subset.greedy;

import java.util.ArrayList;
import java.util.List;

import select.subset.CriterionFunction;

/**
 * This is a greedy algorithm for feature subset selection.
 * The algorithm works by subsequently dropping the worst feature
 * form the set of features.
 * 
 * See Chapter 10.5 "Feature Subset Selection" 
 *     in K. Fukunaga
 *     "Introduction to statistical pattern recognition"
 *     2nd edition
 *     Academic Press, 1990.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class BackwardSelection extends NestedSubsetAlgorithm {
	
	@Override
	protected void doRun(CriterionFunction f, Iterable<Integer> features, int dimension, int targetSize) {
		GreedySelection.IndexValue worst = null;
		if(candidate == null) {
			// set all available features
			List<Integer> candidate = new ArrayList<Integer>(dimension);
			for(Integer i: features) {
				candidate.add(i);
			}
			
			setInitialCandidate(candidate);
		}
		
		for(int i = candidate.size(); i > targetSize; i--) {
			worst = GreedySelection.getWorstFeature(f, candidate, selectionComparator);
			candidateValue = worst.value;
			candidate.remove(worst.index);
			addToFeatureSpace(worst.index);
		}
	}
}
