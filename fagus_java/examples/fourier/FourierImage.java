package fourier;

import fourier.transform.FastFourierTransform;
import fourier.transform.LogTransform;
import fourier.transform.NormalizeTransform;
import fourier.transform.ShiftTransform;
import fourier.image.Channel;
import fourier.image.ImageReader;
import fourier.image.ImageWriter;
import fourier.image.JaiRGBReader;
import fourier.image.JaiRGBWriter;

/**
 * Create an image of the Fourier transform in PNG format.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class FourierImage {
	public static void main(String[] args) throws Exception {
		String inFile, outFile;
		
		if(args.length < 2) {
			System.err.println("usage: java FourierImage INPUT_IMAGE OUTPUT_IMAGE");
			System.exit(1);
		}
		
		inFile = args[0];
		outFile = args[1];
		
		ImageReader r = new JaiRGBReader();
		r.read(inFile);
		
		FastFourierTransform fft = new FastFourierTransform(r.getHeight(), r.getWidth());
		double[][] data = fft.transform(r.getChannel(Channel.RED));
		
		data = ShiftTransform.transform(NormalizeTransform.transform(LogTransform.transform(data)));
		
		ImageWriter w = new JaiRGBWriter(data);
		w.write(outFile);
	}
}
