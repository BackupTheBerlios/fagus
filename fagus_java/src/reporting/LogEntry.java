package reporting;

import util.ClassDescriptor;

/**
 * A simple structure holding a single logging entry.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class LogEntry {
	public final double[] vector;
	public final ClassDescriptor target;
	public final ClassDescriptor result;
	
	public LogEntry(double[] vector, ClassDescriptor target, ClassDescriptor result) {
		this.vector = vector;
		this.target = target;
		this.result = result;
	}

}
