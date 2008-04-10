package util.io;

import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import select.FeatureSelection;
import util.FeatureScaler;

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
	private FeatureScaler scaling = null;
	
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
	
	public boolean needsScaling() {
		return scaling != null;
	}
	
	public FeatureScaler getScaling() {
		return scaling;
	}
	
	private void initialize(ModelParser parser) throws IOException {
		try {
			Class clazz = Class.forName(parser.getClassName());
			
			/*
			 * Search for a method with annotation "Import" present.
			 */
			Method[] methods = clazz.getMethods();
			for(int i = 0; i < methods.length && classifier == null; i++) {
				Annotation a = methods[i].getAnnotation(Import.class);
				
				/*
				 * Check if annotation really specifies an Import method for
				 * a classifier.
				 */
				if(a != null && ((Import)a).value() == ModelType.CLASSIFIER) {
					classifier = (Classifier)methods[i].invoke(null, parser.getModelData(), 
							parser.getClassData());
					
				}
			}
			if(classifier == null) {
				throw new IOException("Classifier does not provide instantiation method");
			}
			
			if(parser.useSelection()) {
				clazz = Class.forName(parser.getSelectionClassName());
				
				methods = clazz.getMethods();
				for(int i = 0; i < methods.length && selection == null; i++) {
					Annotation a = methods[i].getAnnotation(Import.class);

					/*
					 * Check if annotation really specifies an Import method
					 * for a feature selection algorithm.
					 */
					if(a != null && ((Import)a).value() == ModelType.FEATURE_SELECTION) {
						selection = (FeatureSelection)methods[i].invoke(null, parser.getSelectionData());						
					}
				}
				if(selection == null) {
					throw new IOException("Selection does not provide instantiation method");
				}
			}
			
			if(parser.useScaling()) {
				clazz = Class.forName(parser.getScalingClassName());
				
				methods = clazz.getMethods();
				for(int i = 0; i < methods.length && scaling == null; i++) {
					Annotation a = methods[i].getAnnotation(Import.class);

					/*
					 * Check if annotation really specifies an Import method
					 * for a feature scaling method.
					 */
					if(a != null && ((Import)a).value() == ModelType.FEATURE_SCALING) {
						scaling = (FeatureScaler)methods[i].invoke(null, parser.getScalingData());						
					}
				}
				if(scaling == null) {
					throw new IOException("Scaling does not provide instantiation method");
				}
			}

		} catch(ClassNotFoundException e) {
			throw new IOException("Model of XML file is not in classpath: " + e.getMessage());
		} catch(Exception e) {
			throw new IOException("Instantiation of classifier failed: " + e.getMessage());
		}
	}
}
