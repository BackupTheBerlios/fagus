package fourier.image;

import java.io.IOException;

/**
 * Abstract class for storing images.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public abstract class ImageWriter {
	protected final int[] rgbData;
	
	/**
	 * Create a new writer for a given set of pixel intensities.
	 * 
	 * @param buffer
	 */
	public ImageWriter(double[][] buffer) {
		/*
		 * Convert to RGB data.
		 */
		rgbData = new int[buffer.length * buffer[0].length];
		
		int i = 0;
		for(int j = 0; j < buffer.length; j++) {
			for(int k = 0; k < buffer[j].length; k++) {
				int n = (int)(buffer[j][k] * 255);
				rgbData[i++] = ((n << 16) | (n << 8) | n);
			}
		}
	}
	
	/**
	 * Perform the actual write operation.
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public abstract void write(String filename) throws IOException;
}
