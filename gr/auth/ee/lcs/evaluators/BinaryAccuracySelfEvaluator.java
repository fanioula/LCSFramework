/**
 * 
 */
package gr.auth.ee.lcs.evaluators;

import gr.auth.ee.lcs.LCSExploitTemplate;
import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.data.ClassifierTransformBridge;
import gr.auth.ee.lcs.data.IEvaluator;

/**
 * An Accuracy Evaluator. Accuracy is considered a binary value (0/1)
 * 
 * @author Miltos Allamanis
 * 
 */
public class BinaryAccuracySelfEvaluator implements IEvaluator {

	/**
	 * A boolean indicating if the evaluator is going to print the results.
	 */
	private final boolean printResults;

	/**
	 * A boolean indicating if the evaluator is going to print the confusion
	 * matrix.
	 */
	private final boolean printConfusionMatrix;

	/**
	 * Constructor for setting evaluator parameters.
	 * 
	 * @param print
	 *            true if evaluator will print data
	 * @param confusionMatrix
	 *            true if evaluator is going to print confusion matrix
	 */
	public BinaryAccuracySelfEvaluator(final boolean print,
			final boolean confusionMatrix) {
		printResults = print;
		printConfusionMatrix = confusionMatrix;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gr.auth.ee.lcs.data.IEvaluator#evaluateSet(gr.auth.ee.lcs.classifiers
	 * .ClassifierSet)
	 */
	@Override
	public double evaluateSet(ClassifierSet population) {
		LCSExploitTemplate eval = new LCSExploitTemplate();
		int tp = 0, fp = 0;
		for (int i = 0; i < ClassifierTransformBridge.instances.length; i++) { // for
																				// each
																				// instance
			if (eval.classifyCorrectly(ClassifierTransformBridge.instances[i],
					population))
				tp++;
			else if (eval.classify(ClassifierTransformBridge.instances[i],
					population) != -1)
				fp++;
		}

		double errorRate = ((double) fp) / ((double) (fp + tp));

		if (printResults) {
			System.out.println("tp:" + tp + " fp:" + fp + " errorRate:"
					+ errorRate + " total instances:"
					+ ClassifierTransformBridge.instances.length);
		}
		return errorRate;
	}

}