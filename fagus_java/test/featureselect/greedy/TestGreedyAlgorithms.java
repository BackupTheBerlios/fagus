package featureselect.greedy;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import reporting.SubsetSelectionLogger;
import select.subset.CriterionFunction;
import select.subset.greedy.BackwardSelection;
import select.subset.greedy.ForwardSelection;
import select.subset.greedy.NestedSubsetAlgorithm;
import select.subset.greedy.SequentialBackwardFloatingSearch;
import select.subset.greedy.SequentialForwardFloatingSearch;
import util.VectorSet;

public class TestGreedyAlgorithms {

	private static CriterionFunction[] f;
	private static double[] results;
	private static VectorSet vectors;
	private static final int N = 48;     // number of available feature	
	private static final int M = N - 16; // number of features to drop
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		f = new CriterionFunction[2];
		results = new double[2];
		
		f[0] = new CriterionFunction() {
			public void initialize(int dimension, VectorSet data) {}
			
			public double getCriterionValue() {
				return 0.0;
			}
			
			public double getCriterionValue(int[] features) {
				double sum = 0.0;
				
				for(int i = 0; i < features.length; i++) {
					sum += features[i] + 1;
				}
				
				return sum;
			}
		};
		results[0] = (N * (N + 1) - M * (M + 1)) / 2;
		
		f[1] = new CriterionFunction() {
			public void initialize(int dimension, VectorSet data) {}
			
			public double getCriterionValue() {
				return 0.0;
			}
			
			public double getCriterionValue(int[] features) {
				double sum = 0.0;
				
				for(int i = 0; i < features.length; i++) {
					sum += M - features[i];
				}
				
				return sum;
			}
		};
		results[1] = (M * (M + 1) - (N - M) * (N - M + 1)) / 2;
		
		vectors = new VectorSet(null, new String[N]);
	}
	
	@Test
	public void testForwardSearch() {
		System.out.println("Forward Search:");
		
		for(int i = 0; i < f.length; i++) {
			NestedSubsetAlgorithm alg = new ForwardSelection();
			alg.addObserver(new SubsetSelectionLogger());
			alg.run(vectors, f[i], M);

			int[] features = alg.getFeatureVector();

			assertEquals("iteration: " + i, results[i], (int)f[i].getCriterionValue(features));
			System.out.println();
		}
	}

	@Test
	public void testBackwardSearch() {
		System.out.println("Backward Search:");
		
		for(int i = 0; i < f.length; i++)
		{
			NestedSubsetAlgorithm alg = new BackwardSelection();
			alg.addObserver(new SubsetSelectionLogger());
			alg.run(vectors, f[i], M);

			int[] features = alg.getFeatureVector();

			assertEquals("iteration: " + i, results[i], (int)f[i].getCriterionValue(features));
			System.out.println();
		}
	}

	@Test
	public void testSequentialForwardFloatingSearch() {
		System.out.println("SFFS:");
		
		for(int i = 0; i < f.length; i++) {
			NestedSubsetAlgorithm alg = new SequentialForwardFloatingSearch();
			alg.addObserver(new SubsetSelectionLogger());
			alg.run(vectors, f[i], M);

			int[] features = alg.getFeatureVector();

			assertEquals("iteration: " + i, results[i], (int)f[i].getCriterionValue(features));
			System.out.println();
		}		
	}
	
	@Test
	public void testSequentialBackwardFloatingSearch() {
		System.out.println("SBFS:");
		
		for(int i = 0; i < f.length; i++) {
			NestedSubsetAlgorithm alg = new SequentialBackwardFloatingSearch();
			alg.addObserver(new SubsetSelectionLogger());
			alg.run(vectors, f[i], M);

			int[] features = alg.getFeatureVector();

			assertEquals("iteration: " + i, results[i], (int)f[i].getCriterionValue(features));
			System.out.println();
		}		
	}
}
