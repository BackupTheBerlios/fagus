package fourier.image;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.media.jai.JAI;

/**
 * Write an image via the JAI (Java Advanced Imageing) library.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class JaiRGBWriter extends ImageWriter {
	
	public JaiRGBWriter(double[][] data) {
		super(data);
	}
	
	@Override
	public void write(String filename) throws IOException {
		BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		img.setRGB(0, 0, 256, 256, rgbData, 0, 256);
		
		JAI.create("filestore", img, filename, "PNG");
	}
}
