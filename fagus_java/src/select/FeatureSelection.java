package select;

import util.VectorSet;
import util.io.ExportVisitor;

/**
 * This is the common interface of all feature selection 
 * algorithms.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface FeatureSelection {
	
	/**
	 * Initialize with a given set of vectors.
	 * 
	 * @param vectors
	 */	
	void initialize(VectorSet vectors);
	
	/**
	 * Get the mappings of the vectors that were used for
	 * initialization.
	 * 
	 * @return
	 */
	VectorSet getMappedData();
	
	/**
	 * Select the relevant features from a single vector.
	 * 
	 * @param original the original vector.
	 * @return the mapped vector (with lower dimension).
	 */
	double[] mapVector(double[] original);
	
	/**
	 * Export (i.e. serialize) this feature selection system.
	 * 
	 * @param exporter
	 */
	void export(ExportVisitor exporter);
	
}
