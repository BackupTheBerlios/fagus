package math;

import static org.junit.Assert.*;
import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

public class TestRootSearch {

	private static final Function sinc = new Function() {
		public double getValue(double x) {
			if(x == 0) {
				return 1.0;
			}
			return Math.sin(x)/x;
		}
	};
	
	private static final Function cos = new Function() {
		public double getValue(double x) {
			return Math.cos(x);
		}
	};
	
	private static final Function ln = new Function() {
		public double getValue(double x) {
			return Math.log(x);
		}
	};
	
	private static final Function quadr = new Function() {
		public double getValue(double x) {
			return x*x - 3;
		}
	};
	
	@Test
	public void testBisection() {
		double v;
		
		v = RootSearch.bisection(quadr, 0, 10, 0.0001);
		assertEquals(Math.sqrt(3), v, 0.0002);
		
		v = RootSearch.bisection(sinc, -5, 0, 0.0001);
		assertEquals(-Math.PI, v, 0.0002);
		
		v = RootSearch.bisection(sinc, 5, 9, 0.0001);
		assertEquals(2*Math.PI, v, 0.0002);
		
		v = RootSearch.bisection(ln, 0.1, Double.MAX_VALUE, 0.0001);
		assertEquals(1.0, v, 0.0002);		

		v = RootSearch.bisection(cos, -Math.PI, 1.5, 0.0001);
		assertEquals(-Math.PI/2.0, v, 0.0002);
	}

	@Test
	public void testNewton() {
		double v;
		
		v = RootSearch.newton(quadr, 10, 0.0001);
		assertEquals(Math.sqrt(3), v, 0.0002);

		v = RootSearch.newton(sinc, 4, 0.0001);
		assertEquals(Math.PI, v, 0.0002);

		v = RootSearch.newton(sinc, 5, 0.0001);
		assertEquals(2*Math.PI, v, 0.0002);
		
		// start values >= e will fail
		v = RootSearch.newton(ln, Math.E - 0.1, 0.0001);
		assertEquals(1.0, v, 0.0002);
		
		v = RootSearch.newton(ln, Math.E + 0.1, 0.0001);
		assertTrue(Double.isNaN(v));

		v = RootSearch.newton(cos, 0.5, 0.0001);
		assertEquals(Math.PI/2.0, v, 0.0002);
	}
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestRootSearch.class);
	}	

}
