package util.io;

import java.io.FileWriter;
import java.io.IOException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import select.FeatureSelection;

import classify.Classifier;

/**
 * This is the export system of this project.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class ModelWriter {
	private Classifier classifier;
	private FeatureSelection selection = null;
	
	/**
	 * Set the classifier, which should be exported.
	 * 
	 * @param classifier
	 */
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}
	
	/**
	 * Add feature selection, which should be exported.
	 * 
	 * @param selection
	 */
	public void setSelection(FeatureSelection selection) {
		this.selection = selection;
	}
	
	/**
	 * Export the stored data to an XML file.
	 * 
	 * @param output the output file.
	 * @throws IOException
	 */
	public void write(String output) throws IOException {
		XMLExportVisitor visitor = new XMLExportVisitor();
		
		classifier.export(visitor);
		
		if(selection != null) {
			selection.export(visitor);
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
