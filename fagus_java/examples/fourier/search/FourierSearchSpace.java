package fourier.search;

import java.util.Iterator;
import java.util.Observable;

import select.subset.greedy.CloneableFeatureSpace;
import select.subset.greedy.NestedSubsetAlgorithm;

/**
 * This class provides an abstraction of the MultiChannelSearchRange
 * that can be plugged into the feature selection library. Basically,
 * all that this class does is enumerating all possible outputs of
 * the search range iterator, which outputs triples of the form
 * channel-radius-width. The feature selection algorithms then just
 * deal with ordinary integer representations of the features.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class FourierSearchSpace implements CloneableFeatureSpace {
	private final MultiChannelSearchRange searchRange;
	private final int channelWidth;
	private final int nChannels;
	private final int extent;
	private final int maxRingWidth;
	
	/**
	 * Create a new FourierSearchSpace of a given size.
	 * 
	 * @param nChannels The number of color channels.
	 * @param extent The number of possible radii.
	 * @param maxRingWidth The maximal ring width.
	 */
	public FourierSearchSpace(int nChannels, int extent, int maxRingWidth) {
		this.searchRange = new MultiChannelSearchRange(nChannels, extent, maxRingWidth);
		this.nChannels = nChannels;
		this.extent = extent;
		this.maxRingWidth = maxRingWidth;

		// get the number of different filters in a single color channel
		channelWidth = maxRingWidth * (2 * extent - maxRingWidth + 1) / 2;
	}
	
	/**
	 * Update the search space. This method is a callback, which 
	 * gets invoked every time a feature is added or removed from
	 * the current candidate.
	 */
	public void update(Observable o, Object arg) {
		NestedSubsetAlgorithm.Operation op = (NestedSubsetAlgorithm.Operation)arg;
		
		int[] feature = decode(op.getFeature());
		
		if(op.isAdd()) {
			searchRange.free(feature[0], feature[1]);
		} else {
			searchRange.add(feature[0], feature[1], feature[2]);
		}
	}

	public Iterator<Integer> iterator() {
		return new FourierSearchSpaceIterator();
	}
	
	public CloneableFeatureSpace clone() throws CloneNotSupportedException {
		FourierSearchSpace newSearchSpace = new FourierSearchSpace(nChannels, extent, maxRingWidth);
		for(int[] i: searchRange) {
			newSearchSpace.searchRange.add(i[0], i[1], i[2]);
		}
		
		return newSearchSpace;
	}

	/**
	 * Encode a triple of the form channel-radius-width into some
	 * integer.
	 * @param data
	 * @return
	 */
	public int encode(int[] data) {
		/*
		 * The channel (data[0]) is encoded by
		 * 
		 * c = data[0] * channelWidth
		 * 
		 * The radius and width are encoded by enumerating 
		 * all rings of width 1, then all rings of width 2, and so on.
		 */
		return data[0] * channelWidth + (data[2] - 1) * (2 * extent - data[2] + 2) / 2 + data[1];
	}
	
	/**
	 * Decode an integer into a triple of the form channel-radius-width.
	 * @param i
	 * @return
	 */
	public int[] decode(int i) {
		int channel = 0;
		int width = 1;
		
		while(i >= channelWidth) {
			i -= channelWidth;
			channel++;
		}
		
		for(int w = 1; i > extent - w; w++) {
			i -= extent - w + 1;
			width++;
		}
		
		return new int[] {channel, i, width};
	}
	
	private class FourierSearchSpaceIterator implements Iterator<Integer> {
		private final Iterator<int[]> searchRangeIterator = searchRange.iterator();
		
		public boolean hasNext() {
			return searchRangeIterator.hasNext();
		}
		
		public Integer next() {
			return new Integer(encode(searchRangeIterator.next()));
		}
		
		public void remove() {
		}
	}
}
