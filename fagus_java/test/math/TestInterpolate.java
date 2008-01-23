package math;

import static org.junit.Assert.*;
import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;

public class TestInterpolate {
	private static double[][] normal = {
		{1.00,    1.02,    1.05,    1.10,    1.15,    1.20,    1.22},
		{0.84134, 0.84613, 0.85314, 0.86433, 0.87493, 0.88493, 0.88877}
	};
	
	private final static double minX = 0.0;
	private final static double maxX = 10.0;
	private final static int N = 20;
	
	private double[] xs;
	private double[][] exp;  // e^x
	private double[][] sinc; // sin(x)/x	
	
	@Before
	public void setUp() throws Exception {
		double d = (maxX - minX)/N;
		double offset = minX;
		
		xs = new double[N];
		
		// set up uniformly distributed test points
		for(int i = 0; i < xs.length; i++) {
			xs[i] = Math.random() * (maxX - minX) + minX;
		}
		
		exp = new double[2][20];
		
		for(int i = 0; i < 20; i++) {
			double x = offset + i*d;
			exp[0][i] = x;
			exp[1][i] = Math.exp(x);
		}
		
		sinc = new double[2][20];
		sinc[0][0] = 0.0;
		sinc[1][0] = 1.0;
		
		for(int i = 1; i < 20; i++) {
			double x = offset + i*d;
			sinc[0][i] = x;
			sinc[1][i] = Math.sin(x)/x;			
		}
	}
	
	@Test
	public void testPolynomialInterpolate() {
		double v;
		
		v = Interpolate.polynomialInterpolate(normal[0], normal[1], 1.04);
		assertEquals(0.85083, v, 0.0001);
		
		v = Interpolate.polynomialInterpolate(normal[0], normal[1], 1.07);
		assertEquals(0.85769, v, 0.0001);
		
		v = Interpolate.polynomialInterpolate(normal[0], normal[1], 1.18);
		assertEquals(0.88100, v, 0.0001);
		
		for(int i = 0; i < N; i++) {
			v = Interpolate.polynomialInterpolate(exp[0], exp[1], xs[i]);
			assertEquals(Math.exp(xs[i]), v, 0.0001);
		}
		
		for(int i = 0; i < N; i++) {
			v = Interpolate.polynomialInterpolate(sinc[0], sinc[1], xs[i]);
			assertEquals(Math.sin(xs[i])/xs[i], v, 0.0001);
		}
		
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestInterpolate.class);
	}
}
