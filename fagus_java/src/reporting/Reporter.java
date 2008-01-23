package reporting;

import java.io.OutputStream;

/**
 * Basic interface for creating a report out of log data.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface Reporter {
	
	/**
	 * Create a report out of logging data.
	 * 
	 * @param logger an object holding logging information.
	 * @param out the stream to print the report.
	 */
	void createReport(ClassificationLogger logger, OutputStream out);
}
