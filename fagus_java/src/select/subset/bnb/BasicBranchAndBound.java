package select.subset.bnb;

import java.util.Iterator;

/**
 * This is an implementation of an unoptimized Branch &amp; Bound
 * algorithm.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class BasicBranchAndBound extends BranchAndBound {

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
		int descendantsLength = controlSet.size() - dimension + targetSize + level + 1;

		if(descendantsLength == 1) {
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
		
		int[] descendants = new int[descendantsLength];
		Iterator<Integer> it = controlSet.iterator();
		
		for(int i = 0; i < descendantsLength; i++) {
			descendants[i] = it.next();
		}
		for(int i = 0; i < descendantsLength; i++) {
			controlSet.remove(descendants[i]);			
		}
		
		
		for(int i = descendantsLength -1; i >= 0; i--) {
			int child = descendants[i];
			
			int[] config = new int[candidate.length - 1];
			
			for(int j = 0; j < candidate.length; j++) {
				if(child == candidate[j]) {
					for(; j < candidate.length - 1; j++) {
						config[j] = candidate[j+1];
					}
					break;
				} 
				config[j] = candidate[j];
			}

			totalEvaluations++;
			double value = criterion.getCriterionValue(config);
			
			if(value > bound) {
				// subtree cannot be cut off
				if(level + 1 == dimension - targetSize) {
					// we reached a leave -> update bound
					bound = value;
					bestFeatures = config;
					
					updateProgress();
				} else {
					// go to next level
					branchAndBound(level + 1, config);
				}
				
			} else {
				updateProgress(level);
			}

			controlSet.add(child);
		}
	}
}
