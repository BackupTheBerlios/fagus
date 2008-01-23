package fourier.search;

import select.subset.greedy.SelectionComparator;

/**
 * This is a custom comparator for feature selection. The comparator
 * first tries to make a decision based on the two criterion value.
 * If the two criterions are equal, a decision based on the ring widhts
 * is made. That is, broader rings are prefered over narrow ones.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class FourierSelectionComparator implements SelectionComparator {
	private final FourierSearchSpace searchSpace;
	
	public FourierSelectionComparator(FourierSearchSpace searchSpace) {
		this.searchSpace = searchSpace;
	}
	
	public int compare(int feature1, double criterion1, int feature2, double criterion2) {
		if(criterion1 > criterion2) {
			return 1;
		}
		
		if(criterion2 > criterion1) {
			return -1;
		}

		/*
		 * If both criterion values are equal, we 
		 * take the broader ring.
		 */
		int[] f1 = searchSpace.decode(feature1);
		int[] f2 = searchSpace.decode(feature2);
			
		return f1[2] - f2[2];
	}

}
