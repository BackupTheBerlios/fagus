package util.io;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import util.ClassDescriptor;
import util.DefaultClassDescriptor;

/**
 * This is a parser for models stored in XML format. The parser
 * uses the SAX API. The grammar file (XML schema) can be found at
 * 
 * resources/xml/ClassificationModel.xsd
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class XMLModelParser extends DefaultHandler implements ModelParser {
	/*
	 * This enum holds the valid parser states. Note that there
	 * are three states for the "elem" tag, since this tag can
	 * appear in three different data structures.
	 */
	private enum ParserState {
		MODEL, 
		PARAM, 
		COMPLEX_PARAM, 
		SELECTION,
		SCALING,
		CLASS,
		MATRIX, 
		MATRIX_ROW,
		MATRIX_ELEM,
		VECTOR, 
		VECTOR_ELEM,
		INDEX_SET,
		INDEX_ELEM
	};

	/*
	 * Temporary variables and data structures for parsing.
	 */
	private Stack<ParserState> stateStack;
	private Stack<Map<String, Object>> paramStack;
	private String complexId;
	private Object complexObject;
	private ClassDescriptor classDescr;
	private MatrixData matrixData;
	private VectorData vectorData;
	private IndexData indexData;
	private Exception exception;
	private StringBuilder buffer; // for character data

	/*
	 * Parsed information.
	 */
	private Map<ClassDescriptor, Map<String, Object>> classes = null;
	private Map<String, Object> model = null;
	private Map<String, Object> selection = null;
	private Map<String, Object> scaling = null;
	private String className = null;
	private String selectionClassName = null;
	private String scalingClassName = null;
	
	
	public String getClassName() {
		return className;
	}
	
	
	public String getSelectionClassName() {
		return selectionClassName;
	}
	
	
	public boolean useSelection() {
		return selection != null;
	}
	
	
	public String getScalingClassName() {
		return scalingClassName;
	}
	
	
	public boolean useScaling() {
		return scaling != null;
	}
	
	
	public Map<ClassDescriptor, Map<String, Object>> getClassData() {
		return classes;
	}
	
	
	public Map<String, Object> getModelData() {
		return model;
	}
	
	
	public Map<String, Object> getSelectionData() {
		return selection;
	}
	
	
	public Map<String, Object> getScalingData() {
		return scaling;
	}
	
	
	public void parse(Reader input) throws IOException {
		exception = null;
		
		try {
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(this);
			reader.setErrorHandler(this);

			/*
			 * Validate the document using the XML Schema definition.
			 */
			reader.setFeature("http://xml.org/sax/features/validation", true);
			reader.setFeature("http://apache.org/xml/features/validation/schema", true);
			reader.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", "http://www.cosy.sbg.ac.at/wavelab/ClassificationModel resources/xml/ClassificationModel.xsd");

			reader.parse(new InputSource(input));
		} catch(SAXException e) {
			throw new IOException("Parsing XML-model failed: " + e.getMessage());
		}
		
		if(exception != null) {
			throw new IOException("XML file contains errors: " + exception.getMessage());
		}
	}
	
	
	/*
	 * Begin of parser code.
	 */
	
	public void startDocument() {
		stateStack = new Stack<ParserState>();
		paramStack = new Stack<Map<String,Object>>();
		classes = new HashMap<ClassDescriptor, Map<String, Object>>();
	}
	
	public void startElement(String uri, String name, String qname, Attributes atts) {
		if(name.equals("model")) {
			/*
			 * Attributes: 'class'
			 * Holds model parameters, selection and classes.
			 */
			stateStack.push(ParserState.MODEL);
			paramStack.push(new HashMap<String, Object>());
			
			className = atts.getValue("class");

		} else if(name.equals("param")) {
			/*
			 * Attributes: 'id' and 'value'
			 */
			stateStack.push(ParserState.PARAM);
			
			String key = atts.getValue("id");
			String value = atts.getValue("value");
			
			paramStack.peek().put(key, value);
			
		} else if(name.equals("complexParam")) {
			/*
			 * Attributes: 'id'
			 * Can hold either a matrix, a vector, or an index set.
			 */
			stateStack.push(ParserState.COMPLEX_PARAM);
			
			complexId = atts.getValue("id");
			
		} else if(name.equals("selection")) {
			/*
			 * Holds selection parameters.
			 */
			stateStack.push(ParserState.SELECTION);
			paramStack.push(new HashMap<String, Object>());
			
			selectionClassName = atts.getValue("class");
			
		} else if(name.equals("scaling")) {
			/*
			 * Holds scaling parameters.
			 */
			stateStack.push(ParserState.SCALING);
			paramStack.push(new HashMap<String, Object>());
			
			scalingClassName = atts.getValue("class");
			
		} else if(name.equals("class")) {
			/*
			 * Attributes: 'label'
			 * Holds class-specific parameters.
			 */
			stateStack.push(ParserState.CLASS);
			paramStack.push(new HashMap<String, Object>());
			
			classDescr = DefaultClassDescriptor.getInstance(atts.getValue("label"));
			
		} else if(name.equals("matrix")) {
			/*
			 * Attributes: 'rows' and 'cols'
			 * Holds a number of 'row's, where each row holds a number of 'elem's
			 * of type double.
			 */
			stateStack.push(ParserState.MATRIX);
			
			int rows = Integer.parseInt(atts.getValue("rows"));
			int cols = Integer.parseInt(atts.getValue("cols"));
			matrixData = new MatrixData(rows, cols);
			
		} else if(name.equals("row")) {
			stateStack.push(ParserState.MATRIX_ROW);
			
		} else if(name.equals("vector")) {
			/*
			 * Attributes: size
			 * Holds 'size' 'elem's of type double.
			 */
			stateStack.push(ParserState.VECTOR);
			
			int size = Integer.parseInt(atts.getValue("size"));
			vectorData = new VectorData(size);
			
		} else if(name.equals("indexSet")) {
			/*
			 * Attributes: size
			 * Holds 'size' 'elem's of type integer.
			 */
			stateStack.push(ParserState.INDEX_SET);
			
			int size = Integer.parseInt(atts.getValue("size"));
			indexData = new IndexData(size);
			
		} else if(name.equals("elem")) {
			buffer = new StringBuilder();
			
			/*
			 * Look at the last tag read to get the next state.
			 */
			switch(stateStack.peek()) {
			case VECTOR:
				stateStack.push(ParserState.VECTOR_ELEM);
				break;
			
			case MATRIX_ROW:
				stateStack.push(ParserState.MATRIX_ELEM);
				break;
			
			case INDEX_SET:
				stateStack.push(ParserState.INDEX_ELEM);
				break;
			}
			
		} else {
			// should never happen, if validation is used.
			exception = new Exception("Unknown XML tag appeared: " + name);
		}
	}
	
	public void endElement(String uri, String name, String qname) {
		double d;
		int i;
		
		switch(stateStack.pop()) {
		case MODEL:
			model = paramStack.pop();
			break;
			
		case SELECTION:
			selection = paramStack.pop();
			break;
			
		case SCALING:
			scaling = paramStack.pop();
			break;
			
		case COMPLEX_PARAM: 
			paramStack.peek().put(complexId, complexObject);
			break;
			
		case CLASS:
			classes.put(classDescr, paramStack.pop());
			break;
			
		case MATRIX:
			complexObject = matrixData.data;
			break;
			
		case MATRIX_ROW:
			matrixData.nextRow();
			break;
			
		case VECTOR: 
			complexObject = vectorData.list;
			break;
			
		case INDEX_SET:
			complexObject = indexData.list;
			break;
			
		case MATRIX_ELEM:
			d = Double.parseDouble(buffer.toString());
			matrixData.add(d);
			break;
			
		case VECTOR_ELEM:
			d = Double.parseDouble(buffer.toString());
			vectorData.add(d);
			break;
			
		case INDEX_ELEM:
			i = Integer.parseInt(buffer.toString());
			indexData.add(i);
			break;

		case PARAM: 			
		default:
			break;	
		}
	}
	
	public void characters(char ch[], int start, int length) {
		/*
		 * Note, that SAX does not guarantee, that all characters
		 * of a character sequence are read at once, i.e. in one
		 * chunck. We therefore use a StringBuilder as a temporary
		 * buffer.
		 */
		
		//First, drop leading whitespaces.
		while(length > 0 && Character.isWhitespace(ch[start])) {
			start++;
			length--;
		}
		
		if(length > 0) {
			buffer.append(ch, start, length);
		}
	}
	
	public void error(SAXParseException e) {
		exception = e;
	}
	
	public void warning(SAXParseException e) {
		System.err.println("Parser warning: " + e.getMessage());
	}
	
	
	/*
	 * Code for data structures used during parsing.
	 * 
	 * FIXME: Check for array overflows. Currently, the parser
	 *        relies upon the correctness of the supplied 'size'
	 *        attributes.
	 */
	private class VectorData {
		public final double[] list;
		private int pointer;
		
		public VectorData(int size) {
			list = new double[size];
			pointer = 0;
		}
		
		public void add(double d) {
			list[pointer] = d;
			pointer++;
		}
	}
	
	
	private class IndexData {
		public final int[] list;
		private int pointer;
		
		public IndexData(int size) {
			list = new int[size];
			pointer = 0;
		}
		
		public void add(int i) {
			list[pointer] = i;
			pointer++;
		}
	}
	
	
	private class MatrixData {
		public final double[][] data;
		private int rowPointer;
		private int colPointer;
		
		public MatrixData(int rows, int cols) {
			data = new double[rows][cols];
			rowPointer = 0;
			colPointer = 0;
		}
		
		public void add(double d) {
			data[rowPointer][colPointer] = d;
			colPointer++;
		}
		
		public void nextRow() {
			rowPointer++;
			colPointer = 0;
		}
	}

}
