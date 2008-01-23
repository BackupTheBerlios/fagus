package fourier.search;

import java.util.List;
import java.util.LinkedList;

/**
 * This class is an augmented version of a standard binary
 * search tree. Each node contains a closed interval
 * 
 * [low, high]
 * 
 * where 'low' acts as the sorting key. This tree does not
 * allow for overlapping intervals. This means that no two
 * intervals can have the same lower index.
 *
 * <br><br>
 * See Chapter 14.3 of
 *     T. Cormen, C. Leiserson, R. Rivest, and C. Stein
 *     "Introduction to Algorithms"
 *     2nd edition
 *     MIT Press, 2001
 */
public class IntervalSearchTree {
	private Entry root;

	public IntervalSearchTree() {
		root = null;
	}


	/**
	 * Inorder traversal of the tree.
	 * 
	 * @return
	 */
	public List<int[]> treeWalk() {
		LinkedList<int[]> list = new LinkedList<int[]>();
		
		inorderWalk(root, list);
	
		return list;
	}


	/**
	 * Add an interval to the tree.
	 * 
	 * @param low the lower index of the interval.
	 * @param high the higher index of the interval.
	 */
	public void add(int low, int high) {
		Entry y = null; // parent node
		Entry x = root;

		Entry z = new Entry(low, high);

		while(x != null) {
			y = x;

			if(low < x.low) {
				x = x.left;
			} else {
				x = x.right;
			}
		}

		z.parent = y;

		if(y == null) {
			root = z;
		} else if(low < y.low) {
			y.left = z;
		} else {
			y.right = z;
		}
	}

	
	/**
	 * Delete an interval that was previously added to the tree.
	 * 
	 * @param low the lower index of the interval.
	 */
	public void delete(int low) {
		Entry x, y;
		Entry z = search(low);

		assert(z != null);

		// find the replacement node for z
		if( (z.left == null) || (z.right == null) ) {
			y = z;
		} else {
			y = successor(z);
		}

		// find successor x of the replacement node
		if(y.left != null) {
			x = y.left;
		} else {
			x = y.right;
		}

		// splice out replacement node
		if(x != null) {
			x.parent = y.parent;
		}

		if(y.parent == null) {
			root = x;
		} else if(y == y.parent.left) {
			y.parent.left = x;
		} else {
			y.parent.right = x;
		}

		// replace node z with replacement node y
		if(y != z) {
			z.low = y.low;
			z.high = y.high;
		}
			
	}

	/**
	 * Check, if some interval is free, or if it overlaps with some
	 * already present interval.
	 * 
	 * @param low the lower index of the occupied interval.
	 * @param high the higher index of the occupied interval.
	 * @return true if the interval does not overlap with any other interval.
	 */
	public boolean isFree(int low, int high) {
		Entry x = root;
		
		while(x != null) {
			/*
			 * If the intervals overlap each other, return false.
			 * See CLR Chapter 14.3 pp. 311
			 */
			if( (x.low <= high) && (low <= x.high) ) {
				return false;
			}

			/*
			 * This implementation is slightly different than
			 * the one in the book. We don't use the 'max' field
			 * here, because intervals cannot overlap in this
			 * implementation.
			 */
			if(x.low >= low) {
				x = x.left;
			} else {
				x = x.right;
			}
		}
		
		return true;
	}

	
	/*
	 * Travese the tree in inorder.
	 */
	private void inorderWalk(Entry x, LinkedList<int[]> list) {
		if(x != null) {
			// take the left
			inorderWalk(x.left, list);

			// process data
			int[] e = {x.low, x.high};
			list.add(e);
			
			// take the right
			inorderWalk(x.right, list);
		}
	}

	
	/*
	 * Search for some interval. The lower bound
	 * acts as the search key.
	 */
	private Entry search(int low) {
		Entry x = root;
		
		while( (x != null) && (low != x.low) ) {
			if(low < x.low) {
				x = x.left;
			} else {
				x = x.right;
			}
		}
		
		return x;
	}


	/*
	 * Get the element with the next higher key, if any.
	 */
	private Entry successor(Entry x) {
		/*
		 * If the right subtree contains any element,
		 * we take the leftmost node in this tree.
		 */
		if(x.right != null) {
			x = x.right;

			while(x.left != null) {
				x = x.left;
			}
			
			return x;
		}

		/*
		 * Otherwise, we walk toward the root
		 * and take the first node y, such that
		 * x is a descendant of y's left subtree.
		 */
		Entry y = x.parent;

		while( (y != null) && (x == y.right) ) {
			x = y;
			y = y.parent;
		}

		return y;
	} 


	/*
	 * This class holds the actual data, while at the
	 * same time representing a node in the interval
	 * tree.
	 */
	private class Entry {
		public Entry left, right, parent;
		public int low, high;
		
		public Entry(int low, int high) {
			this.low = low;
			this.high = high;
			left = right = parent = null;
		}
	}

}
