package reporting;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import util.ClassDescriptor;

/**
 * This class is able to create reports including a confusion matrix. That 
 * is a matrix of classification targets and classification results.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class ConfusionMatrixReporter implements Reporter {

	private final Iterable<ClassDescriptor> classes;
	
	/**
	 * Create a new reporter for a given set of class descriptors.
	 * 
	 * @param classes the set of classes known by this reporter.
	 */
	public ConfusionMatrixReporter(Iterable<ClassDescriptor> classes) {
		this.classes = classes;
	}
	
	public void createReport(ClassificationLogger logger, OutputStream out) {
		PrintStream p = new PrintStream(out);
		Map<ClassDescriptor, Map<ClassDescriptor, Double>> results;
		
		results = getConfusionMatrix(logger);
		
		p.println("      \\ classified as");
		p.print("target \\  ");
		
		for(ClassDescriptor d: classes) {
			p.printf("%-7s ", d.toString());
		}
		
		p.println("\n--------------------------------------------------------------------------------");
		
		for(ClassDescriptor target: classes) {
			p.printf("%-7s | ", target.toString());
			
			for(ClassDescriptor result: classes) {
				p.printf("%-7.2f ", results.get(target).get(result) * 100);
			}
			
			p.println();
		}
	}
	
	private Map<ClassDescriptor, Map<ClassDescriptor, Double>> getConfusionMatrix(ClassificationLogger logger) {
		Map<ClassDescriptor, Map<ClassDescriptor, Double>> result =
			new HashMap<ClassDescriptor, Map<ClassDescriptor, Double>>();
		Map<ClassDescriptor, Integer> classSizes =
			new HashMap<ClassDescriptor, Integer>();
		
		/* Initialize map */
		for(ClassDescriptor d: classes) {
			HashMap<ClassDescriptor, Double> entry = new HashMap<ClassDescriptor, Double>();

			for(ClassDescriptor d1: classes) {
				entry.put(d1, 0.0);
			}
			
			result.put(d, entry);
			classSizes.put(d, 0);
		}
		
		/* parse log data */
		for(LogEntry entry: logger.getData()) {
			Double d = result.get(entry.target).get(entry.result);
			result.get(entry.target).put(entry.result, d + 1);
			
			int n = classSizes.get(entry.target);
			classSizes.put(entry.target, n + 1);
		}
		
		/* divide by class size */
		for(ClassDescriptor t: classes) {
			double size = classSizes.get(t);
			
			for(ClassDescriptor r: classes) {
				Double d = result.get(t).get(r);
				result.get(t).put(r, d / size);
			}
		}
		
		return result;
	}

}
