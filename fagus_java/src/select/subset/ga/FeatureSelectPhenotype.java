package select.subset.ga;

import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import select.subset.CriterionFunction;

import evSOLve.JEvolution.PermChromosome;
import evSOLve.JEvolution.Phenotype;

/**
 * The phenotype of an individual is the subset of features. This
 * is encoded in an integer chromosome. The fitness is determined
 * by the criterion value of the current subset.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class FeatureSelectPhenotype implements Phenotype {
	private final CriterionFunction f;
	private final int maxFeatures;
	
	private int[] features;
	private double fitness;
	
	/**
	 * Create a new phenotype.
	 * 
	 * @param f the criterion function used to determine the fitness.
	 * @param maxFeatures the maximum number of features in an individual.
	 */
	public FeatureSelectPhenotype(CriterionFunction f, int maxFeatures) {
		this.f = f;
		this.maxFeatures = maxFeatures;
	}
	
	
	public void calcFitness() {
		fitness = f.getCriterionValue(features);
	}

	
	public void doOntogeny(Vector genotype) {
		features = getFeatures(genotype, maxFeatures);
	}

	
	public double getFitness() {
		return fitness;
	}
	
	
	public Object clone() {
		FeatureSelectPhenotype clone = new FeatureSelectPhenotype(f, maxFeatures);
		
		clone.features = features;
		clone.fitness = fitness;
		
		return clone;
	}

	
	/**
	 * Extract the feature subset from the genotype.
	 * 
	 * @param genotype
	 * @return
	 */
	public static int[] getFeatures(Vector genotype, int maxFeatures) {
		PermChromosome chrom = (PermChromosome)genotype.get(0);
		Vector info = (Vector)chrom.getBases();
		
		TreeSet<Integer> t = new TreeSet<Integer>();
		
		for(int i = 0; i < maxFeatures; i++) {
			t.add((Integer)info.get(i));
		}
		
		int[] result = new int[t.size()];
		Iterator<Integer> it = t.iterator();
		
		for(int i = 0; i < result.length; i++) {
			result[i] = it.next();
		}
		
		return result;		
	}
}
