package util.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;


/**
 * This ExportVisitor can be used to generate XML output. It
 * is an implementation of an XMLReader and generates SAX events,
 * which can be passed on to an XMLTransformer.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class XMLExportVisitor implements ExportVisitor, XMLReader {
	private ContentHandler contentHandler;
	private ErrorHandler errHandler;
	
	private final String namespace = "http://www.cosy.sbg.ac.at/wavelab/ClassificationModel";
	private final String rootElem = "model";
	
	private XMLModelParameters modelParams;
	private XMLModelParameters selectionParams = null;
	private Map<String, XMLModelParameters> classes;
	private String modelClass;
	private String selectionClass = null;
	
	
	public XMLExportVisitor() {
		classes = new HashMap<String, XMLModelParameters>();
	}
	
	public Parameters newParametersInstance() {
		return new XMLModelParameters();
	}

	public void addClass(String label, Parameters params) {
		classes.put(label, (XMLModelParameters)params);
	}

	public void setModel(String className, Parameters params) {
		modelClass = className;
		modelParams = (XMLModelParameters)params;
	}

	public void setSelection(String className, Parameters params) {
		selectionClass = className;
		selectionParams = (XMLModelParameters)params;
	}

	public ContentHandler getContentHandler() {
		return contentHandler;
	}

	public void setContentHandler(ContentHandler contentHandler) {
		this.contentHandler = contentHandler;
	}

	public ErrorHandler getErrorHandler() {
		return errHandler;
	}

	public void setErrorHandler(ErrorHandler errHandler) {
		this.errHandler = errHandler;	
	}
	
	public void parse(InputSource ignore) throws IOException, SAXException {
		if(contentHandler == null) {
			throw new SAXException("No content handler");
		}
		
		contentHandler.startDocument();
		
		AttributesImpl atts = new AttributesImpl();
		atts.addAttribute(namespace, "class", "class", "string", modelClass);
		contentHandler.startElement(namespace, rootElem, rootElem, atts);
		
		modelParams.parse();
		
		if(selectionClass != null) {
			atts = new AttributesImpl();
			atts.addAttribute(namespace, "class", "class", "string", selectionClass);
			contentHandler.startElement(namespace, "selection", "selection", atts);
			
			selectionParams.parse();
			
			contentHandler.endElement(namespace, "selection", "selection");
		}
		
		for(String cl: classes.keySet()) {
			atts = new AttributesImpl();
			atts.addAttribute(namespace, "label", "label", "string", cl);
			contentHandler.startElement(namespace, "class", "class", atts);
			
			classes.get(cl).parse();
			
			contentHandler.endElement(namespace, "class", "class");
		}
		
		contentHandler.endElement(namespace, rootElem, rootElem);
		
		contentHandler.endDocument();
	}
	
	/*
	 * Ignore these methods. They are just for compatibility
	 * with the interface definition.
	 */
	public DTDHandler getDTDHandler() {return null;}
	public void setDTDHandler(DTDHandler dtdHandler) {}
	public EntityResolver getEntityResolver() {return null;}
	public void setEntityResolver(EntityResolver entityResolver) {}
	public Object getProperty(String name) {return null;}
	public void setProperty(String name, Object value) {}
	public boolean getFeature(String name) {return false;}
	public void setFeature(String name, boolean value) {}
	public void parse(String systemId) throws IOException, SAXException {}
	
	
	private class XMLModelParameters implements ExportVisitor.Parameters {
		private Map<String, String> stringParams = new HashMap<String, String>();
		private Map<String, double[]> vectorParams = new HashMap<String, double[]>();
		private Map<String, double[][]> matrixParams = new HashMap<String, double[][]>();
		private Map<String, int[]> indexSetParams = new HashMap<String, int[]>();
		
		public void setParameter(String key, String value) {
			stringParams.put(key, value);
		}
		
		public void setParameter(String key, double[] vector) {
			vectorParams.put(key, vector);
		}
	
		public void setParameter(String key, double[][] matrix) {
			matrixParams.put(key, matrix);
		}
	
		public void setParameter(String key, int[] indexSet) {
			indexSetParams.put(key, indexSet);
		}
		
		public void parse() throws SAXException {
			Attributes nullAtts = new AttributesImpl();
			
			for(String key: stringParams.keySet()) {
				AttributesImpl atts = new AttributesImpl();
				atts.addAttribute(namespace, "id", "id", "string", key);
				atts.addAttribute(namespace, "value", "value", "string", stringParams.get(key));
				contentHandler.startElement(namespace, "param", "param", atts);
				
				contentHandler.endElement(namespace, "param", "param");
			}
			
			for(String key: vectorParams.keySet()) {
				AttributesImpl atts = new AttributesImpl();
				atts.addAttribute(namespace, "id", "id", "string", key);
				contentHandler.startElement(namespace, "complexParam", "complexParam", atts);
				
				double[] v = vectorParams.get(key);
				
				atts = new AttributesImpl();
				atts.addAttribute(namespace, "size", "size", "integer", Integer.toString(v.length));
				contentHandler.startElement(namespace, "vector", "vector", atts);
				
				for(int i = 0; i < v.length; i++) {
					String s = Double.toString(v[i]);

					contentHandler.startElement(namespace, "elem", "elem", nullAtts);
					contentHandler.characters(s.toCharArray(), 0, s.length());
					contentHandler.endElement(namespace, "elem", "elem");
				}
				
				contentHandler.endElement(namespace, "vector", "vector");
				contentHandler.endElement(namespace, "complexParam", "complexParam");
			}
			
			for(String key: indexSetParams.keySet()) {
				AttributesImpl atts = new AttributesImpl();
				atts.addAttribute(namespace, "id", "id", "string", key);
				contentHandler.startElement(namespace, "complexParam", "complexParam", atts);
				
				int[] v = indexSetParams.get(key);
				
				atts = new AttributesImpl();
				atts.addAttribute(namespace, "size", "size", "integer", Integer.toString(v.length));
				contentHandler.startElement(namespace, "indexSet", "indexSet", atts);
				
				for(int i = 0; i < v.length; i++) {
					String s = Integer.toString(v[i]);

					contentHandler.startElement(namespace, "elem", "elem", nullAtts);
					contentHandler.characters(s.toCharArray(), 0, s.length());
					contentHandler.endElement(namespace, "elem", "elem");
				}
				
				contentHandler.endElement(namespace, "indexSet", "indexSet");
				contentHandler.endElement(namespace, "complexParam", "complexParam");				
			}
			
			for(String key: matrixParams.keySet()) {
				AttributesImpl atts = new AttributesImpl();
				atts.addAttribute(namespace, "id", "id", "string", key);
				contentHandler.startElement(namespace, "complexParam", "complexParam", atts);
				
				double[][] v = matrixParams.get(key);
				
				atts = new AttributesImpl();
				atts.addAttribute(namespace, "rows", "rows", "integer", Integer.toString(v.length));
				atts.addAttribute(namespace, "cols", "cols", "integer", Integer.toString(v[0].length));
				contentHandler.startElement(namespace, "matrix", "matrix", atts);
				
				for(int i = 0; i < v.length; i++) {
					contentHandler.startElement(namespace, "row", "row", nullAtts);
					
					for(int j = 0; j < v[i].length; j++) {
						String s = Double.toString(v[i][j]);

						contentHandler.startElement(namespace, "elem", "elem", nullAtts);
						contentHandler.characters(s.toCharArray(), 0, s.length());
						contentHandler.endElement(namespace, "elem", "elem");
					}
					
					contentHandler.endElement(namespace, "row", "row");
				}
				
				contentHandler.endElement(namespace, "matrix", "matrix");
				contentHandler.endElement(namespace, "complexParam", "complexParam");
			}
		}

	}
}
