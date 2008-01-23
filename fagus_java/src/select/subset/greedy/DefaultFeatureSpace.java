package select.subset.greedy;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;

import select.subset.greedy.NestedSubsetAlgorithm.Operation;

/**
 * This is the default implementation of a feature space.
 * Basically it operates on a list of feature indices, that
 * is, on a list of integer values. If a feature is added 
 * to the candidate subset, it is removed from the feature
 * space to exclude it from further add operations. If, on the
 * other hand, a feature is removed from the candidate, it is
 * added to the feature space again.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class DefaultFeatureSpace implements CloneableFeatureSpace {
	private final Collection<Integer> features;
	
	private DefaultFeatureSpace(Collection<Integer> features) {
		this.features = features;
	}
	
	
	public DefaultFeatureSpace() {
		this.features = new LinkedList<Integer>();
	}
	
	public void update(Observable o, Object arg) {
		Operation op = (Operation)arg;
		Integer f = op.getFeature();
		
		if(op.isAdd()) {
			features.add(f);
		} else if(op.isRemove()) {
			features.remove(f);
		}
	}

	public Iterator<Integer> iterator() {
		return features.iterator();
	}

	public CloneableFeatureSpace clone() throws CloneNotSupportedException {
		LinkedList<Integer> newFeatures = new LinkedList<Integer>(features);
		return new DefaultFeatureSpace(newFeatures);
	}
}
