package fourier.image;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

/**
 * Read an image using the JAI (Java Advanced Imageing) library.
 * This library is not part of the Java SDK and must be included
 * in the CLASSPATH.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class JaiRGBReader extends ImageReader {
	private int[] rgbData;
	
	@Override
	public void read(String filename) throws IOException {
		RenderedOp imgOp = JAI.create("fileload", filename);
		
		height = imgOp.getHeight();
		width = imgOp.getWidth();
		
		BufferedImage image = imgOp.getAsBufferedImage();
		rgbData = image.getRGB(0, 0, width, height, null, 0, width);
	}
	
	@Override
	public int[][] getChannel(Channel ch) {
		int[][] result = new int[height][width];
		int i = 0;
		
		switch(ch) {
		case RED:
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					result[y][x] = (rgbData[i++] >> 16) & 0xFF;
				}
			}
			break;
			
		case GREEN:
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					result[y][x] = (rgbData[i++] >> 8) & 0xFF;
				}
			}
			break;
			
		case BLUE:
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					result[y][x] = rgbData[i++] & 0xFF;
				}
			}
			break;
			
		case GRAY:
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					double r = (rgbData[i] >> 16) & 0xFF;
					double g = (rgbData[i] >> 8) & 0xFF;
					double b = (rgbData[i]) & 0xFF;
					i++;
					
					// use Y channel of YUV
					result[y][x] = (int)(0.299 * r + 0.587 * g + 0.114 * b);
				}
			}
			break;
		}
		
		
		return result;
	}
}
