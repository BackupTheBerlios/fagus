package fourier.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import select.subset.CriterionFunction;
import fourier.transform.RingFilter;
import util.ClassDescriptor;
import util.VectorSet;

/**
 * This class is used to obtain rings of arbitrary size from 
 * rings of width 1.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class RingGrouping {
	private final ClassDescriptor[] classMapping;
	private final double[][] vectorMapping;
	private final int nVectors;
	private final int channelLength;
	private final int[] ringPixels;
	
	private VectorSet candidate;
	
	public RingGrouping(VectorSet orig, CriterionFunction f, int nChannels, int maxExtent) {
		nVectors = orig.getData().size();
		
		classMapping = new ClassDescriptor[nVectors];
		vectorMapping = new double[nVectors][];
		
		int k = 0;
		Map<double[], ClassDescriptor> data = orig.getData();
		
		for(double[] v: data.keySet()) {
			vectorMapping[k] = v;
			classMapping[k] = data.get(v);
			k++;
		}
		
		int dimension = orig.getDimension();
		channelLength = dimension / nChannels;
		
		RingFilter filter = new RingFilter(maxExtent);
		ringPixels = filter.getNumberOfPixels();
	}
	
	/**
	 * Set a configuration, discarding the current one.
	 * @param configuration a list of triples (channel, radius, width)
	 */
	public void setConfiguration(List<int[]> configuration) {
		double[][] tmp = new double[nVectors][configuration.size()];
		for(int i = 0; i < nVectors; i++) {
			int j = 0;
			
			for(int[] c: configuration) {
				tmp[i][j] = group(vectorMapping[i], c[0], c[1], c[2]);
				j++;
			}
		}
		
		candidate = new VectorSet(getMapping(tmp), getLabels(configuration.size()));
	}
	
	/**
	 * Get the resulting vectors for the current configuration.
	 * @return
	 */
	public VectorSet getVectorSet() {
		return candidate;
	}
	
	
	private Map<double[], ClassDescriptor> getMapping(double[][] data) {
		Map<double[], ClassDescriptor> map = new HashMap<double[], ClassDescriptor>();
		
		for(int i = 0; i < nVectors; i++) {
			map.put(data[i], classMapping[i]);
		}
		
		return map;
	}
	
	
	private String[] getLabels(int n) {
		String[] labels = new String[n];
		for(int i = 0; i < n; i++) {
			labels[i] = Integer.toString(i + 1);
		}
		
		return labels;
	}
	
	
	private double group(double[] singleRings, int channel, int radius, int width) {
		int offset = channel * channelLength + radius;
		double sum = 0;
		int n = 0;
		
		for(int i = 0; i < width; i++) {
			sum += singleRings[offset + i];
			n += ringPixels[radius + i];
		}
		
		return sum / n;
	}
}
