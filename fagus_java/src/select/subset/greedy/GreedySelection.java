package select.subset.greedy;

import java.util.Collection;

import select.subset.CriterionFunction;


public class GreedySelection {

	/**
	 * Get the best features from a list of available features.
	 * 
	 * @param f a criterion function to meassure the effectiveness.
	 * @param available a list of available features.
	 * @param current a list of currently used features.
	 * @return
	 */
	public static IndexValue getBestFeature(CriterionFunction f, Iterable<Integer> available, Collection<Integer> current, SelectionComparator selectionComparator) {
		double best = Double.NEGATIVE_INFINITY;
		Integer bestIndex = -1;
		
		// copy current best feature to new experiment vector
		// leaving the last element empty
		int[] vec = new int[current.size() + 1];

		int i = 0;
		for(Integer j: current) {
			vec[i++] = j;
		}
		
		for(Integer j: available) {
			// insert j at the last position in the vector
			vec[vec.length - 1] = j;
			
			double v = f.getCriterionValue(vec);
			
			if(selectionComparator.compare(j.intValue(), v, bestIndex.intValue(), best) > 0) {
				best = v;
				bestIndex = j;
			}
		}

		return new IndexValue(best, bestIndex);
	}

	
	/**
	 * Search for the worst feature, according to some criterion function, from
	 * a set of features.
	 * 
	 * @param f 
	 * @param current
	 * @return
	 */
	public static IndexValue getWorstFeature(CriterionFunction f, Collection<Integer> current, SelectionComparator selectionComparator) {
		double worst = Double.NEGATIVE_INFINITY;
		int worstIndex = -1;
		int[] all = new int[current.size()];
		int[] vec = new int[current.size() - 1];
		
		int j = 0;
		for(Integer i: current) {
			all[j++] = i;
		}
		
		for(int i = 0; i < current.size(); i++) {
			System.arraycopy(all, 0, vec, 0, i);
			System.arraycopy(all, i+1, vec, i, current.size()-i-1);
		
			double v = f.getCriterionValue(vec);
			
			int w = -1;
			if(worstIndex >= 0) {
				w = all[worstIndex];
			}
			
			if(selectionComparator.compare(all[i], v, w, worst) > 0) {
				worstIndex = i;
				worst = v;
			}
		}
		
		System.out.println(worstIndex + "(" + all[worstIndex] + "): " + worst);

		return new IndexValue(worst, new Integer(all[worstIndex]));
	}
	
	public static class IndexValue {
		public final double value;
		public final Integer index;
		
		public IndexValue(double value, Integer index) {
			this.value = value;
			this.index = index;
		}
	}

}
