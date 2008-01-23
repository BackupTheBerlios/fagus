package fourier.transform;

/**
 * This class provides frequency filtering for a 2D discrete
 * Fourier transform. The filters are applied to the Fourier
 * coefficient matrix and return the sum of all coefficients
 * that fall into the region of this filter.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class RingFilter {
	private final int origin;
	private final int extent;
	
	/**
	 * Create a new frequency filter for a Fourier coefficient
	 * matrix of size extend x extend.
	 * 
	 * @param extent
	 */
	public RingFilter(int extent) {
		origin = extent / 2;
		this.extent = extent;
	}
	
	/**
	 * Get the number of pixels for each of the possible ring
	 * filters of width 1. That is, the number of Fourier coefficients
	 * that fall into the area of this filter.
	 * 
	 * @return
	 */
	public int[] getNumberOfPixels() {
		int[] result = new int[extent];
		
		result[0] = 4;
		for(int radius = 1; radius < extent; radius++) {
			int x = 0;
			int y = radius;
			int n = 0;
			
			do{
				n += 8;
				
				if(matches(radius, x, y-1)) {
					y--;
				} else if(matches(radius, x+1, y)) {
					x++;
				} else {
					x++;
					y--;
				}
			} while(x < y);
			
			if(matches(radius, x, x)) {
				n += 4;
			}
			
			result[radius] = n;
		}
		
		return result;
	}
	
	/**
	 * This method creates a ring filter of width one at a given
	 * start radius.
	 * 
	 * @param data The Fourier coefficient matrix.
	 * @param radius The start radius of the filter.
	 * @return The sum of all Fourier coefficients.
	 */
	public double apply(double[][] data, int radius) {
		int x = 0;
		int y = radius;
		double result = 0.0;

		/*
		 * The following algorithm has been derived from
		 * Bresenham's algorithm for drawing a circle.
		 * 
		 * Imagin that the Fourier coefficient matrix as 
		 * follows:
		 * 
		 * +---+---+---+---+---+---+---+---+
		 * |   |   |   |   |   |   |   |   |
		 * +---+---+---+---+---+---+---+---+
		 * |   |   | o | o | o | o |   |   |
		 * +---+---+---+---+---+---+---+---+
		 * |   | o | o |   |   | o | o |   |
		 * +---+---+---+---+---+---+---+---+
		 * |   | o |   |   |   |   | o |   |
		 * +---+---+---+---x---+---+---+---+
		 * |   | o |   |   |   |   | o |   |
		 * +---+---+---+---+---+---+---+---+
		 * |   | o | o |   |   | o | o |   |
		 * +---+---+---+---+---+---+---+---+
		 * |   |   | o | o | o | o |   |   |
		 * +---+---+---+---+---+---+---+---+
		 * |   |   |   |   |   |   |   |   |
		 * +---+---+---+---+---+---+---+---+
		 * 
		 * This matrix has size 8x8 and includes a filter of
		 * width 1 with start radius 2. The 'x' indicates the
		 * 'origin' of the matrix, that is the origin of all
		 * ring filters. Every coefficient that is marked with
		 * an 'o' is considered by this filter. These are the
		 * coefficients such that the distance d from their 'center'
		 * to the 'origin' is
		 * 
		 *   2 <= d < 3
		 * 
		 * What can be easily be observed is that it is only
		 * necessary to find the coefficients of one quadrant.
		 * All the other coefficients can be found by mirroring
		 * this quadrant. Moreover, it suffices to draw only one
		 * octant (not including the elements on the diagonal) and
		 * to mirror it eight times. The coefficients on the diagonal
		 * (where |x| == |y|) must be obtained separately and
		 * mirrored only 4 times. 
		 */
		if(radius == 0) {
			// This case cannot be handled by the algorithm below.
			return add4(data, 0);
		}
		
		// coefficients not on the diagonal
		do {
			result += add8(data, x, y);
			
			if(matches(radius, x, y-1)) {
				y--;
			} else if(matches(radius, x+1, y)) {
				x++;
			} else {
				x++;
				y--;
			}
		} while(x < y);
		
		// coefficients on the diagonal
		if(matches(radius, x, x)) {
			result += add4(data, x);
		}

		return result;
	}

	private double add8(double[][] data, int x, int y) {
		return data[origin + x][origin + y]
			+ data[origin + y][origin + x]
			+ data[origin + x][origin - (y+1)]
			+ data[origin - (y+1)][origin + x]
			+ data[origin - (x+1)][origin + y]
			+ data[origin + y][origin - (x+1)]
			+ data[origin - (x+1)][origin - (y+1)]
			+ data[origin - (y+1)][origin - (x+1)];
	}
	
	private double add4(double[][] data, int x) {
		return data[origin + x][origin + x] 
			+ data[origin + x][origin - (x+1)]
			+ data[origin - (x+1)][origin + x]
			+ data[origin - (x+1)][origin - (x+1)];
	}
	
	private boolean matches(int radius, int x, int y) {
		/*
		 * radius <= sqrt[ (x + 1/2)^2 + (y + 1/2)^2 ] <= r + 1
		 */
		double u = Math.sqrt(x * (x + 1) + y * (y + 1) + 0.5);
		
		return (radius <= u) && (u < radius + 1);
	}
}
