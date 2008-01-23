package classify.parzen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import math.statistics.MaximumLikelihoodEstimation;

import util.ClassDescriptor;
import util.io.ExportVisitor;
import classify.bayes.BayesClassifier;

/**
 * The Parzen window classifier is a nonparametric approach to
 * classification. It uses a kernel function to smooth the
 * empirical density obtained from the training samples. The
 * kernel is usually the PDF of either a uniform or a normal
 * distribution.
 *   
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class ParzenWindowClassifier extends BayesClassifier {
	private final double radius;
	private final KernelType type;
	private int dimension;
	
	/**
	 * Enumeration of different kernel types.
	 * 
	 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
	 */
	public static enum KernelType {NORMAL, UNIFORM, GAUSSIAN_PRODUCT;
	
		/**
		 * Create a new instance of this kernel. The kernel's shape and size is
		 * determined by the covariance matrix and the radius, respectively.
		 * 
		 * @param covariance
		 * @param radius
		 * @return
		 */
		public Kernel getKernel(double[][] covariance, double radius) {
			Kernel kernel = null;
			
			switch(this) {
			case UNIFORM: kernel = new UniformKernel(covariance, radius);
			case NORMAL: kernel = new NormalKernel(covariance, radius);
			case GAUSSIAN_PRODUCT: kernel = new GaussianProductKernel(radius);
			}
			
			return kernel;
		}
	};
	
	/**
	 * Create a new Parzen Window Classifier
	 * @param radius
	 */
	public ParzenWindowClassifier(double radius) {
		this(radius, KernelType.NORMAL);
	}
	
	public ParzenWindowClassifier(double radius, KernelType kernel) {
		this.radius = radius;
		this.type = kernel;
	}
	

	public void export(ExportVisitor visitor) {
		ExportVisitor.Parameters params = visitor.newParametersInstance();
		params.setParameter("radius", Double.toString(radius));
		params.setParameter("dimension", Integer.toString(dimension));

		switch(type) {
		case NORMAL:
			params.setParameter("kernel", "normal");
			break;
		case UNIFORM:
			params.setParameter("kernel", "uniform");
			break;
		}
		
		visitor.setModel(this.getClass().getName(), params);
		
		for(ClassDescriptor c: distributions.keySet()) {
			/*
			 * The covariance matrix can be reconstructed from
			 * the original vectors. Therefore, only these vectors
			 * must be exported.
			 */
			ParzenDistribution d = (ParzenDistribution)distributions.get(c);
			
			List<double[]> vectors = d.getVectors();
			double[][] a = new double[vectors.size()][];
			a = vectors.toArray(a);
			
			params = visitor.newParametersInstance();
			params.setParameter("vectors", a);
			
			visitor.addClass(c.toString(), params);
		}
	}
	
	
	/**
	 * Reconstruct a parzen window classifier from serialized data.
	 * 
	 * @param model
	 * @param classes
	 * @return
	 */
	public static ParzenWindowClassifier newInstance(Map<String, Object> model, 
			Map<ClassDescriptor, Map<String, Object>> classes) {
		
		ParzenWindowClassifier classifier;
		
		double radius = Double.parseDouble((String)model.get("radius"));
		int dimension = Integer.parseInt((String)model.get("dimension"));
		KernelType type;
		
		if( ((String)model.get("kernel")).equals("normal") ) {
			type = KernelType.NORMAL;
		} else {
			type = KernelType.UNIFORM;
		}
		
		classifier = new ParzenWindowClassifier(radius, type);
		
		for(ClassDescriptor c: classes.keySet()) {
			double[][] a = (double[][])classes.get(c).get("vectors");
			List<double[]> l = new ArrayList<double[]>(a.length);
			
			for(int i = 0; i < a.length; i++) {
				l.add(a[i]);
			}
			
			// reconstruct covariance matrix
			double[] mean = MaximumLikelihoodEstimation.getMean(l, dimension);
			double[][] cov = MaximumLikelihoodEstimation.getCovariance(l, mean);
			
			Kernel kernel = type.getKernel(cov, radius);
			
			classifier.distributions.put(c, new ParzenDistribution(kernel, l));
		}
		
		return classifier;
	}

	
	@Override
	protected void doTrain(Map<ClassDescriptor, List<double[]>> data,
			int dimension) {
		
		this.dimension = dimension;
		
		for(ClassDescriptor c: data.keySet()) {
			/*
			 * Estimate covariance using ML.
			 */
			double[] mean = MaximumLikelihoodEstimation.getMean(data.get(c), dimension);
			double[][] cov = MaximumLikelihoodEstimation.getCovariance(data.get(c), mean);

			Kernel kernel = type.getKernel(cov, radius);
			
			distributions.put(c, new ParzenDistribution(kernel, data.get(c)));
		}
	}

}
