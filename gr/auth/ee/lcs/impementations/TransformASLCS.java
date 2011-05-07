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
import gr.auth.ee.lcs.data.representations.GenericMultiLabelRepresentation;
import gr.auth.ee.lcs.data.representations.GenericMultiLabelRepresentation.VotingClassificationStrategy;
import gr.auth.ee.lcs.data.updateAlgorithms.ASLCSUpdateAlgorithm;
import gr.auth.ee.lcs.evaluators.AccuracyEvaluator;
import gr.auth.ee.lcs.evaluators.AllSingleLabelEvaluator;
import gr.auth.ee.lcs.evaluators.ExactMatchEvalutor;
import gr.auth.ee.lcs.evaluators.HammingLossEvaluator;
import gr.auth.ee.lcs.geneticalgorithm.IGeneticAlgorithmStrategy;
import gr.auth.ee.lcs.geneticalgorithm.algorithms.SteadyStateGeneticAlgorithm;
import gr.auth.ee.lcs.geneticalgorithm.operators.SinglePointCrossover;
import gr.auth.ee.lcs.geneticalgorithm.operators.UniformBitMutation;
import gr.auth.ee.lcs.geneticalgorithm.selectors.RouletteWheelSelector;
import gr.auth.ee.lcs.utilities.BinaryRelevanceSelector;
import gr.auth.ee.lcs.utilities.ILabelSelector;

import java.io.IOException;

/**
 * A Transformation ml-ASLCS.
 * 
 * @author Miltos Allamanis
 * 
 */
public class TransformASLCS extends AbstractLearningClassifierSystem {
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {
		final String file = "/home/miltiadis/Desktop/datasets/genbase2.arff";
		final int numOfLabels = 27;
		final int iterations = 600;
		final int populationSize = 2000;
		final float lc = (float) 1.252;
		final BinaryRelevanceSelector selector = new BinaryRelevanceSelector(
				numOfLabels);
		TransformASLCS trucs = new TransformASLCS(file, iterations,
				populationSize, numOfLabels, lc, selector);
		trucs.train();

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
	 * The label selector used for trasformation.
	 */
	private final ILabelSelector selector;

	/**
	 * The target label cardinality.
	 */
	private final float targetLC;

	/**
	 * The GA mutation rate.
	 */
	private static final double MUTATION_RATE = (float) .04;

	/**
	 * The GA activation rate.
	 */
	private static final int THETA_GA = 1000;

	/**
	 * The frequency at which callbacks will be called for evaluation.
	 */
	private static final int CALLBACK_RATE = 500;

	/**
	 * The number of bits to use for representing continuous variables.
	 */
	private static final int PRECISION_BITS = 5;

	/**
	 * The ASLCS n power parameter.
	 */
	private static final int ASLCS_N = 10;

	/**
	 * The accuracy threshold parameter.
	 */
	private static final double ASLCS_ACC0 = .99;

	/**
	 * The ASLCS experience threshold.
	 */
	private static final int ASLCS_EXPERIENCE_THRESHOLD = 5;

	/**
	 * The post-process experience threshold used.
	 */
	private static final int POSTPROCESS_EXPERIENCE_THRESHOLD = 0;

	/**
	 * Coverage threshold for post processing.
	 */
	private static final int POSTPROCESS_COVERAGE_THRESHOLD = 0;

	/**
	 * Post-process threshold for fitness.
	 */
	private static final double POSTPROCESS_FITNESS_THRESHOLD = 0;

	/**
	 * The number of labels used at the dmlUCS.
	 */
	private final int numberOfLabels;

	GenericMultiLabelRepresentation rep;

	/**
	 * Constructor.
	 * 
	 * @param filename
	 *            the filename of the AS-LCS
	 * @param iterations
	 *            the number of iterations to run
	 * @param populationSize
	 *            the size of the population to use
	 * @param numOfLabels
	 *            the number of labels in the problem
	 * @throws IOException
	 */
	public TransformASLCS(final String filename, final int iterations,
			final int populationSize, final int numOfLabels,
			final float problemLC, ILabelSelector transformSelector)
			throws IOException {
		inputFile = filename;
		this.iterations = iterations;
		this.populationSize = populationSize;
		this.numberOfLabels = numOfLabels;
		this.targetLC = problemLC;
		this.selector = transformSelector;

		IGeneticAlgorithmStrategy ga = new SteadyStateGeneticAlgorithm(
				new RouletteWheelSelector(
						AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION,
						true), new SinglePointCrossover(this), CROSSOVER_RATE,
				new UniformBitMutation(MUTATION_RATE), THETA_GA, this);

		rep = new GenericMultiLabelRepresentation(inputFile, PRECISION_BITS,
				numberOfLabels, GenericMultiLabelRepresentation.EXACT_MATCH, 0,
				.7, this);
		rep.setClassificationStrategy(rep.new BestFitnessClassificationStrategy());

		ASLCSUpdateAlgorithm strategy = new ASLCSUpdateAlgorithm(ASLCS_N,
				ASLCS_ACC0, ASLCS_EXPERIENCE_THRESHOLD, .01, ga, this);

		this.setElements(rep, strategy);

	}

	/**
	 * Runs the Direct-ML-UCS.
	 * 
	 * @throws IOException
	 */
	@Override
	public void train() {
		LCSTrainTemplate myExample = new LCSTrainTemplate(CALLBACK_RATE, this);

		ClassifierSet rulePopulation = new ClassifierSet(null);

		ArffLoader loader = new ArffLoader(this);
		try {
			loader.loadInstances(inputFile, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		final IEvaluator eval = new ExactMatchEvalutor(this.instances, true,
				this);
		// myExample.registerHook(new FileLogger(inputFile + "_result.txt",
		// eval));
		AllSingleLabelEvaluator slEval = new AllSingleLabelEvaluator(
				loader.trainSet, numberOfLabels, true, this);
		// myExample.registerHook(slEval);

		do {
			System.out.println("Training Classifier Set");
			rep.activateLabel(selector);
			ClassifierSet brpopulation = new ClassifierSet(
					new FixedSizeSetWorstFitnessDeletion(
							populationSize,
							new RouletteWheelSelector(
									AbstractUpdateStrategy.COMPARISON_MODE_DELETION,
									true)));
			myExample.train(iterations, brpopulation);
			myExample.updatePopulation(iterations / 10, brpopulation);
			rep.reinforceDeactivatedLabels(brpopulation);
			rulePopulation.merge(brpopulation);

		} while (selector.next());
		rep.activateAllLabels();

		System.out.println("Post process...");
		PostProcessPopulationControl postProcess = new PostProcessPopulationControl(
				POSTPROCESS_EXPERIENCE_THRESHOLD,
				POSTPROCESS_COVERAGE_THRESHOLD, POSTPROCESS_FITNESS_THRESHOLD,
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLOITATION);
		SortPopulationControl sort = new SortPopulationControl(
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLOITATION);
		postProcess.controlPopulation(rulePopulation);
		sort.controlPopulation(rulePopulation);

		ExactMatchEvalutor trainEval = new ExactMatchEvalutor(loader.trainSet,
				true, this);
		trainEval.evaluateSet(rulePopulation);
		HammingLossEvaluator trainhamEval = new HammingLossEvaluator(
				loader.trainSet, true, numberOfLabels, this);
		trainhamEval.evaluateSet(rulePopulation);
		AccuracyEvaluator trainaccEval = new AccuracyEvaluator(loader.trainSet,
				true, this);
		trainaccEval.evaluateSet(rulePopulation);

		// rulePopulation.print();
		// ClassifierSet.saveClassifierSet(rulePopulation, "set");
		AllSingleLabelEvaluator teEval = new AllSingleLabelEvaluator(
				loader.testSet, numberOfLabels, true, this);
		teEval.evaluateSet(rulePopulation);
		eval.evaluateSet(rulePopulation);
		slEval.evaluateSet(rulePopulation);
		System.out.println("Evaluating on test set");
		ExactMatchEvalutor testEval = new ExactMatchEvalutor(loader.testSet,
				true, this);
		testEval.evaluateSet(rulePopulation);
		HammingLossEvaluator hamEval = new HammingLossEvaluator(loader.testSet,
				true, numberOfLabels, this);
		hamEval.evaluateSet(rulePopulation);
		AccuracyEvaluator accEval = new AccuracyEvaluator(loader.testSet, true,
				this);
		accEval.evaluateSet(rulePopulation);
		VotingClassificationStrategy str = rep.new VotingClassificationStrategy(
				targetLC);
		rep.setClassificationStrategy(str);
		str.proportionalCutCalibration(this.instances, rulePopulation);
		System.out.println("Evaluating on test set (voting)");
		testEval.evaluateSet(rulePopulation);
		hamEval.evaluateSet(rulePopulation);
		accEval.evaluateSet(rulePopulation);

	}

}
