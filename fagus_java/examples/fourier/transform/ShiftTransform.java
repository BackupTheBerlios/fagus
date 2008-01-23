package fourier.transform;

/**
 * This is a shift transformation that shifts data
 * located in the center of the array to the outside
 * and vice versa. This can be used to shift the low 
 * frequency information of a Fourier transform to
 * the center of the resulting array.
 * 
 * <pre>
 *  _______ _______
 * |       |       |
 * |   1   |   2   |
 * |_______|_______|
 * |       |       |
 * |   3   |   4   |
 * |_______|_______|
 * </pre>
 * becomes
 * <pre>
 *  _______ _______
 * |       |       |
 * |   4   |   3   |
 * |_______|_______|
 * |       |       |
 * |   2   |   1   |
 * |_______|_______|
 * </pre>
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class ShiftTransform {

	public static double[][] transform(double[][] original) {
		final int h = original.length / 2;
		final int w = original[0].length / 2;
		
		double tmp;
		
		for(int y = 0; y < h; y++) {
			int x = 0;
			for(; x < w; x++) {
				tmp = original[y][x];
				int i = y + h;
				int j = x + w;
				
				original[y][x] = original[i][j];
				original[i][j] = tmp;
			}
			for(; x < 2*w; x++) {
				tmp = original[y][x];
				int i = y + h;
				int j = x - w;
				
				original[y][x] = original[i][j];
				original[i][j] = tmp;
			}
		}
		
		return original;
	}

}
