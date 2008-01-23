package math.statistics;

import math.Function;

/**
 * Interfase for a Probability Density Function (PDF).
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface PDF extends Function {
	public double getValue(double x);
}
