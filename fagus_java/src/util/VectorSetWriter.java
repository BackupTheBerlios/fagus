package util;

import java.io.IOException;

/**
 * Common interface for all writers (using whatever format) of 
 * VectorSets.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface VectorSetWriter {
	public void write(VectorSet vectors) throws IOException;
}
