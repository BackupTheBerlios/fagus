package select.subset.bnb;

import java.util.Arrays;
import java.util.Comparator;

/**
 * This is a heurisitc extension to the improved Branch &amp; Bound
 * algorithm. The algorithm saves time by predicting the criterion
 * value for certain inner nodes, thus reducing the number of 
 * evaluations of the criterion function.
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
public class FastBranchAndBound extends BranchAndBound {
	private double[] contribution;       // heuristic information
	private int[] counter;               // evaluations of criterion function for a feature
	
	// algorithm parameters
	private final int minEvaluations = 1;
	private final double optimism = 1.0;
	
	// label criterion values to be either predicted or really evaluated
	private enum type {CALCULATED, PREDICTED};
	
	private final CandidateCompare cmp = new CandidateCompare();

	@Override
	protected void initBnb() {
		int[] candidate = new int[dimension];
		
		/*
		 * Start with all features present.
		 */
		for(int i = 0; i < candidate.length; i++) {
			candidate[i] = i;
		}
		
		/*
		 * Initialize heuristics and counters with 0.
		 */
		contribution = new double[dimension];
		counter = new int[dimension];
		
		totalEvaluations = 1;
		branchAndBound(0, new Candidate(-1, criterion.getCriterionValue(), candidate, type.CALCULATED));
	}

	private void updateContribution(int index, double decrease) {
		contribution[index] = (contribution[index] * counter[index] + decrease) / (counter[index] + 1);
		counter[index]++;
	}
	
	private void branchAndBound(int level, Candidate parent) {
		// determine the number of descendants to look at
		int descendantPointer = controlSet.size() - dimension + targetSize + level;

		if(descendantPointer == 0) {
			// there are no more branches until we reach the leaf,
			// therefore, we immediately evaluate the leaf
			int[] leave = new int[targetSize];
			int k = 0;
			
			for(int i = 0; i < parent.config.length; i++) {
				if(!controlSet.contains(parent.config[i])) {
					leave[k] = parent.config[i];
					k++;
				}
			}
			
			totalEvaluations++;
			double value = criterion.getCriterionValue(leave);
			
			if(value > bound) {
				bound = value;
				bestFeatures = leave;
			}

			updateProgress();
			
			return;
		}
		
		// subsequently leave one feature out and obtain the
		// corresponding criterion values
		Candidate[] descendants = new Candidate[controlSet.size()];
		boolean subtreesAreLeaves = level + 1 < dimension - targetSize;
		int i = 0;
		
		for(Integer child: controlSet) {
			int[] v = new int[parent.config.length - 1];

			for(int j = 0; j < parent.config.length; j++) {
				if(child == parent.config[j]) {
					for(; j < parent.config.length - 1; j++) {
						v[j] = parent.config[j+1];
					}
					break;
				} 
				v[j] = parent.config[j];
			}
			
			if(subtreesAreLeaves && counter[i] > minEvaluations) {
				descendants[i] = new Candidate(child, parent.value - contribution[child], v, type.PREDICTED);
			} else {
				totalEvaluations++;
				double value = criterion.getCriterionValue(v);
				
				updateContribution(child, parent.value - value);			
				descendants[i] = new Candidate(child, value, v, type.CALCULATED);
			}
			
			i++;
		}
	
		// Now sort the features according to their importance.
		Arrays.sort(descendants, cmp);

		// Remove the least important features
		for(i = 0; i <= descendantPointer; i++) {
			controlSet.remove(descendants[i].feature);
		}
		
		// The least important features are left out, when proceeding
		// in this tree.
		for(; descendantPointer >= 0; descendantPointer--) {
			Candidate candidate = descendants[descendantPointer];

			double value;
			if(candidate.typ == type.PREDICTED) {
				value = parent.value - optimism * contribution[candidate.feature];
				if(value <= bound) {
					totalEvaluations++;
					value = criterion.getCriterionValue(candidate.config);
					updateContribution(candidate.feature, parent.value - value);
					
					candidate.typ = type.CALCULATED;
					candidate.value = value;
				}
			} else {
				value = candidate.value;
			}
			
			if(value > bound) {
				// subtree cannot be cut off
				if(level + 1 == dimension - targetSize) {
					// we reached a leave -> update bound
//					if(candidate.typ == type.PREDICTED) {
//						totalEvaluations++;
//						candidate.value = criterion.getCriterionValue(candidate.config);
//						updateContribution(candidate.feature, parent.value - candidate.value);
//						candidate.typ = type.CALCULATED;
//					}
					
					bound = candidate.value;
					bestFeatures = candidate.config;
					
					updateProgress();
				} else {
					// go to next level
					branchAndBound(level + 1, candidate);
				}
				
			} else {
				updateProgress(level);
			}

			controlSet.add(candidate.feature);
		}
	}
	
	
	private class Candidate {
		public final int feature;
		public double value;
		public final int[] config;
		public type typ;
		
		public Candidate(int feature, double value, int [] config, type typ) {
			this.feature = feature;
			this.value = value;
			this.config = config;
			this.typ = typ;
		}
	}

	private class CandidateCompare implements Comparator<Candidate> {
		public int compare(Candidate v1, Candidate v2) {
			if (v1.value < v2.value)
				return -1;
			if (v2.value < v1.value)
				return 1;
			return 0;
		}
	}
}
