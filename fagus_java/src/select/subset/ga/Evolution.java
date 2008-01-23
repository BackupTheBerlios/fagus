package select.subset.ga;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import select.subset.CriterionFunction;
import select.subset.SelectionAlgorithm;
import util.VectorSet;
import evSOLve.JEvolution.JEvolution;
import evSOLve.JEvolution.JEvolutionException;
import evSOLve.JEvolution.JEvolutionReporter;
import evSOLve.JEvolution.PermChromosome;
import evSOLve.JEvolution.TournamentSelection;

/**
 * This is an implementation of a genetic algorithm for feature
 * subset selection. This algorithm can be configured by editing
 * the &quot;resources/Evolution.properties&quot; file.
 * 
 * The genetic algorithm uses a permutation chromosome, which has
 * been suggested by
 * 
 * H.A. Mayer, P. Somol, R. Huber, and P. Pudil
 * "Improving Statistical Measures of Feature Subsets by 
 * Conventional and Evolutionary Apporaches"
 * Joint IAPR International Workshops SSPR 2000 and SPR 2000
 * Alicante, Spain 2000
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class Evolution implements SelectionAlgorithm {
	private int[] bestFeature;
	
	private final int maxGenerations;
	private final int populationSize;
	private final int crossoverPoints;
	private final int tournamentSize;
	
	private final static int DEFAULT_MAX_GENERATIONS = 400;
	private final static int DEFAULT_POPULATION_SIZE = 400;
	private final static int DEFAULT_CROSSOVER_POINTS = 2;
	private final static int DEFAULT_TOURNAMENT_SIZE = 2;
	

	/**
	 * Create a new genetic algorithm and parse the configuration
	 * data.
	 */
	public Evolution() {
		Properties prop = new Properties();
		
		try {
			InputStream in = new FileInputStream("resources" + File.separator 
					+ "Evolution.properties");
			
			prop.load(in);
		} catch(IOException e) {
		}
			
		if(prop.containsKey("maxGenerations")) {
			this.maxGenerations = 
				Integer.parseInt(prop.getProperty("maxGenerations"));
		} else {
			this.maxGenerations = DEFAULT_MAX_GENERATIONS;
		}
			
		if(prop.containsKey("populationSize")) {
			this.populationSize = 
				Integer.parseInt(prop.getProperty("populationSize"));
		} else {
			this.populationSize = DEFAULT_POPULATION_SIZE;
		}
			
		if(prop.containsKey("crossoverPoints")) {
			this.crossoverPoints = 
				Integer.parseInt(prop.getProperty("crossoverPoints"));
		} else {
			this.crossoverPoints = DEFAULT_CROSSOVER_POINTS;
		}
			
		if(prop.containsKey("tournamentSize")) {
			this.tournamentSize = 
				Integer.parseInt(prop.getProperty("tournamentSize"));
		} else {
			this.tournamentSize = DEFAULT_TOURNAMENT_SIZE;
		}
	}
	
	
	/**
	 * Run the genetic algorithm. The algorithm will stop, if the maximal
	 * number of generations have been reached.
	 */
	public void run(VectorSet vectors, CriterionFunction f, int dropNrFeatures) {
		final int dimension = vectors.getDimension();
		final int targetSize = dimension - dropNrFeatures;
		
		f.initialize(dimension, vectors);
		
		JEvolution evolution = new JEvolution();
		JEvolutionReporter reporter = evolution.getJEvolutionReporter();
		evolution.setMaximization(true); // maximization problem
		
		PermChromosome chromosome = new PermChromosome();
		
		try {
			chromosome.setLength(dimension);
			chromosome.setMutationRate(0.01);
			chromosome.setCrossoverRate(0.6);
			chromosome.setCrossoverPoints(crossoverPoints);
			
			evolution.addChromosome(chromosome);
			evolution.setPhenotype(new FeatureSelectPhenotype(f, targetSize));
			evolution.setSelection(new TournamentSelection(tournamentSize));
			evolution.setMaximalGenerations(maxGenerations);
			evolution.setPopulationSize(populationSize);
		} catch(JEvolutionException e) {
			e.printStackTrace();
		}
		
		evolution.doEvolve();
		
		bestFeature = FeatureSelectPhenotype.getFeatures(reporter.getBestIndividual().getGenotype(), targetSize);
	}

	
	/**
	 * Get the best feature subset of all generations.
	 */
	public int[] getFeatureVector() {
		return bestFeature;
	}
}
