package apps;

import java.io.IOException;

import classify.bayes.NormalMLEClassifier;
import reporting.ProgressBar;
import reporting.SubsetSelectionLogger;
import select.subset.BhattacharyyaDistance;
import select.subset.ChernoffCriterion;
import select.subset.CriterionFunction;
import select.subset.CrossValidationCriterion;
import select.subset.FeatureSubsetSelection;
import select.subset.FisherClassSeparabilityCriterion;
import select.subset.SelectionAlgorithm;
import select.subset.bnb.BranchAndBound;
import select.subset.bnb.FastBranchAndBound;
import select.subset.bnb.RecursiveBranchAndBound;
import select.subset.es.ExhaustiveSearch;
import select.subset.ga.Evolution;
import select.subset.greedy.ForwardSelection;
import select.subset.greedy.NestedSubsetAlgorithm;
import select.subset.greedy.OscillatingSearch;
import select.subset.greedy.SequentialForwardFloatingSearch;
import util.LibSVMVectorSetReader;
import util.LibSVMVectorSetWriter;
import util.VectorSet;
import util.VectorSetWriter;

/**
 * This is the feature selection's main program. It will import 
 * data from a given directory, reduce the dimensions of vectors 
 * by a given size, and write the filtered data to a given output
 * directory. The criterion function to use can either be set 
 * manually, or can be left as a choice to the program. For two
 * class problems a Recursive Branch &amp; Bound algorithm using
 * the Bhattacharyya distance will be run. For a multi class problem,
 * the Fisher criterion and a Fast Branch &amp; Bound will be used.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class SubsetSelection {

	private static void usage() {
		System.err.println("Usage: java apps.SubsetSelection ALGORITHM [CRITERION] -FEATURES ORIGINAL_FEATURES OUTPUT_FILE");
		System.err.println("  where ALGORITHM is one of:");
		System.err.println("    bnb       : Branch and bound search");
		System.err.println("    exhaustive: Exhaustive search");
		System.err.println("    sffs      : Sequencial forward floating search");
		System.err.println("    greedy    : Greedy forward selection");
		System.err.println("    ga        : Genentic algorithm");
		System.err.println("    oscillate : Oscillating search");
		System.err.println();
		System.err.println("  and CRITERION is one of:");
		System.err.println("    fisher");
		System.err.println("    bhattacharyya");
		System.err.println("    chernoff");
		System.err.println("    bayes");
	}
	
	private static void printReport(int[] features, VectorSet orig) {
		System.out.print("[ ");
		for(int i = 0; i < features.length; i++) {
			System.out.print(orig.getFeatureLabel(features[i]) + " ");
		}
		System.out.println("]");
	}
	
	
	private static SelectionAlgorithm getSelector(String algorithm) {
		SelectionAlgorithm selector = null;
		
		if(algorithm.equalsIgnoreCase("bnb")) {
			BranchAndBound bnb = new FastBranchAndBound();
			bnb.addObserver(new ProgressBar());
			selector = bnb;
		} else if(algorithm.equalsIgnoreCase("exhaustive")) {
			ExhaustiveSearch es = new ExhaustiveSearch();
			es.addObserver(new ProgressBar());
			selector = es;
		} else if(algorithm.equalsIgnoreCase("sffs")) {
			NestedSubsetAlgorithm alg = new SequentialForwardFloatingSearch();
			alg.addObserver(new SubsetSelectionLogger());
			selector = alg;
		} else if(algorithm.equalsIgnoreCase("greedy")) {
			NestedSubsetAlgorithm alg = new ForwardSelection();
			alg.addObserver(new SubsetSelectionLogger());
			selector = alg;
		} else if(algorithm.equalsIgnoreCase("ga")) {
			selector = new Evolution();
		} else if(algorithm.equalsIgnoreCase("oscillate")) {
			NestedSubsetAlgorithm alg = new OscillatingSearch();
			alg.addObserver(new SubsetSelectionLogger());
			selector = alg;
		} else {
			usage();
			System.exit(1);
		}
		
		return selector;
	}

	public static void main(String[] args) {
		int argp = 0;

		if(args.length < 3) {
			usage();
			System.exit(1);
		}
		
		String algorithm = args[argp++];
		
		String criterion = null;
		
		if(args.length > 4) {
			criterion = args[argp++];
		}
		
		// number of features to keep or drop
		int k = Integer.parseInt(args[argp++]);
		
		// input data
		VectorSet vectorSet = null;
		try {
			vectorSet = (new LibSVMVectorSetReader(args[argp++])).parse();
		} catch(IOException e) {
			System.err.println("Cannot read input file: " + e.getMessage());
			System.exit(1);
		}
		
		if(k > 0) {
			k = vectorSet.getDimension() - k;
		} else {
			k = -k;
		}
		
		SelectionAlgorithm selector = null;
		CriterionFunction f = null;
		
		/*
		 * The bhattacharyya distance gives better results, but is only applicable
		 * for a two-class problem. The fisher criterion, on the other hand, cannot
		 * be computed recursively.
		 */
		if(criterion == null) {
			if(vectorSet.isTwoClassProblem()) {
				if(algorithm.equalsIgnoreCase("bnb")) {
					BranchAndBound bnb = new RecursiveBranchAndBound();
					bnb.addObserver(new ProgressBar());
					selector = bnb;
				}
				
				f = new BhattacharyyaDistance();
			} else {
				f = new FisherClassSeparabilityCriterion();
			}
		} else if(criterion.equalsIgnoreCase("bhattacharyya")) {
			if(vectorSet.isTwoClassProblem()) {
				if(algorithm.equalsIgnoreCase("bnb")) {
					// TODO: Recursive B&B would be faster, but apperently
					//       it does not find the optimal subset.
					//BranchAndBound bnb = new RecursiveBranchAndBound();
					BranchAndBound bnb = new FastBranchAndBound();
					bnb.addObserver(new ProgressBar());
					selector = bnb;
				}
				
				f = new BhattacharyyaDistance();
			} else {
				System.err.println("Cannot use Bhattacharyya bound for more than 2 classes");
				System.exit(1);
			}
		} else if(criterion.equalsIgnoreCase("fisher")) {
			f = new FisherClassSeparabilityCriterion();
		} else if(criterion.equalsIgnoreCase("chernoff")) {
			f = new ChernoffCriterion();
		} else if(criterion.equalsIgnoreCase("bayes")) {
			f = new CrossValidationCriterion(new NormalMLEClassifier());
		} else {
			System.err.println("Unknown criterion function: " + criterion + "\n");
			usage();
			System.exit(1);
		}
		
		// if the algorithm has not been set, set it now
		if(selector == null) {
			selector = getSelector(algorithm);
		}
		
		FeatureSubsetSelection fss = new FeatureSubsetSelection(selector, f, k);
		
		fss.initialize(vectorSet);
		
		System.out.println();
		
		// print report
		printReport(selector.getFeatureVector(), vectorSet);
		
		try {
			VectorSetWriter writer = new LibSVMVectorSetWriter(args[argp]);
			writer.write(fss.getMappedData());
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

}
