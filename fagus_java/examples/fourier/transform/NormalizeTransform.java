package fourier.transform;

public class NormalizeTransform {
	/**
	 * Normalize the Fourier coefficient matrix, such that every
	 * element is in [0,1]
	 * 
	 * @param orig
	 * @return
	 */
	public static double[][] transform(double[][] orig) {
		double dc = orig[0][0];
		
		for(int i = 0; i < orig.length; i++) {
			for(int j = 0; j < orig[i].length; j++) {
				orig[i][j] = orig[i][j] / dc;
			}
		}
		
		return orig;
	}
}
