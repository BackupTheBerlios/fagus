package classify.parzen;

/**
 * A simple interface for kernels for use in Parzen classifiers.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface Kernel {
	/**
	 * Get the kernel value at a given point v.
	 * 
	 * @param v
	 * @return
	 */
	double getValue(double[] v);
}
