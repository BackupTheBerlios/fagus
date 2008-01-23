package fourier.transform;

/**
 * This is an implementation of a two dimensional FFT processor.
 * Note that the dimensions are required to be powers of 2.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class FastFourierTransform {

	private static final Complex[] roots = {
		new Complex(-1.0, 0.0),            // cos(pi) + i sin(pi)
		new Complex(0.0, 1.0),             // cos(pi/2) + i sin(pi/2)
		new Complex(0.7071067811865475244, 0.7071067811865475244), // cos(pi/4) + i sin(pi/4)
		new Complex(0.9238795325112867561, 0.3826834323650897717), // cos(pi/8) + i sin(pi/8)
		new Complex(0.9807852804032304491, 0.1950903220161282678), // cos(pi/16) + i sin(pi/16)
		new Complex(0.9951847266721968862, 0.0980171403295606020), // cos(pi/32) + i sin(pi/32)
		new Complex(0.9987954562051723927, 0.0490676743274180143), // cos(pi/64) + i sin(pi/64)
		new Complex(0.9996988186962042201, 0.0245412285229122880)  // cos(pi/128) + i sin(pi/128)
	}; // works for matrices of maximum size 256x256
	
	private final int width;
	private final int height;
	private final int[] horizontalBitReverseTable;
	private final int[] verticalBitReverseTable;
	private final Complex[][] fftBuffer;
	private final Complex[] columnBuffer;
	
	/**
	 * Initialize an FFT processor. The constructor performs
	 * the preprocessing. All further data is required to 
	 * conform to the dimensions that are passed to this 
	 * constructor.
	 * @param height the height of data. This must be a power of 2.
	 * @param width the width of data. This must be a power of 2.
	 */
	public FastFourierTransform(int height, int width) {
		this.height = height;
		this.width = width;
		
		verticalBitReverseTable = getBitReverseTable(height);
		horizontalBitReverseTable = getBitReverseTable(width);
		
		fftBuffer = new Complex[height][width];
		columnBuffer = new Complex[height];
	}
	
	/**
	 * Get the two dimensional Fourier transformation of the 
	 * data. This algorithm uses an iterative unshifted FFT 
	 * scheme.
	 */
	public double[][] transform(int[][] original) {
		toComplexData(original);
		
		transform2D();
		
		return fromComplexData();
	}
	
	/*
	 * The two dimensional Fourier transformation can be done by
	 * first doing a one dimensional transformation on the rows
	 * and then applying a one dimensional transformation on all
	 * columns.
	 */
	private void transform2D() {
		for(int y = 0; y < height; y++) {
			transform1D(fftBuffer[y], width, horizontalBitReverseTable);
		}
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				columnBuffer[y] = fftBuffer[y][x];
			}
			/*
			 * Note, that the column vector does only contain references
			 * to the data array. Therfore, the results of the transformation
			 * need not be copied back to the array.
			 */
			transform1D(columnBuffer, height, verticalBitReverseTable);
		}
	}
	
	/*
	 * This is an iterative algorithm for FFT. It is based on
	 * the solution in
	 * 
	 * Chapter 30.3 "Efficient FFT Implementations"
	 * in
	 * T.H. Cormen, C.E. Leiserson, R.L. Rivest, and C. Stein
	 * "An Introduction to Algorithms"
	 * Second Edition
	 * MIT Press, 2001
	 * 
	 * The bit-reverse mapping is done in a preprocessing step
	 * by the constructor of this class, since all images have
	 * the same size (as suggested in the book).
	 */
	private void transform1D(Complex[] z, int n, int[] bitReverseTable) {
		for(int k = 1; k < n; k++) {
			int l = bitReverseTable[k];
			
			if(l > k) {
				/*
				 * Do not swap references here! Otherwise, we would
				 * have to copy back the column vector in the vertical
				 * part of the 2D transformation.
				 */
				Complex.swap(z[k], z[l]);
			}
		}
	    
		Complex tmp = new Complex();
		Complex omega = new Complex();

		int i = 0;
		for(int m = 2; m <= n; m = m << 1) {
			/*
			 * The complex roots of unity are computed
			 * by starting with the 0-th power (i.e. 1)
			 * and subsequently multiplying the 1-th power
			 * root to it.
			 */
			for(int k = 0; k < n; k += m) {
				omega.setTo(1, 0);
				int mHalf = m >> 1;
			
				for(int j = 0; j < mHalf; j++) {
					/*
					 * Warning: this is a dirty hack that bypasses
					 * the use of an additional temporary variable.
					 * The order of operations is really crucial, to
					 * prevent from overwriting an important value.
					 */
					tmp.setTo(z[k + j + mHalf]);
					tmp.mult(omega);
					z[k + j + mHalf].setTo(z[k+j]);
					z[k + j + mHalf].sub(tmp);
					z[k+j].add(tmp);
					
					omega.mult(roots[i]);
				}
			}
			
			i++;
		}
	}

	/*
	 * Convert an integer array to a complex array.
	 */
	private void toComplexData(int[][] original) {
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				fftBuffer[y][x] = new Complex(original[y][x], 0);
			}
		}
	}
	
	/*
	 * Convert complex data to a double array.
	 */
	private double[][] fromComplexData() {
		double[][] result = new double[height][width];
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				result[y][x] = fftBuffer[y][x].magnitude() / width;
			}
		}
		
		return result;
	}
	
	/*
	 * Create a bit reverse data for some number n, which
	 * is required to be a power of 2. A bit reverse table
	 * contains the bit reverse of the indices.
	 * 
	 *  	Bits	Reverse
	 * 0	000 	000 	0
	 * 1	001 	100 	4
	 * 2	010 	010 	2
	 * 3	011 	110 	6
	 * 4	100 	001 	1
	 * 5	101 	101 	5
	 * 6	110 	011 	3
	 * 7	111 	111 	7
	 * 
	 * This tables are needed to reorganize the vectors before
	 * applying the iterative FFT.
	 */
	private int[] getBitReverseTable(int n) {
		int[] map = new int[n];
		
		for(int i = 1; i < n; i++) {
			int j = 0;
			int k = i;
			
			for(int l = n; l > 1; l = l >> 1) {
				j = (j << 1) | (k & 0x1);
				k = k >> 1;
			}
			
			map[i] = j;
		}
		
		return map;
	}
	
	private static class Complex {
		private double real, imag;
		
		public Complex() {
			this(1.0, 0.0);
		}
		
		public Complex(double r, double i) {
			real = r;
			imag = i;
		}
		
		public void add(Complex c) {
			real += c.real;
			imag += c.imag;
		}
		
		public void sub(Complex c) {
			real -= c.real;
			imag -= c.imag;
		}
		
		public void mult(Complex c) {
			double tmp = real;
			real = real * c.real - imag * c.imag;
			imag = tmp * c.imag + c.real * imag;
		}
		
		public double magnitude() {
			return Math.sqrt(real * real + imag * imag);
		}
		
		public void setTo(double r, double i) {
			real = r;
			imag = i;
		}
		
		public void setTo(Complex c) {
			real = c.real;
			imag = c.imag;
		}
		
		public static void swap(Complex c1, Complex c2) {
			double tmp = c1.real;
			c1.real = c2.real;
			c2.real = tmp;
			
			tmp = c1.imag;
			c1.imag = c2.imag;
			c2.imag = tmp;
		}
	}
}
