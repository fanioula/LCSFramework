/**
 * 
 */
package gr.auth.ee.lcs.impementations;

import gr.auth.ee.lcs.AbstractLearningClassifierSystem;
import gr.auth.ee.lcs.ArffLoader;
import gr.auth.ee.lcs.LCSTrainTemplate;
import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.classifiers.populationcontrol.FixedSizeSetWorstFitnessDeletion;
import gr.auth.ee.lcs.classifiers.populationcontrol.PostProcessPopulationControl;
import gr.auth.ee.lcs.classifiers.populationcontrol.SortPopulationControl;
import gr.auth.ee.lcs.data.AbstractUpdateStrategy;
import gr.auth.ee.lcs.data.IEvaluator;
import gr.auth.ee.lcs.data.representations.UniLabelRepresentation;
import gr.auth.ee.lcs.data.representations.UniLabelRepresentation.ThresholdClassificationStrategy;
import gr.auth.ee.lcs.data.updateAlgorithms.ASLCSUpdateAlgorithm;
import gr.auth.ee.lcs.evaluators.AccuracyEvaluator;
import gr.auth.ee.lcs.evaluators.ExactMatchEvalutor;
import gr.auth.ee.lcs.evaluators.FileLogger;
import gr.auth.ee.lcs.evaluators.HammingLossEvaluator;
import gr.auth.ee.lcs.geneticalgorithm.IGeneticAlgorithmStrategy;
import gr.auth.ee.lcs.geneticalgorithm.algorithms.SteadyStateGeneticAlgorithm;
import gr.auth.ee.lcs.geneticalgorithm.operators.SinglePointCrossover;
import gr.auth.ee.lcs.geneticalgorithm.operators.UniformBitMutation;
import gr.auth.ee.lcs.geneticalgorithm.selectors.RouletteWheelSelector;
import gr.auth.ee.lcs.utilities.InstanceToDoubleConverter;

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
		final String file = "/home/miltiadis/Desktop/datasets/genbase2.arff";
		final int numOfLabels = 27;
		final int iterations = 200;
		final int populationSize = 6000;
		RTASLCS rtaslcs = new RTASLCS(file, iterations, populationSize,
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
	private final float CROSSOVER_RATE = (float) 0.8;

	/**
	 * The GA mutation rate.
	 */
	private final double MUTATION_RATE = (float) .04;

	/**
	 * The GA activation rate.
	 */
	private final int THETA_GA = 900;

	/**
	 * The frequency at which callbacks will be called for evaluation.
	 */
	private final int CALLBACK_RATE = 200;

	/**
	 * The number of bits to use for representing continuous variables
	 */
	private final int PRECISION_BITS = 7;

	/**
	 * The ASLCS n power parameter.
	 */
	private final int ASLCS_N = 10;

	/**
	 * The accuracy threshold parameter.
	 */
	private final double ASLCS_ACC0 = .99;

	/**
	 * The ASLCS experience threshold.
	 */
	private final int ASLCS_EXPERIENCE_THRESHOLD = 20;

	/**
	 * The post-process experience threshold used.
	 */
	private final int POSTPROCESS_EXPERIENCE_THRESHOLD = 5;

	/**
	 * Coverage threshold for post processing.
	 */
	private final int POSTPROCESS_COVERAGE_THRESHOLD = 0;

	/**
	 * Post-process threshold for fitness.
	 */
	private final double POSTPROCESS_FITNESS_THRESHOLD = 0.0;

	/**
	 * The number of labels used at the dmlUCS.
	 */
	private final int numberOfLabels;

	private ThresholdClassificationStrategy str;

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

		IGeneticAlgorithmStrategy ga = new SteadyStateGeneticAlgorithm(
				new RouletteWheelSelector(
						AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION,
						true), new SinglePointCrossover(this), CROSSOVER_RATE,
				new UniformBitMutation(MUTATION_RATE), THETA_GA, this);

		UniLabelRepresentation rep = new UniLabelRepresentation(inputFile,
				PRECISION_BITS, numberOfLabels, .7, this);
		str = rep.new ThresholdClassificationStrategy();
		rep.setClassificationStrategy(str);

		ASLCSUpdateAlgorithm update = new ASLCSUpdateAlgorithm(ASLCS_N,
				ASLCS_ACC0, ASLCS_EXPERIENCE_THRESHOLD, .01, ga, this);

		this.setElements(rep, update);
	}

	/**
	 * Runs the Direct-ML-UCS.
	 * 
	 * @throws IOException
	 */
	@Override
	public void train() {
		LCSTrainTemplate myExample = new LCSTrainTemplate(CALLBACK_RATE, this);

		ClassifierSet rulePopulation = new ClassifierSet(
				new FixedSizeSetWorstFitnessDeletion(
						populationSize,
						new RouletteWheelSelector(
								AbstractUpdateStrategy.COMPARISON_MODE_DELETION,
								true)));

		ArffLoader loader = new ArffLoader(this);
		try {
			loader.loadInstances(inputFile, true);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		AccuracyEvaluator acc = new AccuracyEvaluator(loader.trainSet, true,
				this);
		final IEvaluator eval = new ExactMatchEvalutor(this.instances, true,
				this);
		myExample.registerHook(new FileLogger(inputFile + "_result.txt", eval));
		myExample.registerHook(acc);
		myExample.train(iterations, rulePopulation);

		// rulePopulation.print();
		System.out.println("Post process...");
		// rulePopulation.print();
		PostProcessPopulationControl postProcess = new PostProcessPopulationControl(
				POSTPROCESS_EXPERIENCE_THRESHOLD,
				POSTPROCESS_COVERAGE_THRESHOLD, POSTPROCESS_FITNESS_THRESHOLD,
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLOITATION);
		SortPopulationControl sort = new SortPopulationControl(
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLOITATION);
		postProcess.controlPopulation(rulePopulation);
		sort.controlPopulation(rulePopulation);
		eval.evaluateSet(rulePopulation);

		System.out.println("Evaluating on test set (pre-calibration)");
		ExactMatchEvalutor testEval = new ExactMatchEvalutor(loader.testSet,
				true, this);
		testEval.evaluateSet(rulePopulation);
		HammingLossEvaluator hamEval = new HammingLossEvaluator(loader.testSet,
				true, numberOfLabels, this);
		hamEval.evaluateSet(rulePopulation);
		AccuracyEvaluator accEval = new AccuracyEvaluator(loader.testSet, true,
				this);
		accEval.evaluateSet(rulePopulation);

		str.proportionalCutCalibration(this.instances, rulePopulation,
				(float) 1.252);
		// rulePopulation.print();
		// ClassifierSet.saveClassifierSet(rulePopulation, "set");

		eval.evaluateSet(rulePopulation);

		System.out.println("Evaluating on test set");

		testEval.evaluateSet(rulePopulation);

		hamEval.evaluateSet(rulePopulation);
		accEval.evaluateSet(rulePopulation);

		str.proportionalCutCalibration(
				InstanceToDoubleConverter.convert(loader.testSet),
				rulePopulation, (float) 1.252);

		System.out.println("Evaluating on test set (Pcut on test)");

		testEval.evaluateSet(rulePopulation);

		hamEval.evaluateSet(rulePopulation);
		accEval.evaluateSet(rulePopulation);

	}
}
