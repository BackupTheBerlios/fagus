package reporting;

import java.util.Observable;
import java.util.Observer;

import select.subset.greedy.NestedSubsetAlgorithm;

/**
 * This logging keeps track of the progress for nested subset
 * selection algorithms.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public class SubsetSelectionLogger implements Observer {
	
	public void update(Observable alg, Object data) {
		NestedSubsetAlgorithm.Operation op = (NestedSubsetAlgorithm.Operation)data;
		
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
		b.append(op.getFeature());
		
		b.append(": [");
		for(Integer index: ((NestedSubsetAlgorithm)alg).getCandidate()) {
			b.append(' ');
			b.append(index);
			b.append(' ');
		}
		b.append("]: ");
		b.append(((NestedSubsetAlgorithm)alg).getCandidateValue());

		System.out.println(b.toString());
	}
}
