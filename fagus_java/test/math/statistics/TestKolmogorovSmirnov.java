package math.statistics;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestKolmogorovSmirnov {
	
	// n is taken from a uniform distribution in [1,200]
	private static final int[] n =
		{14,      16,      22,      32,      36,      39,      46,      66,      67,      75,      79,      85,      147,     149,     156,     159,     163,     180,     185,     191};
	
	// p is taken from a uniform distribution in [0,1]
	private static final double[] p =
		{0.636,   0.362,   0.501,   0.364,   0.346,   0.856,   0.413,   0.231,   0.427,   0.765,   0.411,   0.681,   0.650,   0.145,   0.440,   0.146,   0.978,   0.655,   0.910,   0.065};
	
	private static final double[] k =
		{0.67050, 0.43659, 0.55694, 0.44839, 0.43491, 0.95815, 0.49302, 0.34295, 0.50833, 0.83226, 0.49657, 0.73840, 0.71116, 0.26675, 0.52553, 0.26819, 1.36758, 0.71736, 1.08495, 0.17179};


	@Test
	public void testLookup() {
		double v;
		
		for(int i = 0; i < n.length; i++) {
			v = KolmogorovSmirnovTest.getK(n[i], p[i]);
			assertEquals(k[i], v, 0.05);
		}
	}
	
	@Test
	public void testInverseLookup() {
		double v;
		
		for(int i = 0; i < n.length; i++) {
			v = KolmogorovSmirnovTest.getP(n[i], k[i]);
			assertEquals(p[i], v, 0.01);
		}		
	}

}
