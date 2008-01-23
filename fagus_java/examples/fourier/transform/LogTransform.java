package fourier.transform;

public class LogTransform {
	/**
	 * Make a logarithmic transform of the Fourier coefficient
	 * matrix for display purpose.
	 * 
	 * @param orig
	 * @return
	 */
	public static double[][] transform(double[][] orig) {
		for(int i = 0; i < orig.length; i++) {
			for(int j = 0; j < orig[i].length; j++) {
				orig[i][j] = Math.log(1.0 + orig[i][j]);
			}
		}
		
		return orig;
	}
}
