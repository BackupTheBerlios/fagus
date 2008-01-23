package fourier.image;

import java.io.IOException;

/**
 * Abstract class for reading images from files.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public abstract class ImageReader {
	protected int height = -1;
	protected int width = -1;

	/**
	 * Read an image.
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public abstract void read(String filename) throws IOException;
	
	/**
	 * Get a specific color channel of an image.
	 * 
	 * @param ch The channel to extract.
	 * @return An array holding the pixel intensities of that channel.
	 */
	public abstract int[][] getChannel(Channel ch);
	
	/**
	 * Get the hight of this image.
	 * @return
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Get the width of this image.
	 * @return
	 */
	public int getWidth() {
		return width;
	}
}
