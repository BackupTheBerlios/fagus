package math;

/**
 * This is an interface for real functions, since Java
 * does not provide function pointers.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface Function {
	/**
	 * Get the value of a function at a specific location.
	 * 
	 * @param x 
	 * @return
	 */
	public double getValue(double x);
}
