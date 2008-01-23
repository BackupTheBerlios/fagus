package fourier.search;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TestSearchRange {
	SearchRange range;
	List<Integer>[] lists;	
	
	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		range = new SearchRange(8, 4);

		lists = new List[4];
		for(int i = 0; i < lists.length; i++) {
			lists[i] = new ArrayList<Integer>(8);
		}
	}

	@Test
	public void testIterator() {
		range.add(2, 2);
		
		
		lists[0].add(0);
		lists[0].add(1);
		lists[0].add(4);
		lists[0].add(5);
		lists[0].add(6);
		lists[0].add(7);
		lists[1].add(0);
		lists[1].add(4);
		lists[1].add(5);
		lists[1].add(6);
		lists[2].add(4);
		lists[2].add(5);
		lists[3].add(4);
		
		check();
		
		range.add(5, 2);
		
		lists[0].add(0);
		lists[0].add(1);
		lists[0].add(4);
		lists[0].add(7);
		lists[1].add(0);
		
		check();
		
		range.free(2);
		
		lists[0].add(0);
		lists[0].add(1);
		lists[0].add(2);
		lists[0].add(3);
		lists[0].add(4);
		lists[0].add(7);
		lists[1].add(0);
		lists[1].add(1);
		lists[1].add(2);
		lists[1].add(3);
		lists[2].add(0);
		lists[2].add(1);
		lists[2].add(2);
		lists[3].add(0);
		lists[3].add(1);
	
		check();
	}
	
	private void check() {
		for(int[] e: range) {
			Integer i = new Integer(e[0]);
			assertTrue("Element (" + e[0] + "," + e[1] + ") missing", lists[e[1] - 1].contains(i));
			
			lists[e[1] - 1].remove(i);
		}
		
		for(int i = 0; i < lists.length; i++) {
			assertTrue("List " + i + " not empty", lists[i].isEmpty());
		}		
	}

}
