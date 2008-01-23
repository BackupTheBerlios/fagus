package math;


import static org.junit.Assert.*;

import org.junit.Test;

public class TestBinomial {
	@Test
	public void testBinomial() {
		long l;
		
		l = Binomial.binomial(0, 0);
		assertEquals(l, 1L);

		l = Binomial.binomial(20, 0);
		assertEquals(l, 1L);

		l = Binomial.binomial(20, 20);
		assertEquals(l, 1L);

		l = Binomial.binomial(4, 3);
		assertEquals(l, 4L);
		
		l = Binomial.binomial(2, 1);
		assertEquals(l, 2L);

		l = Binomial.binomial(5, 2);
		assertEquals(l, 10L);		
		
		l = Binomial.binomial(15, 12);
		assertEquals(l, 455L);
		
		l = Binomial.binomial(40, 12);
		assertEquals(l, 5586853480L);
		
		l = Binomial.binomial(40, 28);
		assertEquals(l, 5586853480L);
		
		l = Binomial.binomial(48, 12);
		assertEquals(l, 69668534468L);
	}
}
