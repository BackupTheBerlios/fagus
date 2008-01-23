package reporting;

import java.util.List;
import java.util.ArrayList;

import util.ClassDescriptor;

/**
 * Create a report from classification log data.
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class ClassificationLogger {
	protected List<LogEntry> logList = new ArrayList<LogEntry>();
	
	/**
	 * Log a classification event.
	 * @param vector The classified vector.
	 * @param target The target value.
	 * @param result The actual value.
	 */
	public void log(double[] vector, ClassDescriptor target, ClassDescriptor result) {
		logList.add(new LogEntry(vector, target, result));
	}
	
	public List<LogEntry> getData() {
		return logList;
	}

}
