package select.subset.bnb;

import java.util.Arrays;
import java.util.Comparator;

/**
 * This is an improved version of the Branch &amp; Bound algorithm.
 * At every node, the features are ordered according to their expected
 * importance and the least imporant features are dropped first.
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
public class ImprovedBranchAndBound extends BranchAndBound {
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
		
		branchAndBound(0, candidate);
	}

	private void branchAndBound(int level, int[] candidate) {
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
		
		for(Integer child: controlSet) {
			int[] v = new int[candidate.length - 1];
			
			for(int j = 0; j < candidate.length; j++) {
				if(child == candidate[j]) {
					for(; j < candidate.length - 1; j++) {
						v[j] = candidate[j+1];
					}
					break;
				} 
				v[j] = candidate[j];
			}
		
			totalEvaluations++;
			descendants[i] = new CriterionValue(child, criterion.getCriterionValue(v), v);

			i++;
		}
	
		// Now sort the features according to their importance.
		Arrays.sort(descendants, cmp);

		// Remove the least important features
		for(i = 0; i <= descendantPointer; i++) {
			controlSet.remove(new Integer(descendants[i].feature));
		}
		
		// The least important features are left out, when proceeding
		// in this tree.
		for(; descendantPointer >= 0; descendantPointer--) {
			CriterionValue child = descendants[descendantPointer];
			
			if(child.value > bound) {
				// subtree cannot be cut off
				if(level + 1 == dimension - targetSize) {
					// we reached a leave -> update bound
					bound = child.value;
					bestFeatures = child.config;
					
					updateProgress();
				} else {
					// go to next level
					branchAndBound(level + 1, child.config);
				}
				
			} else {
				updateProgress(level);
			}

			controlSet.add(child.feature);
		}
	}
	
	private class CriterionValue {
		public final int feature;
		public final double value;
		public final int[] config;
		
		public CriterionValue(int feature, double value, int[] config) {
			this.feature = feature;
			this.value = value;
			this.config = config;
		}
	}
	
	private class CriterionCompare implements Comparator<CriterionValue> {
		public int compare(CriterionValue v1, CriterionValue v2) {
			if (v1.value < v2.value)
				return -1;
			if (v2.value < v1.value)
				return 1;
			return 0;
		}
	}
}
