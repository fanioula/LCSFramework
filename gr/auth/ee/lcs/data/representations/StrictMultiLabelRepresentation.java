/**
 * 
 */
package gr.auth.ee.lcs.data.representations;

import gr.auth.ee.lcs.classifiers.Classifier;
import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.classifiers.ExtendedBitSet;
import gr.auth.ee.lcs.data.UpdateAlgorithmFactoryAndStrategy;

import java.io.IOException;

import weka.core.Instances;

/**
 * A strict multi-label representation. Each labels uses one bit that it may be
 * 0/1 to either classify or not a sample to a label
 * 
 * @author Miltos Allamanis
 * 
 */
public class StrictMultiLabelRepresentation extends ComplexRepresentation {

	private final int metricType;

	public static final int EXACT_MATCH = 0;
	public static final int ACCURACY = 1;
	public static final int F_MEASURE = 2;
	public static final int HAMMING_LOSS = 3;

	public StrictMultiLabelRepresentation(Attribute[] attributes,
			String[] ruleConsequentsNames, int labels, int type) {
		super(attributes, ruleConsequentsNames, labels);
		metricType = type;
	}

	public StrictMultiLabelRepresentation(String inputArff, int precision,
			int labels, int type) throws IOException {
		super(inputArff, precision, labels);
		metricType = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gr.auth.ee.lcs.data.representations.ComplexRepresentation#
	 * createClassRepresentation(weka.core.Instances)
	 */
	@Override
	protected void createClassRepresentation(Instances instances) {
		for (int i = 0; i < numberOfLabels; i++) {

			final int labelIndex = attributeList.length - numberOfLabels + i;

			String attributeName = instances.attribute(labelIndex).name();

			attributeList[labelIndex] = new Label(chromosomeSize,
					attributeName, 0);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gr.auth.ee.lcs.data.ClassifierTransformBridge#classifyAbility(gr.auth
	 * .ee.lcs.classifiers.Classifier, int)
	 */
	@Override
	public float classifyAbilityAll(Classifier aClassifier, int instanceIndex) {
		switch (metricType) {
		case EXACT_MATCH:
			return classifyExact(aClassifier, instanceIndex);
		case ACCURACY:
			return 0; // TODO
		case HAMMING_LOSS:
			return classifyHamming(aClassifier, instanceIndex);
		}
		return 0;

	}

	private float classifyHamming(Classifier aClassifier, int instanceIndex) {
		float result = 0;
		for (int i = 0; i < numberOfLabels; i++) {
			final int currentLabelIndex = attributeList.length - numberOfLabels
					+ i;
			if (attributeList[currentLabelIndex].isMatch(
					(float) instances[instanceIndex][currentLabelIndex],
					aClassifier))
				result++;
		}
		return result / numberOfLabels;
	}

	/**
	 * Classify absolutely as 0/1
	 * 
	 * @param aClassifier
	 *            the classifier used to classify
	 * @param instanceIndex
	 *            the instance to classify
	 * @return 0 if classifier does not classify the instance correctly, 1
	 *         otherwise
	 */
	private float classifyExact(Classifier aClassifier, int instanceIndex) {
		for (int i = 0; i < numberOfLabels; i++) {
			final int currentLabelIndex = attributeList.length - numberOfLabels
					+ i;
			if (!attributeList[currentLabelIndex].isMatch(
					(float) instances[instanceIndex][currentLabelIndex],
					aClassifier))
				return 0;
		}
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gr.auth.ee.lcs.data.ClassifierTransformBridge#getClassification(gr.auth
	 * .ee.lcs.classifiers.Classifier)
	 */
	@Override
	public int[] getClassification(Classifier aClassifier) {
		int[] labels = new int[numberOfLabels];
		int labelIndex = 0;
		for (int i = 0; i < numberOfLabels; i++) {
			final int currentLabelIndex = attributeList.length - numberOfLabels
					+ i;
			if (attributeList[currentLabelIndex].isMatch(1, aClassifier)) {
				labels[labelIndex] = i;
				labelIndex++;
			}
		}
		int[] result = new int[labelIndex];

		for (int i = 0; i < labelIndex; i++) {
			result[i] = labels[i];
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gr.auth.ee.lcs.data.ClassifierTransformBridge#getDataInstanceLabels(double
	 * [])
	 */
	@Override
	public int[] getDataInstanceLabels(double[] dataInstance) {
		int numOfLabels = 0;
		for (int i = 0; i < numberOfLabels; i++) {
			final int currentLabelIndex = attributeList.length - numberOfLabels
					+ i;
			if (dataInstance[currentLabelIndex] == 1)
				numOfLabels++;
		}
		int[] result = new int[numOfLabels];
		int resultIndex = 0;
		for (int i = 0; i < numberOfLabels; i++) {
			final int currentLabelIndex = attributeList.length - numberOfLabels
					+ i;
			if (dataInstance[currentLabelIndex] == 1) {
				result[resultIndex] = i;
				resultIndex++;
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gr.auth.ee.lcs.data.ClassifierTransformBridge#setClassification(gr.auth
	 * .ee.lcs.classifiers.Classifier, int)
	 */
	@Override
	public void setClassification(Classifier aClassifier, int action) {
		final int labelIndex = attributeList.length - numberOfLabels + action;
		attributeList[labelIndex].randomCoveringValue(1, aClassifier);

	}

	/**
	 * A simple 0/1 label.
	 * 
	 * @author Miltos Allamanis
	 * 
	 */
	public class Label extends Attribute {

		public Label(int startPosition, String attributeName,
				double generalizationRate) {
			super(startPosition, attributeName, generalizationRate);
			lengthInBits = 1;
			chromosomeSize += lengthInBits;
		}

		@Override
		public void fixAttributeRepresentation(
				ExtendedBitSet generatedClassifier) {
			return;
		}

		@Override
		public boolean isEqual(ExtendedBitSet baseChromosome,
				ExtendedBitSet testChromosome) {
			return (baseChromosome.get(positionInChromosome) == testChromosome
					.get(positionInChromosome));
		}

		public boolean getValue(ExtendedBitSet chromosome) {
			return chromosome.get(positionInChromosome);
		}

		@Override
		public boolean isMatch(float attributeVision,
				ExtendedBitSet testedChromosome) {
			return (testedChromosome.get(positionInChromosome) == (attributeVision == 1 ? true
					: false));

		}

		@Override
		public boolean isMoreGeneral(ExtendedBitSet baseChromosome,
				ExtendedBitSet testChromosome) {
			return baseChromosome.get(positionInChromosome) == testChromosome
					.get(positionInChromosome);
		}

		@Override
		public void randomCoveringValue(float attributeValue,
				Classifier generatedClassifier) {
			if (attributeValue == 1)
				generatedClassifier.set(positionInChromosome);
			else
				generatedClassifier.clear(positionInChromosome);

		}

		@Override
		public String toString(ExtendedBitSet convertingClassifier) {
			return convertingClassifier.get(positionInChromosome) ? "1" : "0";
		}

	}

	/**
	 * A voting strategy using voting. Each classifier can vote for or against a
	 * label. The votes are proportional to each classifier's numerosity and
	 * fitness. When a label has positive votes the input is classified with
	 * this label
	 * 
	 * @author Miltos Allamanis
	 * 
	 */
	public class VotingClassificationStrategy implements
			IClassificationStrategy {

		@Override
		public int[] classify(ClassifierSet aSet, double[] visionVector) {
			double[] votingTable = new double[numberOfLabels];
			for (int i = 0; i < numberOfLabels; i++)
				votingTable[i] = 0;

			final ClassifierSet matchSet = aSet.generateMatchSet(visionVector);
			// Let each classifier vote
			final int setSize = matchSet.getNumberOfMacroclassifiers();
			for (int i = 0; i < setSize; i++) {
				// For each classifier
				for (int label = 0; label < numberOfLabels; label++) {
					final Classifier currentClassifier = matchSet
							.getClassifier(i);
					final int classifierNumerosity = matchSet
							.getClassifierNumerosity(i);
					final double fitness = currentClassifier
							.getComparisonValue(UpdateAlgorithmFactoryAndStrategy.COMPARISON_MODE_EXPLOITATION);
					final boolean labelActivated = ((Label) attributeList[attributeList.length
							- numberOfLabels + label])
							.getValue(currentClassifier);
					if (labelActivated)
						votingTable[label] += classifierNumerosity * fitness;
					else
						votingTable[label] -= classifierNumerosity * fitness;

				}
			}

			int numberOfActiveLabels = 0;
			for (int i = 0; i < votingTable.length; i++)
				if (votingTable[i] > 0)
					numberOfActiveLabels++;

			int[] result = new int[numberOfActiveLabels];

			int currentIndex = 0;
			for (int i = 0; i < votingTable.length; i++)
				if (votingTable[i] > 0) {
					result[currentIndex] = i;
					currentIndex++;
				}

			return result;
		}

	}

	@Override
	public float classifyAbilityLabel(Classifier aClassifier,
			int instanceIndex, int label) {
		final int currentLabelIndex = attributeList.length - numberOfLabels
				+ label;
		if (attributeList[currentLabelIndex].isMatch(
				(float) instances[instanceIndex][currentLabelIndex],
				aClassifier)) {
			return 1;
		}
		return -1;
	}

}
