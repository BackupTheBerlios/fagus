package select.subset.greedy;

/**
 * This is a simple extension of a feature space that provides 
 * and additional clone method. This is usefull for the
 * OscillatingSearch, for instance.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface CloneableFeatureSpace extends FeatureSpace, Cloneable {
	/**
	 * Make a deep copy of the feature space.
	 * 
	 * @return
	 * @throws CloneNotSupportedException This should never be thrown. Just
	 * for compatibility with Object.
	 */
	CloneableFeatureSpace clone() throws CloneNotSupportedException;
}
