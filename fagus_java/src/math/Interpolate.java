package math;

public class Interpolate {
	
	/**
	 * This gives the result of the polynomial approximation of a function
	 * at a given point x. The function is interpolated by the x and y points
	 * provided.
	 *  
	 * @param xs
	 * @param ys
	 * @param x
	 * @return
	 */
	public static double polynomialInterpolate(double[] xs, double[] ys, double x) {
		final int n = xs.length;
		
		/*
		 * The Neville scheme is used in favour of the Lagrangian form.
		 */
		double[] t = new double[n];
		for(int i = 0; i < n; i++) {
			t[i] = ys[i];
		}
		
		for(int k = 1; k < n; k++) {
			for(int i = 0; i < n - k; i++) {
				t[i] = ( (xs[i+k] - x) * t[i] + (x - xs[i]) * t[i+1] ) / (xs[i+k] - xs[i]);
			}
		}
		
		
		return t[0];
	}
}
