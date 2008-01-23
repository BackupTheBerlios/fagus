package classify.parzen;

public class GaussianProductKernel implements Kernel {
	private final double radius;
	
	public GaussianProductKernel(double radius) {
		this.radius = radius;
	}
	
	public double getValue(double[] v) {
		double sqrNorm = 0;
		
		for(double x: v) {
			sqrNorm += x * x;
		}
		
		return Math.exp(- v.length / 2.0 * Math.log(2 * radius * radius * Math.PI)
				        - sqrNorm / (2.0 * radius * radius));
	}

}
