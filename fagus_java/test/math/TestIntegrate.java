package math;

import static org.junit.Assert.*;
import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

public class TestIntegrate {
	private static final Function gauss = new Function() {
		public double getValue(double x) {
			return 1.0/Math.sqrt(2 * Math.PI) * Math.exp(- x*x/2.0);
		}
	};
	
	private static final Function sin = new Function() {
		public double getValue(double x) {
			return Math.sin(x);
		}
	};
	
	private static final Function cos = new Function() {
		public double getValue(double x) {
			return Math.cos(x);
		}
	};
	
	private static final Function ln = new Function() {
		public double getValue(double x) {
			return 1.0/x;
		}
	};
	
	// the integral of this function is atan(x)
	private static final Function atan = new Function() {
		public double getValue(double x) {
			return 1.0 / (x*x + 1);
		}
	};
	
	@Test
	public void testIntegrate() {
		double v;
		
		v = Integrate.integrate(gauss, -1, 1, 0.0001);
		assertEquals(0.6826895, v, 0.0002);
		
		// -cos(Pi) + cos(0) 
		v = Integrate.integrate(sin, 0, Math.PI, 0.0001);
		assertEquals(2.0, v, 0.0002);

		// sin(-Pi) - sin(Pi)
		v = Integrate.integrate(cos, -Math.PI, Math.PI, 0.0001);
		assertEquals(0.0, v, 0.0002);
		
		// ln(e) - ln(1)
		v = Integrate.integrate(ln, 1, Math.E, 0.0001);
		assertEquals(1.0, v, 0.0002);
		
		// atan(1) = Pi/4
		v = Integrate.integrate(atan, 0, 1, 0.0001);
		assertEquals(Math.PI/4.0, v, 0.0002);
		
		v = Integrate.integrate(sin, 0, 18*Math.PI, 0.001);
		assertEquals(0.0, v, 0.0002);
	}
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestIntegrate.class);
	}

}
