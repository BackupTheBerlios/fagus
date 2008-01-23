package select.subset.greedy;

import java.util.ArrayList;
import java.util.List;

import select.subset.CriterionFunction;

public class SequentialBackwardFloatingSearch extends NestedSubsetAlgorithm {

	@Override
	protected void doRun(CriterionFunction f, Iterable<Integer> features, int dimension, int targetSize) {
		if(candidate == null) {
			// set all available features
			List<Integer> candidate = new ArrayList<Integer>(dimension);
			for(Integer i: features) {
				candidate.add(i);
			}
			
			setInitialCandidate(candidate);
		}
		
		double[] criterionValues = new double[candidate.size() - targetSize];
		GreedySelection.IndexValue worst;
		
		int n = candidate.size();
		int i = n;
		
		/*
		 * The initial candidate contains two elements which
		 * are selected in a greedy way.
		 */
		if(i > targetSize) {
			worst = GreedySelection.getWorstFeature(f, candidate, selectionComparator);
			candidateValue = worst.value;
			candidate.remove(worst.index);
			addToFeatureSpace(worst.index);
			i--;
			criterionValues[i - targetSize] = worst.value;
		}
		
		if(i > targetSize) {
			worst = GreedySelection.getWorstFeature(f, candidate, selectionComparator);
			candidateValue = worst.value;
			candidate.remove(worst.index);
			addToFeatureSpace(worst.index);
			i--;
			criterionValues[i - targetSize] = worst.value;			
		}
		
		while(i > targetSize) {
			worst = GreedySelection.getWorstFeature(f, candidate, selectionComparator);
			
			candidateValue = worst.value;
			candidate.remove(worst.index);
			addToFeatureSpace(worst.index);
			i--;
			criterionValues[i - targetSize] = worst.value;
			
			GreedySelection.IndexValue best = GreedySelection.getBestFeature(f, features, candidate, selectionComparator);
			while((i < n - 1) && (best.value < criterionValues[i - targetSize + 1])) {
				candidateValue = best.value;
				candidate.add(best.index);
				removeFromFeatureSpace(best.index);
				i++;
				
				criterionValues[i-targetSize] = best.value;
				
				best = GreedySelection.getBestFeature(f, features, candidate, selectionComparator);
			}
		}
		
	}

	@Override
	public Iterable<Integer> getCandidate() {
		return candidate;
	}

}
