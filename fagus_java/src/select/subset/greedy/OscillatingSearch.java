package select.subset.greedy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import select.subset.CriterionFunction;

/**
 * This algorithm tries to optimize an already given solution
 * by subsequently adding new features and dropping others. The 
 * algorithm has two phases. In the down-swing phase o features
 * are dropped and then o features are added again, both using 
 * a floating selection. The up-swing phase is just the other way
 * round, first add o features the drop o features. The swing
 * parameter o is initially set to 1 and incremented each time
 * the swing phases fail to find a better solution. If o reaches
 * some maximum value delta, the algorithm terminates.
 * 
 * See P. Somol and P. Pudil 
 *     "Oscillating Search Algorithms for Feature Selection"
 *     IEEE International Conference on Pattern Recognition
 *     pp. 406--409
 *     2000
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class OscillatingSearch extends NestedSubsetAlgorithm {
	private static final double DEFAULT_DELTA = 0.5;
	private enum State {DOWNSWING, DOWNSWING_FAILED, UPSWING, UPSWING_FAILED}
	private CloneableFeatureSpace featureSpace;
	
	@Override
	public void setInitialCandidate(Collection<Integer> features) {
		// this class assumes that the candidate is sorted.
		candidate = new TreeSet<Integer>(features);
	}
	
	@Override
	public void setFeatureSpace(FeatureSpace featureSpace) {
		// the feature space must be Cloneable
		assert(false);
	}
	
	public void setFeatureSpace(CloneableFeatureSpace featureSpace) {
		this.featureSpace = featureSpace;
	}
	
	@Override
	protected void doRun(CriterionFunction f, Iterable<Integer> features, int dimension, int targetSize) {
		int c = 0;
		int o = 1;
		State state = State.DOWNSWING;

		int delta;
		boolean done = false;
		
		/*
		 * Determine the termination value. It is usually
		 * set to 50 % of the desired subset size.
		 */
		delta = (int)(targetSize * DEFAULT_DELTA);
		
		double[] maxValues = new double[delta * 2 + 1];
		for(int i = 0; i < maxValues.length; i++) {
			maxValues[i] = Double.NEGATIVE_INFINITY;
		}
		
		/*
		 * Create an initial candidate using a greedy
		 * forward selection.
		 */
		if(candidate == null) {
			NestedSubsetAlgorithm fs = new ForwardSelection();
			fs.setSelectionComparator(selectionComparator);
			try {
				fs.setFeatureSpace(featureSpace.clone());
			} catch(CloneNotSupportedException e) {
				// cannot happen
			}
			fs.doRun(f, features, dimension, targetSize);
			maxValues[delta] = fs.getCandidateValue();
			
			candidate = new TreeSet<Integer>();
			for(Integer feature: fs.getCandidate()) {
				candidate.add(feature);
			}
		} else {
			int[] tmp = new int[targetSize];
			int i = 0;
			for(Integer feature: candidate) {
				tmp[i++] = feature;
			}
			maxValues[delta] = f.getCriterionValue(tmp);
		}
		
		while(!done) {
			SortedSet<Integer> newAvailable;
			NestedSubsetAlgorithm alg;
			Collection<Integer> oldCandidate;
			double v;
			
			switch(state) {
			case DOWNSWING:
				//System.out.println("Down " + o);

				newAvailable = new TreeSet<Integer>();
				for(Integer feature: features) {
					newAvailable.add(feature);
				}
				
				alg = new SequentialBackwardFloatingSearch();
				alg.setSelectionComparator(selectionComparator);
				try {
					alg.setFeatureSpace(featureSpace.clone());
				} catch(CloneNotSupportedException e) {
					// cannot happen
				}
				alg.setInitialCandidate(new TreeSet<Integer>(candidate));
				
				/*
				 * Drop o features.
				 */
				try {
					alg.doRun(f, newAvailable, dimension, targetSize - o);
					v = alg.getCandidateValue();
				} catch(ArithmeticException e) {
					state = State.DOWNSWING_FAILED;
					break;
				}
				
				/*
				 * If this candidate is not the best among all subsets
				 * of target size - o seen so far, break downswing.
				 */
				if(v < maxValues[delta - o]) {
					state = State.DOWNSWING_FAILED;
					break;
				}
				
				maxValues[delta - o] = v;

				oldCandidate = alg.candidate;
				
				alg = new SequentialForwardFloatingSearch();
				alg.setSelectionComparator(selectionComparator);
				try {
					alg.setFeatureSpace(featureSpace.clone());
				} catch(CloneNotSupportedException e) {
					// cannot happen
				}
				alg.setInitialCandidate(oldCandidate);
				
				/*
				 * Add o new features.
				 */
				try {
					alg.doRun(f, newAvailable, dimension, targetSize);
					v = alg.getCandidateValue();
				} catch(ArithmeticException e) {
					state = State.DOWNSWING_FAILED;
					break;
				}

				if(v > maxValues[delta]) {
					/*
					 * Better solution has been found.
					 */
					candidateValue = v;
					updateCandidate(alg.getCandidate());
					
					maxValues[delta] = v;
					
					c = 0;
					o = 1;
					
					state = State.UPSWING;
					break;
				}
				// fall through
			
			case DOWNSWING_FAILED:
				c++;
				if(c == 2) {
					/*
					 * Last downswing failed as well. Increment oscillation
					 * parameter.
					 */
					o++;
					
					if(o > delta) {
						done = true;
						break;
					}
					
					c = 0;
				}
				// fall through
			
			case UPSWING:
				//System.out.println("Up " + o);
				
				newAvailable = new TreeSet<Integer>();
				for(Integer feature: features) {
					newAvailable.add(feature);
				}
				
				alg = new SequentialForwardFloatingSearch();
				alg.setSelectionComparator(selectionComparator);
				try {
					alg.setFeatureSpace(featureSpace.clone());
				} catch(CloneNotSupportedException e) {
					// cannot happen
				}
				alg.setInitialCandidate(new TreeSet<Integer>(candidate));
				
				/*
				 * Add o new features.
				 */
				try {
					alg.doRun(f, newAvailable, dimension, targetSize + o);
					v = alg.getCandidateValue();
				} catch(ArithmeticException e) {
					state = State.UPSWING_FAILED;
					break;
				}
				
				/*
				 * If this candidate is not the best among all subsets
				 * of target size + o seen so far, break upswing.
				 */
				if(v < maxValues[delta + o]) {
					state = State.UPSWING_FAILED;
					break;
				}

				maxValues[delta + o] = v;

				oldCandidate = alg.candidate;
				
				alg = new SequentialBackwardFloatingSearch();
				alg.setSelectionComparator(selectionComparator);
				try {
					alg.setFeatureSpace(featureSpace.clone());
				} catch(CloneNotSupportedException e) {
					// cannot happen
				}
				alg.setInitialCandidate(oldCandidate);

				/*
				 * Drop o features.
				 */
				try {
					alg.doRun(f, newAvailable, dimension, targetSize);
					v = alg.getCandidateValue();
				} catch(ArithmeticException e) {
					state = State.UPSWING_FAILED;
					break;
				}

				if(v > maxValues[delta]) {
					/*
					 * Better solution has been found.
					 */
					candidateValue = v;
					updateCandidate(alg.getCandidate());
					
					maxValues[delta] = v;
					
					c = 0;
					o = 1;
					
					state = State.DOWNSWING;
					break;
				}
				// fall through
				
			case UPSWING_FAILED:
				c++;
				if(c == 2) {
					/*
					 * Last upswing failed as well. Increment oscillation
					 * parameter.
					 */
					o++;
					
					if(o > delta) {
						done = true;
						break;
					}
					
					c = 0;
				}
				
				state = State.DOWNSWING;
			}
		}

	}

	/*
	 * Update the list of candidate features.
	 */
	private void updateCandidate(Iterable<Integer> newCandidate) {
		// list of features to be added to the search space
		// and removed from the candidate
		List<Integer> add = new ArrayList<Integer>();
		
		// list of features to be removed from the search space
		// and added to the candidate
		List<Integer> remove = new ArrayList<Integer>();
		
		Iterator<Integer> it1 = newCandidate.iterator();
		Iterator<Integer> it2 = candidate.iterator();

		/*
		 * We assume that both the candidate and the newCandidate
		 * are sorted. Therefore, we can use a method similar to
		 * the merge operation in mergesort.
		 */
		Integer i1, i2;
		do {
			i1 = it1.next();
			i2 = it2.next();
			
			/*
			 * If i1 and i2 are equal, we simply take the next
			 * element of each list.
			 * 
			 * If they do not match, all elements in the
			 * newCandidate that are smaller than i2 are added 
			 * to the remove list and all elements in the
			 * candidate that are smaller than i1 are added to
			 * the add list.
			 */
			while(!i1.equals(i2)) {
				if(i1.intValue() < i2.intValue()) {
					remove.add(i1);
					if(it1.hasNext()) {
						i1 = it1.next();
					} else {
						add.add(i2);
						break;
					}
				} else {
					add.add(i2);
					if(it2.hasNext()) {
						i2 = it2.next();
					} else {
						remove.add(i1);
						break;
					}
				}
			}
		} while(it1.hasNext() && it2.hasNext());

		// one of the lists might still contain elements
		
		while(it1.hasNext()) {
			i1 = it1.next();
			remove.add(i1);
		}
		
		while(it2.hasNext()) {
			i2 = it2.next();
			add.add(i2);
		}
		
		
		// adjust search space and candidate
		for(Integer i: add) {
			candidate.remove(i);
			addToFeatureSpace(i);
		}
		
		for(Integer i: remove) {
			candidate.add(i);
			removeFromFeatureSpace(i);
		}
	}
}
