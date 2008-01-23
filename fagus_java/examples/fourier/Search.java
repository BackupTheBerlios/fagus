package fourier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import classify.bayes.NormalMLEClassifier;
import classify.bayes.NormalRegularizedClassifier;
import fourier.search.FourierFeatureSelectionLogger;
import fourier.search.FourierSelectionComparator;
import fourier.search.FourierSearchSpace;
import fourier.search.RingGrouping;

import select.subset.CriterionFunction;
import select.subset.CrossValidationCriterion;
import select.subset.FeatureSubsetSelection;
import select.subset.greedy.ForwardSelection;
import select.subset.greedy.NestedSubsetAlgorithm;
import select.subset.greedy.SequentialForwardFloatingSearch;
import util.LibSVMVectorSetReader;
import util.LibSVMVectorSetWriter;
import util.VectorSet;
import util.VectorSetWriter;

/**
 * Search for a good configuration of ring filters.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class Search {
	private static final int nChannels = 3;
	private static final int maxRingWidth = 15;
	
	private static int extent;
	
	private static void usage() {
		System.err.println("usage: java Search ALGORITHM CRITERION NR_FEATURES INPUT_DATA OUTPUT_FILE");
		System.err.println("where");
		System.err.println("  ALGORITHM = \"greedy\" | \"sffs\"");
		System.err.println("  CRITERION = \"rda\" | \"bayes\"");
	}
	
	private static VectorSet getFullVectorSet(VectorSet singleRings) {
		List<int[]> configuration = new ArrayList<int[]>();
		
		for(int c = 0; c < nChannels; c++) {
			for(int r = 0; r < extent; r++) {
				for(int w = 1; (w <= maxRingWidth) && (r + w <= extent); w++) {
					int[] entry = {c, r, w};
					configuration.add(entry);
				}
			}
		}
		
		RingGrouping rg = new RingGrouping(singleRings, null, nChannels, extent * 2);
		rg.setConfiguration(configuration);
		
		return rg.getVectorSet();
	}
	
	public static void main(String[] args) {
		CriterionFunction criterion = null;
		NestedSubsetAlgorithm algorithm = null;
		
		if(args.length < 5) {
			usage();
			System.exit(1);
		}
		
		String algorithmName = args[0];
		if(algorithmName.equalsIgnoreCase("greedy")) {
			algorithm = new ForwardSelection();
		} else if(algorithmName.equalsIgnoreCase("sffs")) {
			algorithm = new SequentialForwardFloatingSearch();
		} else {
			System.err.println("Unknown search algorithm: " + algorithmName);
			System.exit(1);
		}
		
		String criterionName = args[1];
		if(criterionName.equalsIgnoreCase("rda")) {
			criterion = new CrossValidationCriterion(new NormalRegularizedClassifier(0.9));
		} else if(criterionName.equalsIgnoreCase("bayes")) {
			criterion = new CrossValidationCriterion(new NormalMLEClassifier());
		} else {
			System.err.println("Unknown criterion function: " + criterionName);
			System.exit(1);
		}
		
		int nFeatures = Integer.parseInt(args[2]);
		LibSVMVectorSetReader reader = new LibSVMVectorSetReader(args[3]);
		
		VectorSet orig = null;
		
		try{
			orig = reader.parse();
		} catch(IOException e) {
			System.err.println("Cannot read input file \"" + args[1] + "\": " + e.getMessage());
			System.exit(1);
		}
		
		extent = orig.getDimension()/nChannels;
		
		FourierSearchSpace featureSpace = new FourierSearchSpace(nChannels, extent, maxRingWidth);
		
		algorithm.setFeatureSpace(featureSpace);
		algorithm.addObserver(new FourierFeatureSelectionLogger(featureSpace));
		algorithm.setSelectionComparator(new FourierSelectionComparator(featureSpace));

		VectorSet data = getFullVectorSet(orig);

		/*
		 * If nFeatures is a positive number, the number of features
		 * is reduced to this number. If, on the other hand, the number
		 * is negative, the number of features is reduced by nFeatures.
		 */
		if(nFeatures > 0) {
			nFeatures = data.getDimension() - nFeatures;
		} else {
			nFeatures = -nFeatures;
		}

		FeatureSubsetSelection selection = new FeatureSubsetSelection(algorithm, criterion, nFeatures);
		selection.initialize(data);
		
		try {
			VectorSetWriter writer = new LibSVMVectorSetWriter(args[4]);
			writer.write(selection.getMappedData());
		} catch(IOException e) {
			e.printStackTrace();
		}

	}
	
}
