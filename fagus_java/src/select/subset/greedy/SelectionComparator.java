package select.subset.greedy;

/**
 * This abstracts the compare operation for nested subset algorithms.
 * In each operation, the criterions for all features in the feature space
 * are evaluated and the best feature is added to the subset. This 
 * interface allows customizations of this process. The best feature might
 * not only be selected based on the highest criterion value, but certain
 * features might be prefered over others. For instance, if both criterion
 * values are equal, one might prefer one feature over the other.
 *  
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface SelectionComparator {
	
	/**
	 * Compare two feature and their criterion values.
	 * 
	 * @param feature1
	 * @param criterion1
	 * @param feature2
	 * @param criterion2
	 * @return An integer &gt; 0, if feature1/criterion1 is higher; 
	 *         an integer &lt; 0, if feature2/criterion2 is higher;
	 *         0 if both features are equally good.
	 */
	public int compare(int feature1, double criterion1, int feature2, double criterion2);
}
