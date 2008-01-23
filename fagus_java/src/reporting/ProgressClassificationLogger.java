package reporting;

import util.ClassDescriptor;

public class ProgressClassificationLogger extends ClassificationLogger {
	private final int sampleSize;
	private int status;
	private ProgressBar bar;
	
	public ProgressClassificationLogger(int sampleSize) {
		this.sampleSize = sampleSize;
		status = 0;
		bar = new ProgressBar();
	}
	
	@Override
	public void log(double[] vector, ClassDescriptor target, ClassDescriptor result) {
		super.log(vector, target, result);
		status++;
		
		// TODO: use Observer interface for status bar.
		bar.setStatus((double)status/sampleSize);
		bar.print();
	}
}
