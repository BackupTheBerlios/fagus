package featureselect.greedy;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import reporting.SubsetSelectionLogger;
import select.subset.CriterionFunction;
import select.subset.greedy.BackwardSelection;
import select.subset.greedy.ForwardSelection;
import select.subset.greedy.NestedSubsetAlgorithm;
import util.VectorSet;

public class TestGreedyAlgorithms {

	private static CriterionFunction f;
	private static VectorSet vectors;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		f = new CriterionFunction() {
			public void initialize(int dimension, VectorSet data) {
				
			}
			
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
		
		vectors = new VectorSet(null, new String[48]);
	}
	
	@Test
	public void testForwardSearch() {
		System.out.println("Forward Search: ");
		NestedSubsetAlgorithm alg = new ForwardSelection();
		alg.addObserver(new SubsetSelectionLogger());
		alg.run(vectors, f, 32);
		
		int[] features = alg.getFeatureVector();

		assertEquals((int)f.getCriterionValue(features), 648);
		System.out.println();
	}

	@Test
	public void testBackwardSearch() {
		System.out.println("Backward Search: ");
		NestedSubsetAlgorithm alg = new BackwardSelection();
		alg.addObserver(new SubsetSelectionLogger());
		alg.run(vectors, f, 32);
		
		int[] features = alg.getFeatureVector();
		
		assertEquals((int)f.getCriterionValue(features), 648);
	}

}
