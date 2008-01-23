package util.io;

/**
 * This is the common interface of the export, i.e. serialization, system.
 * The newParametersInstance method should be used to create new
 * parameter data structures.
 *  
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface ExportVisitor {
	
	/**
	 * Set the model class and its parameters.
	 * 
	 * @param className the name of the Java class of the 
	 *                  classifier implementation.
	 * @param params the parameters for the classifier.
	 */
	void setModel(String className, Parameters params);
	
	/**
	 * Set the feature selection class and its parameters.
	 * 
	 * @param className the name of the Java class of the
	 *                  feature selection implementation.
	 * @param params the parameters for the feature selection.
	 */
	void setSelection(String className, Parameters params);
	
	/**
	 * Add a class to the model.
	 * 
	 * @param label a unique (string) identifier for the class.
	 * @param params class-specific parameters.
	 */
	void addClass(String label, Parameters params);
	
	/**
	 * Create a new instance of a Parameters data structure.
	 * 
	 * @return
	 */
	Parameters newParametersInstance();
	
	/**
	 * This is a simple datastructure that can hold all
	 * the parameters allowed by the grammar.
	 */
	public interface Parameters {
		void setParameter(String key, String value);
	
		void setParameter(String key, double[] vector);
	
		void setParameter(String key, double[][] matrix);
	
		void setParameter(String key, int[] indexSet);
	}
}
