package util.io;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import select.FeatureSelection;
import util.FeatureScaler;

import classify.Classifier;

/**
 * This is the export system of this project.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class ModelWriter {
	private Classifier classifier;
	private FeatureSelection selection = null;
	private FeatureScaler scaler = null;
	
	/**
	 * Set the classifier that should be exported.
	 * 
	 * @param classifier
	 */
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}
	
	/**
	 * Add the feature selection that should be exported.
	 * 
	 * @param selection
	 */
	public void setSelection(FeatureSelection selection) {
		this.selection = selection;
	}
	
	/**
	 * Add the feature scaler that should be exported.
	 * 
	 * @param scaler
	 */
	public void setScaling(FeatureScaler scaler) {
		this.scaler = scaler;
	}
	
	/**
	 * Export the stored data to an XML file.
	 * 
	 * @param output the output file.
	 * @throws IOException
	 */
	public void write(String output) throws IOException {
		XMLExportVisitor visitor = new XMLExportVisitor();
		
		Method[] methods = classifier.getClass().getMethods();
		for(int i = 0; i < methods.length; i++) {
			Annotation a = methods[i].getAnnotation(Export.class);
			
			if(a != null && ((Export)a).value() == ModelType.CLASSIFIER) {
				try {
					methods[i].invoke(classifier, visitor);
				} catch(IllegalAccessException e) {
					throw new IOException("Cannot access export method of classifier: " + e.getMessage());
				} catch(InvocationTargetException e) {
					throw new IOException("Cannot invoke export method of classifier: " + e.getMessage());
				}
				
				break;
			}
		}
		
		if(selection != null) {
			methods = selection.getClass().getMethods();
			for(int i = 0; i < methods.length; i++) {
				Annotation a = methods[i].getAnnotation(Export.class);
				
				if(a != null && ((Export)a).value() == ModelType.FEATURE_SELECTION) {
					try {
						methods[i].invoke(selection, visitor);
					} catch(IllegalAccessException e) {
						throw new IOException("Cannot access export method of selection: " + e.getMessage());
					} catch(InvocationTargetException e) {
						throw new IOException("Cannot invoke export method of selection: " + e.getMessage());
					}

					break;
				}
			}
		}
		
		if(scaler != null) {
			methods = scaler.getClass().getMethods();
			for(int i = 0; i < methods.length; i++) {
				Annotation a = methods[i].getAnnotation(Export.class);
				
				if(a != null && ((Export)a).value() == ModelType.FEATURE_SCALING) {
					try {
						methods[i].invoke(scaler, visitor);
					} catch(IllegalAccessException e) {
						throw new IOException("Cannot access export method of selection: " + e.getMessage());
					} catch(InvocationTargetException e) {
						throw new IOException("Cannot invoke export method of selection: " + e.getMessage());
					}

					break;
				}
			}
		}
		
		SAXSource source = new SAXSource(visitor, null);
		StreamResult result = new StreamResult(new FileWriter(output));
		TransformerFactory tf = TransformerFactory.newInstance();
		
		try {
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
		} catch(TransformerConfigurationException e) {
			throw new IOException("Cannot create XML transformer: " + e.getMessage());
		} catch(TransformerException e) {
			throw new IOException("Cannot convert SAX stream to XML: " + e.getMessage());
		}
	}
	
}
