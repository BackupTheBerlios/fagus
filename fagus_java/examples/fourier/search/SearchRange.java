package fourier.search;

import java.util.Iterator;

/**
 * This class provides a way to keep track of the search
 * space in a Fourier filter search. The algorithm guarantees
 * that no two ring filters overlap, and that each filter stays
 * within the bounds of the Fourier coefficient matrix.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class SearchRange implements Iterable<int[]> {
	private final int extent; // the maximum value of 'radius + width'
	private final int maxRingWidth;
	/*
	 * The interval search tree is used to remember which
	 * filters may not be used. The excluded intervals are 
	 * stored for filters of width 1.
	 */
	private final IntervalSearchTree occupied;
	
	/**
	 * Create a new search range of a given extent. The 
	 * extent is the maximum value of radius + width. That
	 * is, for an N x N matrix, the extent is N/2.
	 * 
	 * @param extent
	 * @param maxRingWidth The maximum width of a ring filter.
	 */
	public SearchRange(int extent, int maxRingWidth) {
		this.extent = extent;
		this.maxRingWidth = maxRingWidth;
		
		occupied = new IntervalSearchTree();
	}
	
	
	/**
	 * Add a new interval. This interval is then occupied and
	 * is marked as excluded for further add operations.
	 * 
	 * @param radius
	 * @param width
	 */
	public void add(int radius, int width) {
		occupied.add(radius, radius + width - 1);
	}
	
	
	/**
	 * Free an interval that has previously been occupied.
	 * 
	 * @param radius The radius of the interval. 
	 */
	public void free(int radius) {
		occupied.delete(radius);
	}
	
	
	/**
	 * Test whether a given interval overlaps with any of the
	 * previously added ones.
	 * 
	 * @param radius
	 * @param width
	 * @return True, if the interval can be added.
	 */
	public boolean isFree(int radius, int width) {
		// get rid of bands that go beyond the search region.
		if( (width == 0) || (radius + width > extent) ) {
			return false;
		}
		
		return occupied.isFree(radius, radius + width - 1);
	}
	
	
	/**
	 * Iterate over all radius - width pairs that are available.
	 * Each of these filters can be added to the tree. Other
	 * filters overlap with any of the present ones. This iterator
	 * will return tuples (radius, width).
	 */
	public Iterator<int[]> iterator() {
		return new SearchRangeIterator();
	}
	
	/*
	 * Iterate over the interval search tree and subsequently
	 * return every element that does not interfere with the
	 * present filters.
	 */
	private class SearchRangeIterator implements Iterator<int[]> {
		private Iterator<int[]> list;
		private int occLow, occHigh, start;
		private int radius;
		private int width;
		private int nWidths;
		
		public SearchRangeIterator() {
			radius = 0;
			list = occupied.treeWalk().iterator();
			occHigh = -1;
			
			listStep();
		}
		
		public boolean hasNext() {
			if(radius <= occLow - width) {
				// use the next filter of the current ring width
				return true;
			}
			
			if(width < nWidths) {
				// use the next higher ring width
				radius = start;
				width++;
				return true;
			}

			do {
				// go to the next region between two occupied
				// intervals, if any
				if(occLow < extent) {
					listStep();
				} else {
					return false;
				}
			} while(width > nWidths);

			return true;
		}
		
		public int[] next() {
			int[] buffer = {radius, width};
			
			radius++;

			return buffer;
		}
		
		public void remove() { // not needed
		}
		
		// go to the next interval in the tree
		private void listStep() {
			start = occHigh + 1;
			width = 1;
			radius = start;
			
			if(list.hasNext()) {
				int[] e = list.next();
				occLow = e[0];

				// make sure that we stay within the 
				// given extent for filters of greater width
				if(e[1] > extent - width) {
					occHigh = extent - width;
				} else {
					occHigh = e[1];
				}
			} else {
				/* 
				 * The end has been reached:
				 * Set the bounds to some dummy values to be 
				 * able to return all filters that follow the
				 * last occupied interval.
				 */
				occLow = extent;
				occHigh = Integer.MAX_VALUE;
			}
			
			/*
			 * Determine, how many differnt ring widths can be
			 * used for the region between the last and the 
			 * next occupied interval.
			 */
			int gap = occLow - start;

			if(gap > maxRingWidth) {
				nWidths = maxRingWidth;
			} else {
				nWidths = gap;
			}
		}
	}
	
}
