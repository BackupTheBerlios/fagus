package select.subset.bnb;

import java.util.Arrays;
import java.util.Comparator;

import select.subset.RecursiveCriterionFunction;

public class RecursiveBranchAndBound extends BranchAndBound {
	private final CriterionCompare cmp = new CriterionCompare();
	
	@Override
	protected void initBnb() {
		branchAndBound(0, ((RecursiveCriterionFunction)criterion).getCriterionState());
	}

	private void branchAndBound(int level, RecursiveCriterionFunction.State parent) {
		// determine the number of descendants to look at
		int descendantPointer = controlSet.size() - dimension + targetSize + level;
		
		// subsequently leave one feature out and obtain the
		// corresponding criterion values
		RecursiveCriterionFunction.State[] descendants = new RecursiveCriterionFunction.State[controlSet.size()];
		int i = 0;
		
		for(Integer child: controlSet) {
			descendants[i] = ((RecursiveCriterionFunction)criterion).getCriterionState(child, parent);
			totalEvaluations++;
			i++;
		}

		
		// Now sort the features according to their importance.
		Arrays.sort(descendants, cmp);

		// Remove the least important features
		for(i = 0; i <= descendantPointer; i++) {
			controlSet.remove(descendants[i].getRemovedFeature());
		}
		
		// The least important features are left out, when proceeding
		// in this tree.
		for(; descendantPointer >= 0; descendantPointer--) {
			RecursiveCriterionFunction.State childState = descendants[descendantPointer];
			
			if(childState.getValue() > bound) {
				// subtree cannot be cut off
				if(level + 1 == dimension - targetSize) {
					// we reached a leave -> update bound
					bound = childState.getValue();
					bestFeatures = childState.getConfig();

					updateProgress();
				} else {
					// go to next level
					branchAndBound(level + 1, childState);
				}
				
			} else {
				// subtree is cut off
				updateProgress(level);
			}

			controlSet.add(childState.getRemovedFeature());
		}
	}
	
	private class CriterionCompare implements Comparator<RecursiveCriterionFunction.State> {
		public int compare(RecursiveCriterionFunction.State state1, RecursiveCriterionFunction.State state2) {
			double v1 = state1.getValue();
			double v2 = state2.getValue();
			
			if (v1 < v2)
				return -1;
			if (v2 < v1)
				return 1;
			return 0;
		}
	}

}
