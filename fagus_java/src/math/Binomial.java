package math;

/**
 * Calculate the binomial coefficient.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class Binomial {
	/**
	 * Get the binomial coefficient (n choose k).
	 * @param n
	 * @param k
	 * @return
	 */
	public static long binomial(int n, int k) {
		/*
		 * This algorithm uses Pascal's triangle to compute 
		 * n choose k. It is separated into different steps
		 * since not all elements of the triangle are really
		 * necessary to obtain the result.
		 * 
		 *  n\k    0  1  2  ... k
		 *   --------------------
		 * 0    |  1
		 * 1    |  1  1
		 * 2    |  1  2  1
		 *      |
		 *  :   |
		 *      |
		 * k    |  1    ...     1
		 * k+1  |  1    ...     ?
		 *      |
		 *  :   |
		 *      |
		 * n-k  |  1    ...     ?
		 * n-k+1|     ?   ...   ?
		 *      |
		 *  :   |
		 *      |
		 * n-1  |            ?  ?
		 * n    |               ?
		 */
		long[][] a = new long[n+1][k+1];
		
		/*
		 * (n choose k) is equal to (n choose n-k)
		 * Since we want to keep k small, we may want to
		 * use the alternative computation. 
		 */
		if(k > n/2) {
			k = n - k;
		}
		
		/*
		 * (n choose 0) and (n choose n) is always 1.
		 * Note, that we only compute the diagonal to
		 * the k-th column. All other columns have no
		 * impact on (n choose k).
		 */
		for(int i = 0; i <= k; i++) {
			a[i][0] = 1L;
			a[i][i] = 1L;
		}
		
		// compute the rest of the (n choose 0) elements
		for(int i = k; i <= n; i++) {
			a[i][0] = 1L;
		}
		
		/*
		 * Compute the full Pascal's triangle until we
		 * reach the k-th row.
		 */
		for(int i = 2; i <= k; i++) {
			for(int j = 1; j < i; j++) {
				a[i][j] = a[i-1][j] + a[i-1][j-1];
			}
		}
		
		/*
		 * Compute the first k columns of Pascal's triangle
		 * until we reach the n-k-th row. The other columns
		 * have no impact on the result.
		 */
		for(int i = k + 1; i <= n - k; i++) {
			for(int j = 1; j <= k; j++) {
				a[i][j] = a[i-1][j] + a[i-1][j-1];
			}
		}
		
		/*
		 * Compute the rest of the required elements to 
		 * finally get the result.
		 */
		for(int i = 1; i <= k; i++) {
			for(int j = i; j <= k; j++) {
				int l = i + n - k;
				a[l][j] = a[l-1][j] + a[l-1][j-1];
			}
		}

		return a[n][k];
	}
}
