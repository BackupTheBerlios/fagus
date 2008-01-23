package fourier.search;

import java.util.Observable;
import java.util.Observer;

import select.subset.greedy.NestedSubsetAlgorithm;

/**
 * This class is a slightly modified version of the default subset
 * selection logger. All it does is translating the integer representation
 * to the original triple form channel-radius-width.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class FourierFeatureSelectionLogger implements Observer {
	private final FourierSearchSpace featureSpace;

	public FourierFeatureSelectionLogger(FourierSearchSpace featureSpace) {
		this.featureSpace = featureSpace;
	}

	public void update(Observable alg, Object arg) {
		NestedSubsetAlgorithm.Operation op = (NestedSubsetAlgorithm.Operation)arg;

		StringBuilder b = new StringBuilder();
		/*
		 * If a new feature is added to the subset, the search space will be
		 * reduced. Therefore, we print "REMOVE" when an add operation is 
		 * indicated.
		 */
		if(op.isAdd()) {
			b.append("REMOVE ");
		} else {
			b.append("ADD    ");
		}

		// convert from integer to triple
		decode(b, op.getFeature());

		b.append(": [");
		for(Integer index: ((NestedSubsetAlgorithm)alg).getCandidate()) {
			b.append(' ');
			decode(b, index);
			b.append(' ');
		}
		b.append("]: ");
		b.append(((NestedSubsetAlgorithm)alg).getCandidateValue());

		System.out.println(b.toString());
	}

	private void decode(StringBuilder b, int index) {
		int[] c = featureSpace.decode(index);

		b.append('{');
		b.append(c[0]);
		b.append(',');
		b.append(c[1]);
		b.append(',');
		b.append(c[2]);
		b.append('}');
	}
}
