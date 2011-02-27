package gr.auth.ee.lcs.data.updateAlgorithms;

import gr.auth.ee.lcs.classifiers.Classifier;
import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.data.updateAlgorithms.data.GenericSLCSClassifierData;

/**
 * The update algorithm for the AS-LCS.
 * 
 * @author Miltos Allamanis
 * 
 */
public class ASLCSUpdateAlgorithm extends AbstractSLCSUpdateAlgorithm {

	/**
	 * The strictness factor for updating.
	 */
	private double n;

	/**
	 * Object's Constuctor.
	 * 
	 * @param n
	 *            the strictness factor ν used in updating
	 */
	public ASLCSUpdateAlgorithm(final double n) {
		this.n = n;
	}

	/* (non-Javadoc)
	 * @see gr.auth.ee.lcs.data.updateAlgorithms.AbstractSLCSUpdateAlgorithm#updateFitness(gr.auth.ee.lcs.classifiers.Classifier, int, gr.auth.ee.lcs.classifiers.ClassifierSet)
	 */
	@Override
	public void updateFitness(final Classifier aClassifier, final int numerosity,
			final ClassifierSet correctSet) {
		GenericSLCSClassifierData data = ((GenericSLCSClassifierData) aClassifier.updateData);
		if (correctSet.getClassifierNumerosity(aClassifier) > 0)
			data.tp += 1; // aClassifier at the correctSet
		else
			data.fp += 1;

		// Niche set sharing heuristic...
		aClassifier.fitness = Math.pow(((double) (data.tp))
				/ (double) (data.msa), n);

	}

	/* (non-Javadoc)
	 * @see gr.auth.ee.lcs.data.UpdateAlgorithmFactoryAndStrategy#getComparisonValue(gr.auth.ee.lcs.classifiers.Classifier, int)
	 */
	@Override
	public double getComparisonValue(final Classifier aClassifier, final int mode) {
		GenericSLCSClassifierData data;
		switch (mode) {
		case COMPARISON_MODE_EXPLORATION:
			return aClassifier.fitness * (aClassifier.experience < 8 ? 0 : 1);
		case COMPARISON_MODE_DELETION:
			data = (GenericSLCSClassifierData) aClassifier.updateData;
			return aClassifier.fitness
					* ((aClassifier.experience < 20) ? 100. : Math.exp(-(Double
							.isNaN(data.ns) ? 1 : data.ns) + 1))
					* (((aClassifier.getCoverage() == 0) && aClassifier.experience == 1) ? 0.
							: 1);
			// TODO: Something else?
		case COMPARISON_MODE_EXPLOITATION:
			data = (GenericSLCSClassifierData) aClassifier.updateData;
			return (((double) (data.tp)) / (double) (data.msa));
		}
		return 0;
	}

}