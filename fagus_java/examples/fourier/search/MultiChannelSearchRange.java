package fourier.search;

import java.util.Iterator;

/**
 * This class extends the SearchRange class for different channels.
 * While ring filters within the same channel must not overlap,
 * this class allows them to overlap for diffent channels.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class MultiChannelSearchRange implements Iterable<int[]> {
	private final SearchRange[] ranges;
	
	/**
	 * Create a new MultiChannelSearchRange for a given number of
	 * channels.
	 * 
	 * @param nChannels
	 * @param extent
	 * @param maxRingWidth
	 */
	public MultiChannelSearchRange(int nChannels, int extent, int maxRingWidth) {
		ranges = new SearchRange[nChannels];
		
		for(int i = 0; i < nChannels; i++) {
			ranges[i] = new SearchRange(extent, maxRingWidth);
		}
	}
	
	/**
	 * Add a new ring filter and exclude this interval from
	 * the search space.
	 * 
	 * @param channel
	 * @param radius
	 * @param width
	 */
	public void add(int channel, int radius, int width) {
		ranges[channel].add(radius, width);
	}
	
	/**
	 * Free an interval that has previously been added.
	 * 
	 * @param channel
	 * @param radius
	 */
	public void free(int channel, int radius) {
		ranges[channel].free(radius);
	}
	
	/**
	 * Test whether a given interval is free and may be
	 * added.
	 * 
	 * @param channel
	 * @param radius
	 * @param width
	 * @return
	 */
	public boolean isFree(int channel, int radius, int width) {
		return ranges[channel].isFree(radius, width);
	}
	
	/**
	 * Iterate over all filters that are available in this 
	 * search space. The iterator returns triples of the form
	 * (channel, radius, width)
	 */
	public Iterator<int[]> iterator() {
		return new MultiChannelSearchRangeIterator();
	}
	
	/*
	 * This class uses the iterator of the basic SearchRange.
	 * If the iterator of one channel is empty, the iterator
	 * switches to the next.
	 */
	private class MultiChannelSearchRangeIterator implements Iterator<int[]> {
		private Iterator<int[]> it;
		private int channel;
		
		public MultiChannelSearchRangeIterator() {
			channel = 0;
			it = ranges[channel].iterator();
		}
		
		public boolean hasNext() {
			while(!it.hasNext()) {
				if(channel == ranges.length - 1) {
					return false;
				}
				
				channel++;
				it = ranges[channel].iterator();
			}
			
			return true;
		}
		
		public int[] next() {
			int[] b = it.next();
			int[] buffer = {channel, b[0], b[1]};
			
			return buffer;
		}
		
		public void remove() {
		}
	}
}
