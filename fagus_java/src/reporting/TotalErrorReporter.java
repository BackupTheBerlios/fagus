package reporting;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Report the total error.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class TotalErrorReporter implements Reporter {

	public void createReport(ClassificationLogger logger, OutputStream out) {

		PrintStream p = new PrintStream(out);
		p.println();
		p.printf("%.4f %% correctly classified\n", getSuccessRate(logger) * 100.0);
	}

	/**
	 * Get the total number of successfully classified vectors.
	 * @param logger an object holding logging information.
	 * @return
	 */
	public static int getSuccessNumber(ClassificationLogger logger) {
		int classifiedTrue = 0;

		for(LogEntry entry: logger.getData()) {
			if(entry.result.equals(entry.target)) {
				classifiedTrue++;
			}
		}

		return classifiedTrue;
	}
	
	/**
	 * Get the success rate neglecting co-class errors.
	 * @param logger an object holding logging information.
	 * @return a value in [0,1]
	 */
	public static double getSuccessRate(ClassificationLogger logger) {
		return (double)getSuccessNumber(logger) / logger.getData().size();
	}
	
	public static double getErrorRate(ClassificationLogger logger) {
		return 1.0 - getSuccessRate(logger);
	}

}
