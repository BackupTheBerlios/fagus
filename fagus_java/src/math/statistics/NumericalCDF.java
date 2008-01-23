package math.statistics;

import math.Integrate;

/**
 * This is a simple class that provides a CDF, even if
 * it cannot be explicitely computed.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class NumericalCDF implements CDF {
	private final PDF pdf;
	private static final double PRECISION = 1.0E-3;
	
	public NumericalCDF(PDF pdf) {
		this.pdf = pdf;
	}
	
	public double getValue(double x) {
		if(x <= 0.5) {
			return Integrate.integrate(pdf, 0.0, x, PRECISION);
		} else  {
			return 1.0 - Integrate.integrate(pdf, x, 1.0, PRECISION);
		}
	}

}
