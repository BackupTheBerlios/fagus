package select.subset.greedy;

import java.util.ArrayList;

import select.subset.CriterionFunction;

/**
 * The floating search is an improvement of greedy search. It is an
 * alterating add and remove processes. A new feature is selected using
 * a greedy policy and some of the worst feature might be droped
 * afterwards. The algorithm produces suboptimal results, that are
 * usually close to the optimal solution.
 * 
 * See P. Pudil, F.J. Ferri, J. Novovicova and J. Kittler
 *     "Floating Search Methods for Feature Selection with Nonmonotonic
 *     Criterion Functions"
 *     Pattern Recognition Letters
 *     Vol. 15, pp. 1119--1125
 *     1994
 *     
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class SequentialForwardFloatingSearch extends NestedSubsetAlgorithm {
	
	@Override
	protected void doRun(CriterionFunction f, Iterable<Integer> available, int dimension, int targetSize) {
		if(candidate == null) {
			candidate = new ArrayList<Integer>(targetSize);
			// no need to reduce feature space here, since the candidate is empty
		}
		
		double[] criterionValues = new double[targetSize - candidate.size()];
		GreedySelection.IndexValue best;
		
		int n = candidate.size();
		int i = n;
		
		/*
		 * The initial candidate contains two elements which
		 * are selected in a greedy way.
		 */
		if(i < targetSize) {
			best = GreedySelection.getBestFeature(f, available, candidate, selectionComparator);
			candidateValue = best.value;
			candidate.add(best.index);
			removeFromFeatureSpace(best.index);
			i++;
			criterionValues[targetSize - i] = best.value;
		}
		
		if(i < targetSize) {
			best = GreedySelection.getBestFeature(f, available, candidate, selectionComparator);
			candidateValue = best.value;
			candidate.add(best.index);
			removeFromFeatureSpace(best.index);
			i++;
			criterionValues[targetSize - i] = best.value;
		}
		
		while(i < targetSize) {
			best = GreedySelection.getBestFeature(f, available, candidate, selectionComparator);
			
			// Add best feature
			candidateValue = best.value;
			candidate.add(best.index);
			removeFromFeatureSpace(best.index);
			i++;
			criterionValues[targetSize - i] = best.value;
			
			// Now drop bad features, if any
			GreedySelection.IndexValue worst = GreedySelection.getWorstFeature(f, candidate, selectionComparator);
			while((i > n + 1) && (worst.value > criterionValues[targetSize - i + 1])) {
				candidateValue = worst.value;
				candidate.remove(worst.index);
				addToFeatureSpace(worst.index);
				i--;
				criterionValues[targetSize - i] = worst.value;
				
				worst = GreedySelection.getWorstFeature(f, candidate, selectionComparator);
			}
		}

	}
}
