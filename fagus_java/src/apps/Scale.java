package apps;

import java.io.IOException;
import java.util.StringTokenizer;

import util.DefaultFeatureScaler;
import util.FeatureScaler;
import util.LibSVMVectorSetReader;
import util.LibSVMVectorSetWriter;
import util.VectorSet;
import util.VectorSetReader;
import util.VectorSetWriter;

/**
 * This is a tool for scaling classification data. It scales the
 * range of each feature to the interval [-1,1] or some user defined
 * interval. For some classifiers, such as k-NN or SVM, scaling of 
 * the data can improve classification results significantly. If 
 * unscaled data is used for these classifiers, features with higher 
 * magnitudes usually excel others. Therefore, discriminative 
 * information might be lost.
 *  
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class Scale {
	private static void usage() {
		System.err.println("usage: java apps.Scale [lower:upper] INPUT_FILE OUTPUT_FILE");
		System.err.println("  where 'lower' and 'upper' are real numbers setting the maximal");
		System.err.println("  and minimal value of a feature (default [-1,1])");
	}
	
	public static void main(String[] args) {
		int argp = 0;
		double lower = -1.0;
		double upper = 1.0;
		
		if(args.length < 2) {
			usage();
			System.exit(1);
		}
		
		if(args.length > 2) {
			StringTokenizer st = new StringTokenizer(args[argp++], ":");
			
			// the format is 'lower:upper'
			if(st.countTokens() != 2) {
				System.err.println("Invalid interval\n");
				usage();
				System.exit(1);
			}
			
			lower = Double.parseDouble(st.nextToken());
			upper = Double.parseDouble(st.nextToken());
		}
		
		VectorSetReader r = new LibSVMVectorSetReader(args[argp++]);
		VectorSet vectors = null;
		
		try {
			vectors = r.parse();
		} catch(IOException e) {
			System.err.println("Cannot read input file: " + e.getMessage());
			System.exit(1);
		}
		
		FeatureScaler scaler = new DefaultFeatureScaler(vectors);
		scaler.scale(vectors, lower, upper);
		
		VectorSetWriter w = new LibSVMVectorSetWriter(args[argp++]);
		try {
			w.write(vectors);
		} catch(IOException e) {
			System.err.println("Cannot write output: " + e.getMessage());
			System.exit(1);
		}
	}
}
