package select.subset.greedy;

import java.util.Observer;

/**
 * This interface is an abstraction of a feature space. All the
 * required methods are imported from the implemented interfaces.
 * A feature space has to provide some iterator for the individual
 * features, where each feature is represented by an integer value.
 * Moreover, a feature space must be observable. The observer
 * interface is used to allow features to be removed from or added 
 * to the feature space.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface FeatureSpace extends Observer, Iterable<Integer> {
	// Nothing to be done here
}
