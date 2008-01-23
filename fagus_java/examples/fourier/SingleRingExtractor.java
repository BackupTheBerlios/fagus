package fourier;

import fourier.image.Channel;
import fourier.image.ImageReader;
import fourier.image.JaiRGBReader;
import fourier.transform.FastFourierTransform;
import fourier.transform.RingFilter;
import fourier.transform.ShiftTransform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Extract all ring filters of width one from the Fourier domain.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class SingleRingExtractor {
	private static FastFourierTransform fft;
	private static int size;
	
	private static void usage() {
		System.err.println("usage: java SingleRingExtractor INPUT_DIR OUTPUT_FILE");
	}
	
	private static ImageReader getImage(String input) {
		ImageReader r = new JaiRGBReader();
		
		try {
			r.read(input);
		} catch(Exception e) {
			System.err.println("Cannot read image: " + e.getMessage());
			System.exit(1);
		}
		
		return r;
	}
	
	private static double[] getVector(ImageReader r, Channel ch) {
		double[] result = new double[size];
		
		RingFilter f = new RingFilter(256);
		
		int[][] data = r.getChannel(ch);
		double[][] fftData = ShiftTransform.transform(fft.transform(data));
		
		for(int i = 0; i < size; i++) {
			result[i] = f.apply(fftData, i);
		}

		return result;
	}
	
	/*
	 * Write data to an .libsvm file. The format of each line is
	 * as follows:
	 * 
	 * CLASS_LABEL { ' ' FEATURE_INDEX ':' FEATURE } '\n'
	 * 
	 * The first entry is a class label. The other entries are
	 * pairs of index and feature separated by a colon. The entries
	 * themselves are separated by blanks.
	 */
	private static void export(Map<double[][], String> data, String filename) throws IOException {
		PrintStream ps = new PrintStream(new FileOutputStream(filename));
		
		for(double[][] v: data.keySet()) {
			StringBuilder b;
			
			b = new StringBuilder();
			b.append(data.get(v));
			
			int k = 1;
			for(int i = 0; i < v.length; i++) {
				for(int j = 0; j < v[i].length; j++) {
					b.append(' ');
					b.append(k++);
					b.append(':');
					b.append(v[i][j]);
				}
			}

			ps.println(b.toString());
		}
		
		ps.close();
	}
	
	
	public static void main(String[] args) {
		if(args.length < 2) {
			usage();
			System.exit(1);
		}
		
		int classLabel = 1;
		
		Map<double[][], String> data = new HashMap<double[][], String>();
		
		File inputDir = new File(args[0]);
		if(!inputDir.isDirectory()) {
			System.err.println("Input directory is not a directory: " + inputDir);
			System.exit(1);
		}
		
		/*
		 * The directory hirarchy is as follows:
		 * The input directory contains subdirectories, where
		 * each of these holds the images of one class.
		 */
		for(File subdir: inputDir.listFiles()) {
			if(!subdir.isDirectory()) {
				System.err.println("Class directory is not a directory: " + subdir.getName());
				System.exit(1);
			}				
			
			String labelStr = Integer.toString(classLabel);
			System.out.println("Class " + labelStr + ": " + subdir.getName());
			
			for(String fileName: subdir.list()) {
				String imgName = subdir.getAbsolutePath() + File.separator + fileName;
				ImageReader r = null;
				try {
					r = getImage(imgName);
				} catch(Exception e) {
					System.out.println(imgName);
				}
				
				// initialize FFT processor
				if(fft == null) {
					int width = r.getWidth();
					int height = r.getHeight();
					
					/*
					 * We assume that height and width are equal,
					 * and that the dimensions of all images are
					 * the same. Moreover the widht/height must 
					 * be a power of 2.
					 */
					assert(width == height);
					fft = new FastFourierTransform(height, width);
					size = height / 2;
				}
				
				double[][] v = {
						getVector(r, Channel.RED),
						getVector(r, Channel.GREEN),
						getVector(r, Channel.BLUE)
				};
				
				data.put(v, labelStr);
			}
			
			classLabel++;
		}
		
		try {
			export(data, args[1]);
		} catch(IOException e) {
			System.err.println("Cannot write output file: " + e.getMessage());
			System.exit(1);
		}
	}
}
