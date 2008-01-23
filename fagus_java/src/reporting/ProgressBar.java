package reporting;

import java.util.Observable;
import java.util.Observer;

/**
 * This class provides a progress bar for the command line.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class ProgressBar implements Observer {
	private final static int DEFAULT_LINE_WIDTH = 80;
	private final int lineWidth;
	private double status;
	
	public ProgressBar() {
		this(DEFAULT_LINE_WIDTH);
	}
	
	public ProgressBar(int lineWidth) {
		this.lineWidth = lineWidth - 10;
		status = 0.0;
		print();
	}
	
	/**
	 * Set the status of the bar.
	 * @param status a value greater or equal to zero and 
	 *               smaller or equal to one.
	 */
	public void setStatus(double status) {
		this.status = status;
	}
	
	/**
	 * This is the asynchronous observer function to
	 * update and print the state. It is invoked by some
	 * Observable object each time this object changed
	 * its state. The argument should be a double value
	 * between 0.0 and 1.0.
	 */
	public void update(Observable o, Object arg) {
		double newStatus = (Double)arg;
		
		if( (newStatus - status) > 0.0005 ) {
			/*
			 * Updating and reprinting the status bar on every
			 * notification event might add a severe performace
			 * penalty.
			 */
			setStatus(newStatus);
			print();
		}
	}
	
	public void print() {
		StringBuilder buf = new StringBuilder("|");
		
		int i;
		for(i = 0; i < status * lineWidth; i++) {
			buf.append('=');
		}
		
		for(; i < lineWidth; i++) {
			buf.append(' ');
		}
		
		buf.append('|');
		
		System.out.printf("\r%s %.1f%%", buf.toString(), status * 100.0);
		System.out.flush();
	}
}
