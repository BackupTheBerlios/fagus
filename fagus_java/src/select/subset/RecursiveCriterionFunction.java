package select.subset;

/**
 * Criterion Function for use in recursive Branch \& Bound.
 * 
 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
 */
public interface RecursiveCriterionFunction extends CriterionFunction {
	
	/**
	 * Get the criterion state for all features used in the initialization
	 * phase.
	 * 
	 * @return
	 */
	public State getCriterionState();
	
	
	/**
	 * Get the new state by means of the old state.
	 * 
	 * @param features the index of the feature to drop from
	 * the parent state.
	 * @param parentState
	 * @return
	 */
	public State getCriterionState(int feature, State parentState);
	
	/**
	 * This interface acts as a data structure to hold the state
	 * of the criterion function.
	 * 
	 * @author Leonhard Brunauer &lt;lbrunau@cosy.sbg.ac.at&gt;
	 */
	public interface State {
		
		/**
		 * Get the criterion value of the current state.
		 * @return
		 */
		double getValue();
		
		/**
		 * Get the indices of features present in the current state.
		 * @return
		 */
		int[] getConfig();
		
		/**
		 * Get the feature that was removed from parent while creating
		 * this node. If the current node is the root, a negative value
		 * will be returned.
		 * @return
		 */
		int getRemovedFeature();
	}
}
