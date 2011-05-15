/**
 * 
 */
package gr.auth.ee.lcs.evaluators.bamevaluators;

import java.util.Vector;

import gr.auth.ee.lcs.AbstractLearningClassifierSystem;
import gr.auth.ee.lcs.classifiers.Classifier;
import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.data.IEvaluator;
import gr.auth.ee.lcs.evaluators.BAMPercentageEvaluator;
import gr.auth.ee.lcs.utilities.ExtendedBitSet;

/**
 * A mlPosition evaluator
 * 
 * @author Miltiadis Allamanis
 * 
 */
public class PositionBAMEvaluator implements IEvaluator {

	private final AbstractLearningClassifierSystem lcs;

	/**
	 * Constructor.
	 * 
	 * @param n
	 *            the size of the mlPosition_N
	 * @param type
	 *            the type of representation to be used.
	 */
	public PositionBAMEvaluator(final int n, final int type,
			final AbstractLearningClassifierSystem aLcs) {
		bamChromosomes = new Vector<Classifier>();
		lcs = aLcs;
		generateBAM(n, type);

	}

	/**
	 * Rules are represented with a strict ml representation.
	 */
	public static final int STRICT_REPRESENTATION = 0;
	/**
	 * Rules are represented with a generic ml representation.
	 */
	public static final int GENERIC_REPRESENTATION = 1;

	/**
	 * A vector containing the chromosomes of the BAM.
	 */
	private Vector<Classifier> bamChromosomes;

	/**
	 * Generate problem BAM.
	 * 
	 * @param n
	 *            the size of the mlPosition_N
	 * @param type
	 *            the type of representation to be used
	 */
	private void generateBAM(final int n, final int type) {
		for (int i = 0; i < n + 1; i++) {
			bamChromosomes.add(lcs.getNewClassifier(generateRule(i, type, n)));
		}
	}

	/**
	 * Generate a specific BAM rule.
	 * 
	 * @param i
	 *            the i-th rule
	 * @param type
	 *            the type of representation to be used
	 * @param n
	 *            the size of mlPosition_N
	 * @return the chromosome representing this rule
	 */
	private ExtendedBitSet generateRule(final int i, final int type, final int n) {
		String rule = "";
		for (int position = i; position < n; position++) {
			rule = "01" + rule;
		}
		if (i - 1 >= 0)
			rule = "11" + rule;
		for (int position = 0; position < i - 1; position++) {
			rule = "00" + rule;
		}

		// Generate Consequent
		for (int position = n - 1; position >= 0; position--) {
			if (type == GENERIC_REPRESENTATION)
				rule = "1" + rule;
			if (position == i - 1) {
				rule = "1" + rule;
			} else {
				rule = "0" + rule;
			}
		}

		final ExtendedBitSet result = new ExtendedBitSet(rule);
		return result;
	}

	@Override
	public double evaluateSet(ClassifierSet classifiers) {
		BAMPercentageEvaluator eval = new BAMPercentageEvaluator(bamChromosomes);
		return eval.evaluateSet(classifiers);
	}

}