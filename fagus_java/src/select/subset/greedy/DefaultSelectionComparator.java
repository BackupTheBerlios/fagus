package select.subset.greedy;

/**
 * This is the default implementation of the SelectionComparator.
 * The choice is made only based on the criterion value. The features
 * are ignored.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class DefaultSelectionComparator implements SelectionComparator {

	public int compare(int feature1, double criterion1, int feature2, double criterion2) {
		if(criterion1 < criterion2) {
			return -1;
		}
		if(criterion2 < criterion1) {
			return 1;
		}
		return 0;
	}

}
