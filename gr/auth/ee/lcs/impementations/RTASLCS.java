/**
 * 
 */
package gr.auth.ee.lcs.impementations;

import gr.auth.ee.lcs.AbstractLearningClassifierSystem;
import gr.auth.ee.lcs.ArffTrainTestLoader;
import gr.auth.ee.lcs.LCSTrainTemplate;
import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.classifiers.populationcontrol.FixedSizeSetWorstFitnessDeletion;
import gr.auth.ee.lcs.classifiers.populationcontrol.PostProcessPopulationControl;
import gr.auth.ee.lcs.classifiers.populationcontrol.SortPopulationControl;
import gr.auth.ee.lcs.data.AbstractUpdateStrategy;
import gr.auth.ee.lcs.data.IEvaluator;
import gr.auth.ee.lcs.data.representations.complex.UniLabelRepresentation;
import gr.auth.ee.lcs.data.representations.complex.UniLabelRepresentation.ThresholdClassificationStrategy;
import gr.auth.ee.lcs.data.updateAlgorithms.ASLCSUpdateAlgorithm;
import gr.auth.ee.lcs.evaluators.AccuracyRecallEvaluator;
import gr.auth.ee.lcs.evaluators.ExactMatchEvalutor;
import gr.auth.ee.lcs.evaluators.FileLogger;
import gr.auth.ee.lcs.evaluators.HammingLossEvaluator;
import gr.auth.ee.lcs.geneticalgorithm.IGeneticAlgorithmStrategy;
import gr.auth.ee.lcs.geneticalgorithm.algorithms.SteadyStateGeneticAlgorithm;
import gr.auth.ee.lcs.geneticalgorithm.operators.SinglePointCrossover;
import gr.auth.ee.lcs.geneticalgorithm.operators.UniformBitMutation;
import gr.auth.ee.lcs.geneticalgorithm.selectors.RouletteWheelSelector;
import gr.auth.ee.lcs.utilities.InstanceToDoubleConverter;
import gr.auth.ee.lcs.utilities.SettingsLoader;

import java.io.IOException;

/**
 * An Rank-and-Threshold AS-LCS Update Algorithm.
 * 
 * @author Miltos Allamanis
 * 
 */
public class RTASLCS extends AbstractLearningClassifierSystem {
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		SettingsLoader.loadSettings();
		final String file = SettingsLoader.getStringSetting("filename", "");
		final int numOfLabels = (int) SettingsLoader.getNumericSetting(
				"numberOfLabels", 1);
		final int iterations = (int) SettingsLoader.getNumericSetting(
				"trainIterations", 1000);
		final int populationSize = (int) SettingsLoader.getNumericSetting(
				"populationSize", 1500);
		final RTASLCS rtaslcs = new RTASLCS(file, iterations, populationSize,
				numOfLabels);
		rtaslcs.train();

	}

	/**
	 * The input file used (.arff).
	 */
	private final String inputFile;

	/**
	 * The number of full iterations to train the UCS.
	 */
	private final int iterations;

	/**
	 * The size of the population to use.
	 */
	private final int populationSize;

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
			"thetaGA", 100);

	/**
	 * The frequency at which callbacks will be called for evaluation.
	 */
	private final int CALLBACK_RATE = (int) SettingsLoader.getNumericSetting(
			"callbackRate", 100);

	/**
	 * The number of bits to use for representing continuous variables.
	 */
	private final int PRECISION_BITS = (int) SettingsLoader.getNumericSetting(
			"precisionBits", 5);

	/**
	 * The UCS n power parameter.
	 */
	private final int ASLCS_N = (int) SettingsLoader.getNumericSetting(
			"ASLCS_N", 10);

	/**
	 * The accuracy threshold parameter.
	 */
	private final double ASLCS_ACC0 = SettingsLoader.getNumericSetting(
			"ASLCS_Acc0", .99);

	/**
	 * The UCS experience threshold.
	 */
	private final int ASLCS_EXPERIENCE_THRESHOLD = (int) SettingsLoader
			.getNumericSetting("ASLCS_ExperienceTheshold", 10);

	/**
	 * The post-process experience threshold used.
	 */
	private final int POSTPROCESS_EXPERIENCE_THRESHOLD = (int) SettingsLoader
			.getNumericSetting("PostProcess_Experience_Theshold", 0);

	/**
	 * Coverage threshold for post processing.
	 */
	private final int POSTPROCESS_COVERAGE_THRESHOLD = (int) SettingsLoader
			.getNumericSetting("PostProcess_Coverage_Theshold", 0);

	/**
	 * Post-process threshold for fitness.
	 */
	private final double POSTPROCESS_FITNESS_THRESHOLD = SettingsLoader
			.getNumericSetting("PostProcess_Fitness_Theshold", 0);

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
	 * Problem LC.
	 */
	private final float LABEL_CARDINALITY = (float) SettingsLoader
			.getNumericSetting("datasetLabelCardinality", 1);

	/**
	 * The number of labels used at the dmlUCS.
	 */
	private final int numberOfLabels;

	/**
	 * The threshold classification strategy used for the RT method.
	 */
	private final ThresholdClassificationStrategy str;

	/**
	 * Constructor.
	 * 
	 * @param filename
	 *            the filename of the ASLCS
	 * @param iterations
	 *            the number of iterations to run
	 * @param populationSize
	 *            the size of the population to use
	 * @param numOfLabels
	 *            the number of labels in the problem
	 * @throws IOException
	 */
	public RTASLCS(final String filename, final int iterations,
			final int populationSize, final int numOfLabels) throws IOException {
		inputFile = filename;
		this.iterations = iterations;
		this.populationSize = populationSize;
		this.numberOfLabels = numOfLabels;

		final IGeneticAlgorithmStrategy ga = new SteadyStateGeneticAlgorithm(
				new RouletteWheelSelector(
						AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION,
						true), new SinglePointCrossover(this), CROSSOVER_RATE,
				new UniformBitMutation(MUTATION_RATE), THETA_GA, this);

		final UniLabelRepresentation rep = new UniLabelRepresentation(
				inputFile, PRECISION_BITS, numberOfLabels,
				ATTRIBUTE_GENERALIZATION_RATE, this);
		str = rep.new ThresholdClassificationStrategy();
		rep.setClassificationStrategy(str);

		final ASLCSUpdateAlgorithm update = new ASLCSUpdateAlgorithm(ASLCS_N,
				ASLCS_ACC0, ASLCS_EXPERIENCE_THRESHOLD,
				MATCHSET_GA_RUN_PROBABILITY, ga, this);

		this.setElements(rep, update);

		rulePopulation = new ClassifierSet(
				new FixedSizeSetWorstFitnessDeletion(
						populationSize,
						new RouletteWheelSelector(
								AbstractUpdateStrategy.COMPARISON_MODE_DELETION,
								true)));
	}

	/**
	 * Runs the Direct-ML-UCS.
	 * 
	 * @throws IOException
	 */
	@Override
	public void train() {
		final LCSTrainTemplate myExample = new LCSTrainTemplate(CALLBACK_RATE,
				this);

		final ArffTrainTestLoader loader = new ArffTrainTestLoader(this);
		try {
			loader.loadInstances(inputFile, true);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		final AccuracyRecallEvaluator acc = new AccuracyRecallEvaluator(
				loader.trainSet, true, this,
				AccuracyRecallEvaluator.TYPE_ACCURACY);
		final IEvaluator eval = new ExactMatchEvalutor(this.instances, true,
				this);
		myExample.registerHook(new FileLogger(inputFile + "_result.txt", eval));
		myExample.registerHook(acc);
		myExample.train(iterations, rulePopulation);
		myExample.updatePopulation(
				(int) (iterations * UPDATE_ONLY_ITERATION_PERCENTAGE),
				rulePopulation);

		// rulePopulation.print();
		System.out.println("Post process...");
		// rulePopulation.print();
		final PostProcessPopulationControl postProcess = new PostProcessPopulationControl(
				POSTPROCESS_EXPERIENCE_THRESHOLD,
				POSTPROCESS_COVERAGE_THRESHOLD, POSTPROCESS_FITNESS_THRESHOLD,
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLOITATION);
		final SortPopulationControl sort = new SortPopulationControl(
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLOITATION);
		postProcess.controlPopulation(rulePopulation);
		sort.controlPopulation(rulePopulation);
		eval.evaluateSet(rulePopulation);

		System.out.println("Evaluating on test set (pre-calibration)");
		final ExactMatchEvalutor testEval = new ExactMatchEvalutor(
				loader.testSet, true, this);
		testEval.evaluateSet(rulePopulation);
		final HammingLossEvaluator hamEval = new HammingLossEvaluator(
				loader.testSet, true, numberOfLabels, this);
		hamEval.evaluateSet(rulePopulation);
		final AccuracyRecallEvaluator accEval = new AccuracyRecallEvaluator(
				loader.testSet, true, this,
				AccuracyRecallEvaluator.TYPE_ACCURACY);
		accEval.evaluateSet(rulePopulation);

		str.proportionalCutCalibration(this.instances, rulePopulation,
				LABEL_CARDINALITY);
		// rulePopulation.print();
		// ClassifierSet.saveClassifierSet(rulePopulation, "set");

		eval.evaluateSet(rulePopulation);

		System.out.println("Evaluating on test set");

		testEval.evaluateSet(rulePopulation);

		hamEval.evaluateSet(rulePopulation);
		accEval.evaluateSet(rulePopulation);

		str.proportionalCutCalibration(
				InstanceToDoubleConverter.convert(loader.testSet),
				rulePopulation, LABEL_CARDINALITY);

		System.out.println("Evaluating on test set (Pcut on test)");

		testEval.evaluateSet(rulePopulation);

		hamEval.evaluateSet(rulePopulation);
		accEval.evaluateSet(rulePopulation);

	}
}
