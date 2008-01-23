package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is a parser for input files in the libSVM style.
 * The style is as follows.
 * <pre>
 * empty ::= (space|tab){space|tab}
 * elem  ::= int ':' float
 * label ::= ['+'|'-'] int
 * line  ::= label {empty elem}
 * float ::= ['-'] int [ '.' {digit} [ ('E' | 'e') ['-'] int ] ]
 * int   ::= digit {digit}
 * </pre>
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class LibSVMVectorSetReader implements VectorSetReader {
	private final String inputFile;
	private int dimension;
	
	/**
	 * Create a new parser for a given input file.
	 * 
	 * @param inputFile The file in libSVM format.
	 */
	public LibSVMVectorSetReader(String inputFile) {
		this.inputFile = inputFile;
		dimension = -1;
	}
	
	/**
	 * Create a new parser for huge input files of known dimension.
	 * 
	 * @param inputFile The file in LibSVM format.
	 * @param dimension The number of vector elements of a single input line.
	 */
	public LibSVMVectorSetReader(String inputFile, int dimension) {
		this.inputFile = inputFile;
		this.dimension = dimension;
	}

	/**
	 * Parse contents and write it to a VectorSet.
	 */
	public VectorSet parse() throws IOException {
		Map<double[], ClassDescriptor> result = new HashMap<double[], ClassDescriptor>();
		BufferedReader stream = new BufferedReader(new FileReader(new File(inputFile)));
		//int n = getDimension(stream);
		Map<String, ClassDescriptor> classes = new HashMap<String, ClassDescriptor>();
		String[] featureLabels = getFeatureLabels(stream);
		
//		StringBuilder regex = new StringBuilder("(\\S+)");
//		for(int i = 0; i < dimension; i++) {
//			regex.append("\\s+[^\\s:]+:(-?\\d+\\.?\\d*(?>[Ee]-?\\d+)?)");
//		}
//		regex.append("\\s*");
//		
//		Pattern p = Pattern.compile(regex.toString());
		//Pattern p = Pattern.compile("\\s*[^\\s:]+:(-?\\d+\\.?\\d*(?>[Ee]-?\\d+)?)\\s*");
		
		String l = stream.readLine();
		while(l != null) {
			StringTokenizer st = new StringTokenizer(l, " :");
			String label = st.nextToken();

			if(st.hasMoreTokens()) {
				ClassDescriptor c;

				if(classes.containsKey(label)) {
					c = classes.get(label);
				} else {
					c = DefaultClassDescriptor.getInstance(label);
					classes.put(label, c);
				}
				
				double[] v = new double[dimension];
				
				for(int i = 0; i < dimension; i++) {
					st.nextToken();
					
					String str = st.nextToken();
					//Matcher m = p.matcher(str);
					v[i] = Double.parseDouble(str);
				}
				
				result.put(v, c);
			}
			
			
//			Matcher m = p.matcher(l);
//			if(m.matches()) {
//				double[] v = new double[dimension];
//				String label = m.group(1);
//				ClassDescriptor c;
//				
//				if(classes.containsKey(label)) {
//					c = classes.get(label);
//				} else {
//					//create new class descriptor
//					c = DefaultClassDescriptor.getInstance(label);
//					classes.put(label, c);
//				}
//				
//				for(int i = 0; i < dimension; i++) {
//					v[i] = Double.parseDouble(m.group(i+2));
//				}
//				
//				result.put(v, c);
//			}
			
			l = stream.readLine();
		}
		stream.close();
		
		return new VectorSet(result, featureLabels);
	}

	/*
	 * Get the labels of the features in this file. The method will
	 * analyze the first line and assume, that all other lines have
	 * the same feature labels. The stream is reset afterwards.
	 */
	private String[] getFeatureLabels(BufferedReader in) throws IOException {
		String labels[];
		
		if(dimension < 0) {
			in.mark(0x4000);
			List<String> lbs = new ArrayList<String>();
			Pattern p;
			Matcher m;
		
			String l = in.readLine();
			p = Pattern.compile("\\S+(.*)");
			m = p.matcher(l);
		
			m.find();
			l = m.group(1);
		
			p = Pattern.compile("\\s+([^\\s:]+):\\S+");
			m = p.matcher(l);
		
			while(m.find()) {
				lbs.add(m.group(1));
			}
		
			in.reset();
		
			this.dimension = lbs.size();
			
			labels = new String[dimension];
			labels = lbs.toArray(labels);
		} else {
			/*
			 * TODO: read original labels from input file here.
			 */
			labels = new String[dimension];
			
			for(int i = 0; i < dimension; i++) {
				labels[i] = Integer.toString(i + 1);
			}
		}
		
		return labels;
	}
}
