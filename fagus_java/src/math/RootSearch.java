package math;

/**
 * This class provides static routines for searching for a root
 * of a function.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class RootSearch {
	private static final double newtonDelta = 0.01;
	
	/**
	 * Use the bisection algorithm. This algorithm assumes that the function
	 * value at the start is negative and the value at the end of the interval
	 * is positive. Moreover, the function is assumed to have only one root and
	 * to be continous. The algorithm is slower than Newton's method.
	 *  
	 * @param f
	 * @param iStart start of the interval.
	 * @param iEnd end of the interval.
	 * @param precision
	 * @return
	 */
	public static double bisection(Function f, double iStart, double iEnd, double precision) {
		double iMed;
		double y;
		
		do {
			iMed = (iStart + iEnd) / 2.0;
			y = f.getValue(iMed);
			
			if(y < 0) {
				iStart = iMed;
			} else {
				iEnd = iMed;
			}
		} while(Math.abs(y) > precision);
		
		return iMed;
	}
	
	/**
	 * Use Newton's approximation for finding the root of a function.
	 * 
	 * @param f
	 * @param start
	 * @param precision
	 * @return
	 */
	public static double newton(Function f, double start, double precision) {
		double xnew = start;
		double y = Double.NEGATIVE_INFINITY;
		
		while(Math.abs(y) > precision) {
			double xold = xnew;
			
			/*
			 * x_new = x_old - f(x_old) / f'(x_old)
			 */
			y = f.getValue(xold);
			double y2 = f.getValue(xold + newtonDelta);
			
			xnew = xold  - y * newtonDelta / (y2 - y); 
		}
		
		return xnew;
	}
}
