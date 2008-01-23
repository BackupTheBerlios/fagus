package util;

import java.util.HashMap;
import java.util.Map;

/**
 * A very basic implementation of a ClassDescriptor. The
 * descriptor is identified by a label (i.e. a string).
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class DefaultClassDescriptor implements ClassDescriptor {
	/*
	 * This class uses the Singleton design pattern to make
	 * sure that class descriptors with the same label are
	 * indeed the same object. This prevents from expensive
	 * computation of hash codes and equality tests, which
	 * must be computed frequently. With the current design
	 * these methods act on the object's address.
	 */
	private final String label;
	private static Map<String, ClassDescriptor> classes;
	
	static {
		classes = new HashMap<String, ClassDescriptor>();
	}
	
	/**
	 * Create a new class descriptor with a given label.
	 * 
	 * @param label
	 */
	private DefaultClassDescriptor(String label) {
		this.label = label;
	}
		
	public String toString() {
		return label;
	}
	
	/**
	 * Get a new class descriptor with some label, or get a reference
	 * to an existing instance with the same label.
	 * 
	 * @param label
	 * @return
	 */
	public static ClassDescriptor getInstance(String label) {
		ClassDescriptor d;
	
		if(classes.containsKey(label)) {
			d = classes.get(label);
		} else {
			d = new DefaultClassDescriptor(label);
			classes.put(label, d);
		}
		
		return d;
	}
}
