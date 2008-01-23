package util;

import java.io.IOException;

/**
 * Parser interface for a VectorSet. Classes implementing
 * this interface will parse them from some format.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface VectorSetReader {
	public VectorSet parse() throws IOException;
}
