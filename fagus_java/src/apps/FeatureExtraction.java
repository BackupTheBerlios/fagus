package apps;

import java.io.IOException;



import select.FeatureSelection;
import select.extract.ChernoffLinearDiscriminantAnalysis;
import select.extract.FisherLinearDiscriminantAnalysis;
import util.LibSVMVectorSetReader;
import util.LibSVMVectorSetWriter;
import util.VectorSet;

/**
 * Feature extraction application. This program uses linear
 * discriminant analysis to reduce the number of features.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class FeatureExtraction {

	private static void usage() {
		System.err.println("Usage: java apps.FeatureExtraction CRITERION ORIGINAL_FEATURES OUTPUT_FILE");
		System.err.println("  where CRITERION is one of:");
		System.err.println("    fisher");
		System.err.println("    chernoff [-FEATURES]");		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 3) {
			usage();
			System.exit(1);
		}
		
		String outputFile = args[args.length - 1];
		FeatureSelection selection = null;
		
		String criterion = args[0];
		VectorSet vectors = null;
		
		try {
			vectors = (new LibSVMVectorSetReader(args[args.length - 2])).parse();
		} catch(IOException e) {
			System.err.println("Cannot read input file: " + e.getMessage());
			System.exit(1);
		}
		
		if(criterion.equalsIgnoreCase("fisher")) {
//			selection = new GeneralizedDiscriminantAnalysis();
			selection = new FisherLinearDiscriminantAnalysis();
		} else if(criterion.equalsIgnoreCase("chernoff")) {
			int k;
			
			if(args.length == 4) {
				k = Integer.parseInt(args[1]);
				
				if(k < 0) {
					k = vectors.getDimension() + k;
				}
			} else {
				k = vectors.getClassDescriptors().size() - 1;
			}

			selection = new ChernoffLinearDiscriminantAnalysis(k);
		} else {
			System.err.println("Unknown criterion: " + criterion);
			usage();
			System.exit(1);
		}
		
		selection.initialize(vectors);
		LibSVMVectorSetWriter w = new LibSVMVectorSetWriter(outputFile);
		
		try {
			w.write(selection.getMappedData());
		} catch(IOException e) {
			System.out.println("Cannot write output file " + outputFile + ": " + e.getMessage());
		}
	}

}
