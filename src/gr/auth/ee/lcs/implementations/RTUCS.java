/*
 *	Copyright (C) 2011 by Allamanis Miltiadis
 *
 *	Permission is hereby granted, free of charge, to any person obtaining a copy
 *	of this software and associated documentation files (the "Software"), to deal
 *	in the Software without restriction, including without limitation the rights
 *	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *	copies of the Software, and to permit persons to whom the Software is
 *	furnished to do so, subject to the following conditions:
 *
 *	The above copyright notice and this permission notice shall be included in
 *	all copies or substantial portions of the Software.
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *	THE SOFTWARE.
 */
/**
 * 
 */
package gr.auth.ee.lcs.implementations;

import gr.auth.ee.lcs.AbstractLearningClassifierSystem;
import gr.auth.ee.lcs.calibration.InternalValidation;
import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.classifiers.populationcontrol.FixedSizeSetWorstFitnessDeletion;
import gr.auth.ee.lcs.data.AbstractUpdateStrategy;
import gr.auth.ee.lcs.data.representations.complex.UniLabelRepresentation;
import gr.auth.ee.lcs.data.representations.complex.UniLabelRepresentation.ThresholdClassificationStrategy;
import gr.auth.ee.lcs.data.updateAlgorithms.RTUCSUpdateAlgorithm;
import gr.auth.ee.lcs.evaluators.AccuracyRecallEvaluator;
import gr.auth.ee.lcs.evaluators.ExactMatchEvalutor;
import gr.auth.ee.lcs.evaluators.HammingLossEvaluator;
import gr.auth.ee.lcs.geneticalgorithm.IGeneticAlgorithmStrategy;
import gr.auth.ee.lcs.geneticalgorithm.algorithms.SteadyStateGeneticAlgorithm;
import gr.auth.ee.lcs.geneticalgorithm.operators.SinglePointCrossover;
import gr.auth.ee.lcs.geneticalgorithm.operators.UniformBitMutation;
import gr.auth.ee.lcs.geneticalgorithm.selectors.RouletteWheelSelector;
import gr.auth.ee.lcs.utilities.SettingsLoader;

import java.io.IOException;
import java.util.Arrays;

import weka.core.Instances;

/**
 * A unilabel UCS using the RT transform.
 * 
 * @author Miltiadis Allamanis
 * 
 */
public class RTUCS extends AbstractLearningClassifierSystem {

	/**
	 * The input file used (.arff).
	 */
	private final String inputFile;

	/**
	 * The number of full iterations to train the UCS.
	 */
	private final int iterations;

	/**
	 * The GA crossover rate.
	 */
	private final float CROSSOVER_RATE = (float) SettingsLoader
			.getNumericSetting("crossoverRate", .8);

	/**
	 * The GA mutation rate.
	 */
	private final double MUTATION_RATE = (float) SettingsLoader
			.getNumericSetting("mutationRate", .04);

	/**
	 * The GA activation rate.
	 */
	private final int THETA_GA = (int) SettingsLoader.getNumericSetting(
			"thetaGA", 300);

	/**
	 * The number of bits to use for representing continuous variables.
	 */
	private final int PRECISION_BITS = (int) SettingsLoader.getNumericSetting(
			"precisionBits", 5);

	/**
	 * The UCS alpha parameter.
	 */
	private final double UCS_ALPHA = SettingsLoader.getNumericSetting(
			"UCS_Alpha", .1);

	/**
	 * The UCS n power parameter.
	 */
	private final int UCS_N = (int) SettingsLoader.getNumericSetting("UCS_N",
			10);

	/**
	 * The accuracy threshold parameter.
	 */
	private final double UCS_ACC0 = SettingsLoader.getNumericSetting(
			"UCS_Acc0", .99);

	/**
	 * The learning rate (beta) parameter.
	 */
	private final double UCS_LEARNING_RATE = SettingsLoader.getNumericSetting(
			"UCS_beta", .1);

	/**
	 * The UCS experience threshold.
	 */
	private final int UCS_EXPERIENCE_THRESHOLD = (int) SettingsLoader
			.getNumericSetting("UCS_Experience_Theshold", 10);

	/**
	 * The attribute generalization rate.
	 */
	private final double ATTRIBUTE_GENERALIZATION_RATE = SettingsLoader
			.getNumericSetting("AttributeGeneralizationRate", 0.33);

	/**
	 * The matchset GA run probability.
	 */
	private final double MATCHSET_GA_RUN_PROBABILITY = SettingsLoader
			.getNumericSetting("GAMatchSetRunProbability", 0.01);

	/**
	 * Percentage of only updates (and no exploration).
	 */
	private final double UPDATE_ONLY_ITERATION_PERCENTAGE = SettingsLoader
			.getNumericSetting("UpdateOnlyPercentage", .1);

	/**
	 * The number of labels used at the dmlUCS.
	 */
	private final int numberOfLabels;

	/**
	 * The rule representation used.
	 */
	final UniLabelRepresentation rep;

	/**
	 * Constructor.
	 * 
	 * @throws IOException
	 */
	public RTUCS() throws IOException {
		inputFile = SettingsLoader.getStringSetting("filename", "");
		numberOfLabels = (int) SettingsLoader.getNumericSetting(
				"numberOfLabels", 1);
		iterations = (int) SettingsLoader.getNumericSetting("trainIterations",
				1000);
		final int populationSize = (int) SettingsLoader.getNumericSetting(
				"populationSize", 1000);

		final IGeneticAlgorithmStrategy ga = new SteadyStateGeneticAlgorithm(
				new RouletteWheelSelector(
						AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION,
						true), new SinglePointCrossover(this), CROSSOVER_RATE,
				new UniformBitMutation(MUTATION_RATE), THETA_GA, this);

		rep = new UniLabelRepresentation(inputFile, PRECISION_BITS,
				numberOfLabels, ATTRIBUTE_GENERALIZATION_RATE, this);

		rep.setClassificationStrategy(rep.new ThresholdClassificationStrategy());

		final RTUCSUpdateAlgorithm strategy = new RTUCSUpdateAlgorithm(
				UCS_ALPHA, UCS_N, UCS_ACC0, UCS_LEARNING_RATE,
				UCS_EXPERIENCE_THRESHOLD, MATCHSET_GA_RUN_PROBABILITY, ga,
				THETA_GA, 1, numberOfLabels, this);

		this.setElements(rep, strategy);

		rulePopulation = new ClassifierSet(
				new FixedSizeSetWorstFitnessDeletion(
						this,
						populationSize,
						new RouletteWheelSelector(
								AbstractUpdateStrategy.COMPARISON_MODE_DELETION,
								true)));
	}

	@Override
	public int[] classifyInstance(double[] instance) {
		return getClassifierTransformBridge().classify(
				this.getRulePopulation(), instance);
	}

	@Override
	public AbstractLearningClassifierSystem createNew() {
		try {
			return new RTUCS();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String[] getEvaluationNames() {
		final String[] names = { "Accuracy(pcut)", "Recall(pcut)",
				"HammingLoss(pcut)", "ExactMatch(pcut)", "Accuracy(ival)",
				"Recall(ival)", "HammingLoss(ival)", "ExactMatch(ival)" };
		return names;
	}

	@Override
	public double[] getEvaluations(Instances testSet) {
		final double[] results = new double[8];
		Arrays.fill(results, 0);

		final ThresholdClassificationStrategy str = rep.new ThresholdClassificationStrategy();
		rep.setClassificationStrategy(str);

		str.proportionalCutCalibration(this.instances, rulePopulation,
				(float) SettingsLoader.getNumericSetting(
						"datasetLabelCardinality", 1));
		final AccuracyRecallEvaluator accEval = new AccuracyRecallEvaluator(
				testSet, false, this, AccuracyRecallEvaluator.TYPE_ACCURACY);
		results[0] = accEval.getMetric(this);

		final AccuracyRecallEvaluator recEval = new AccuracyRecallEvaluator(
				testSet, false, this, AccuracyRecallEvaluator.TYPE_RECALL);
		results[1] = recEval.getMetric(this);

		final HammingLossEvaluator hamEval = new HammingLossEvaluator(testSet,
				false, numberOfLabels, this);
		results[2] = hamEval.getMetric(this);

		final ExactMatchEvalutor testEval = new ExactMatchEvalutor(testSet,
				false, this);
		results[3] = testEval.getMetric(this);

		final AccuracyRecallEvaluator selfAcc = new AccuracyRecallEvaluator(
				instances, false, this, AccuracyRecallEvaluator.TYPE_ACCURACY);
		final InternalValidation ival = new InternalValidation(this, str,
				selfAcc);
		ival.calibrate(15);

		results[4] = accEval.getMetric(this);
		results[5] = recEval.getMetric(this);
		results[6] = hamEval.getMetric(this);
		results[7] = testEval.getMetric(this);

		return results;
	}

	/**
	 * Runs the Direct-ML-UCS.
	 * 
	 */
	@Override
	public void train() {
		trainSet(iterations, rulePopulation);
		updatePopulation((int) (UPDATE_ONLY_ITERATION_PERCENTAGE * iterations),
				rulePopulation);
	}
}
