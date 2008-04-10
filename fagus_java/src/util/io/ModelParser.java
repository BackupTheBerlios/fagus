package util.io;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import util.ClassDescriptor;

/**
 * This is an interface for parsers of model files. For each
 * supported file format, a parser must support these methods.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface ModelParser {
	/**
	 * Get the class name of the classifier.
	 *
	 * @return the full class name (including packages).
	 */
	String getClassName();
	
	/**
	 * Get the class name of the selection algorithm.
	 * 
	 * @return the full class name (including packages), or null
	 * if no selection is used.
	 */
	String getSelectionClassName();
	
	/**
	 * Is a feature selection algorithm used?
	 * 
	 * @return
	 */
	boolean useSelection();
	
	/**
	 * Get the class name of the scaling algorithm.
	 * 
	 * @return the full class name (including packages), or null
	 * if no scaling is used.
	 */
	String getScalingClassName();
	
	/**
	 * Is a feature scaling algorithm used?
	 * 
	 * @return
	 */
	boolean useScaling();
	
	/**
	 * Get a map of class descriptors and class properties. The
	 * content of the properties depends on the classifier
	 * architecture.
	 * 
	 * @return
	 */
	Map<ClassDescriptor, Map<String, Object>> getClassData();
	
	/**
	 * Get the model properties.
	 * 
	 * @return
	 */
	Map<String, Object> getModelData();
	
	/**
	 * Get the selection properties (if any selection is used).
	 * 
	 * @return
	 */
	Map<String, Object> getSelectionData();
	
	/**
	 * Get the scaling properties (if any scaling is used).
	 * 
	 * @return
	 */
	Map<String, Object> getScalingData();
	
	/**
	 * Parse input data.
	 * 
	 * @param input
	 * @throws IOException
	 */
	void parse(Reader input) throws IOException;
}
