package util.io;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import select.FeatureSelection;

import classify.Classifier;

/**
 * Read a classification model and initialize the classification
 * software.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class ModelReader {
	private Classifier classifier = null;
	private FeatureSelection selection = null;
	
	
	/**
	 * Try to parse an input file.
	 * 
	 * @param input
	 * @throws IOException
	 */
	public void read(String input) throws IOException {
		ModelParser parser = null;
		
		if(input.endsWith(".xml")) {
			parser = new XMLModelParser();
		} else {
			throw new IOException("Unknown file name extension of model file");
		}
		
		parser.parse(new FileReader(input));
		initialize(parser);
	}
	
	public Classifier getClassifier() {
		return classifier;
	}
	
	/**
	 * Determine, whether feature selection is used.
	 * 
	 * @return
	 */
	public boolean needsSelection() {
		return selection != null;
	}
	
	public FeatureSelection getSelection() {
		return selection;
	}
	
	private void initialize(ModelParser parser) throws IOException {
		try {
			Class clazz = Class.forName(parser.getClassName());
			Method m = clazz.getMethod("newInstance", Map.class, Map.class);
			classifier = (Classifier)m.invoke(null, parser.getModelData(), parser.getClassData());
			
			if(parser.useSelection()) {
				clazz = Class.forName(parser.getSelectionClassName());
				m = clazz.getMethod("newInstance", Map.class);
				selection = (FeatureSelection)m.invoke(null, parser.getSelectionData());
			}
			
		} catch(ClassNotFoundException e) {
			throw new IOException("Model of XML file is not in classpath: " + e.getMessage());
		} catch(NoSuchMethodException e) {
			throw new IOException("Classifier does not provide instantiation: " + e.getMessage());
		} catch(Exception e) {
			throw new IOException("Instantiation of classifier failed: " + e.getMessage());
		}
	}
}
