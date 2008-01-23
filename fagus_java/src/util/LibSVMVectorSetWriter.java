package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Output a Vector set in libSVM format.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class LibSVMVectorSetWriter implements VectorSetWriter {
	private final String outFile;
	
	public LibSVMVectorSetWriter(String outFile) {
		this.outFile = outFile;
	}
	
	public void write(VectorSet vectors) throws IOException {
		File out = new File(outFile);
		
		if(out.exists()) {
			if(!out.isFile()) {
				throw new IOException("Output destination \"" + outFile + "\" exists and is not a file");
			}
		} else {
			out.createNewFile();
		}
		
		PrintStream outStream = new PrintStream(new FileOutputStream(out));
		
		for(double[] v: vectors.getData().keySet()) {
			ClassDescriptor c = vectors.getData().get(v);
			
			StringBuilder line = new StringBuilder(c.toString());
			for(int i = 0; i < v.length; i++) {
				line.append(' ');
				line.append(vectors.getFeatureLabel(i));
				line.append(':');
				line.append(v[i]);
			}
			
			outStream.println(line.toString());
		}
		
		outStream.close();
	}

}
