package fourier.search;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class TestIntervalSearchTree {

	List<int[]> testElements;
	
	@Before
	public void setUp() {
		testElements = new ArrayList<int[]>();

		// add 50 intervals [i, i+1]
		for(int low = 0; low < 100; low += 2) {
			testElements.add(new int[] {low, low+1});
		}
	}
	
	@Test
	public void testIST() {
		IntervalSearchTree ist = new IntervalSearchTree();
		int low;

		fillTree(ist);
		
		// test tree size
		List<int[]> inorder = ist.treeWalk();
		assertTrue(inorder.size() == 50);
		
		// test correctness and ordering of the tree
		int[] tmp = new int[100];
		int i = 0;
		for(int[] e: inorder) {
			tmp[i] = e[0];
			tmp[i+1] = e[1];
			i += 2;
		}
		
		for(i = 0; i < 100; i++) {
			assertTrue(tmp[i] == i);
		}
		
		// test border cases
		assertTrue(ist.isFree(100, 101));
		assertFalse(ist.isFree(99, 101));
		assertFalse(ist.isFree(40, 60));
		
		// delete 49 intervals
		for(low = 98; low > 0; low -= 2) {
			assertFalse(ist.isFree(low, low+1));
			ist.delete(low);
		}
		
		// test remaining interval
		inorder = ist.treeWalk();
		assertTrue(inorder.size() == 1);
		
		int[] elem = inorder.get(0);
		assertTrue(elem[0] == 0);
	}

	private void fillTree(IntervalSearchTree ist) {
		Random rng = new Random();
		List<int[]> elems = new LinkedList<int[]>(testElements);
		
		for(int size = elems.size(); size > 0; size--) {
			int[] next = elems.remove(rng.nextInt(size));
			
			ist.add(next[0], next[1]);
			System.out.println("add: " + next[0] + "," + next[1]);
		}
	}
}
