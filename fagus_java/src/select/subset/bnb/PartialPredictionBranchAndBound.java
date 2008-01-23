package select.subset.bnb;

import java.util.Arrays;
import java.util.Comparator;

/**
 * This is a Branch &amp; Bound algorithm using simple heuristics
 * to reduce the number of evaluations of the criterion function.
 * 
 * <br><br>
 * See P. Somol, P. Pudil, and J. Kittler
 *     "Fast Branch & Bound Algorithms for Optimal Feature Selection"
 *     in Transcactions on Pattern Anlaysis and Machine Intelligence
 *     vol.26 no.7 pp.900--912
 *     IEEE 2004
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class PartialPredictionBranchAndBound extends BranchAndBound {
	private double[] contribution;       // heuristic information about imporance of features
	private int[] counter;               // number of evaluations of criterion for each single feature
	
	private final CriterionCompare cmp = new CriterionCompare();

	@Override
	protected void initBnb() {
		int[] candidate = new int[dimension];
		
		/*
		 * Start with all features present.
		 */
		for(int i = 0; i < candidate.length; i++) {
			candidate[i] = i;
		}

		contribution = new double[dimension];
		counter = new int[dimension];
		totalEvaluations = 1;
		
		branchAndBound(0, candidate, criterion.getCriterionValue());
	}

	/*
	 * Update the heuristics.
	 */
	private void updateContribution(int index, double decrease) {
		contribution[index] = (contribution[index] * counter[index] + decrease) / (counter[index] + 1);
		counter[index]++;
	}

	
	private void branchAndBound(int level, int[] candidate, double parentValue) {
		// determine the number of descendants to look at
		int descendantPointer = controlSet.size() - dimension + targetSize + level;

		if(descendantPointer == 0) {
			// there are no more branches until the leaf is reached,
			// therefore, we immediately evaluate the child
			int[] leaf = new int[targetSize];
			int k = 0;
			
			for(int i = 0; i < candidate.length; i++) {
				if(!controlSet.contains(candidate[i])) {
					leaf[k] = candidate[i];
					k++;
				}
			}
			
			totalEvaluations++;
			double value = criterion.getCriterionValue(leaf);
			
			if(value > bound) {
				bound = value;
				bestFeatures = leaf;
			}
			
			updateProgress();
			
			return;
		}
		
		// subsequently leave one feature out and obtain the
		// corresponding criterion values
		CriterionValue[] descendants = new CriterionValue[controlSet.size()];
		
		int i = 0;
		
		for(Integer currF: controlSet) {
			descendants[i] = new CriterionValue(currF, contribution[currF]);
			i++;
		}
	
		// Sort according to predicted contributions in descending order.
		Arrays.sort(descendants, cmp);

		// Remove the least important features
		for(i = 0; i <= descendantPointer; i++) {
			controlSet.remove(new Integer(descendants[i].feature));
		}
		
		// The least important features are left out, when proceeding
		// in this tree.
		for(; descendantPointer >= 0; descendantPointer--) {
			int feature = descendants[descendantPointer].feature;

			int[] subset = new int[candidate.length - 1];
			for(int j = 0; j < candidate.length; j++) {
				if(feature == candidate[j]) {
					for(; j < candidate.length - 1; j++) {
						subset[j] = candidate[j+1];
					}
					break;
				} 
				subset[j] = candidate[j];
			}
		
			totalEvaluations++;
			double value = criterion.getCriterionValue(subset);
			updateContribution(feature, parentValue - value);
			
			if(value > bound) {
				// subtree cannot be cut off
				if(level + 1 == dimension - targetSize) {
					// we reached a leave -> update bound
					bound = value;
					bestFeatures = subset;
					
					updateProgress();
				} else {
					// go to next level
					branchAndBound(level + 1, subset, value);
				}
				
			} else {
				updateProgress(level);
			}

			controlSet.add(feature);
		}
	}
	
	
	private class CriterionValue {
		public final int feature;
		public final double value;
		
		public CriterionValue(int feature, double value) {
			this.feature = feature;
			this.value = value;
		}
	}

	private class CriterionCompare implements Comparator<CriterionValue> {
		public int compare(CriterionValue v1, CriterionValue v2) {
			// Note, that this sort is descending
			if (v1.value > v2.value)
				return -1;
			if (v2.value > v1.value)
				return 1;
			return 0;
		}
	}
}
