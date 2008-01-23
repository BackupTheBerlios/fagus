package math;

import java.util.ArrayList;
import java.util.List;

public class Integrate {
	/**
	 * Integrate a function numerically. This uses Romberg's method
	 * for numerical integration.
	 * 
	 * @param f the function to integrate.
	 * @param a the lower bound of the interval.
	 * @param b the upper bound of the interval.
	 * @return the integral.
	 */
	public static double integrate(Function f, double a, double b, double precision) {
		List<Double> t = new ArrayList<Double>();
		int i = 0;
		double oldValue;
		
		/*
		 * The Romberg scheme is as follows:
		 * 
		 * Let h be the width of an integration interval. The integral of
		 * each interval is estimated using the trapezoidal formula 
		 * 
		 * T(h) = h/2 [f(x_0) + f(x_N) + 2 * sum_{i=1}^{N-1} f(x_i)]
		 * 
		 * where N is the number of points
		 * 
		 * N = (b - a) / h
		 * x_i = a + i * h
		 * 
		 * Note, that x_0 = a and x_N = b.
		 * 
		 * The integration is carried out iteratively using the following scheme
		 * 
		 * T_i0 = T(h_i)
		 * T_ik = (4^k * T_{i,k-1} - T_{i-1,k-1}) / (4^k -1)
		 * 
		 * where
		 * 
		 * h_i = (b-a) / 2^i    i=0,1,...
		 * 
		 * i is incremented, until the last two estimates differ by no more
		 * than the desired precision.
		 */
		do {
			if(i > 0)
				oldValue = t.get(i-1);
			else
				oldValue = Double.POSITIVE_INFINITY;
			
			// number of intervals
			int n = 1 << i;
			t.add(i, getIntegral(n, a, b, f));
			
			for(int k = i; k > 0; k--) {
				int c = 1 << (2*k); // c = 4^k
				double tNew = (c * t.get(k) - t.get(k-1)) / (c - 1);
				
				t.set(k - 1, tNew);
			}
			i++;
		} while(Math.abs(oldValue - t.get(0)) > precision);
		
		return t.get(0);
	}
	
	private static double getIntegral(int n, double a, double b, Function f) {
		double h = (b - a) / n;
		double y = 0.0;
		
		for(int i = 1; i < n; i++) {
			y += f.getValue(a + i*h);
		}
		
		y = f.getValue(a) + f.getValue(b) + 2 * y;
		
		return y * h / 2;
	}
}
