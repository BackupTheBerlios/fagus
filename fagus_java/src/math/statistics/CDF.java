package math.statistics;

import math.Function;

/**
 * Interface for a Cummulated Distribution Function (CDF).
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface CDF extends Function {
	double getValue(double x);
}
