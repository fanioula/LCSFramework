/**
 * 
 */
package gr.auth.ee.lcs.impementations;

import gr.auth.ee.lcs.AbstractLearningClassifierSystem;
import gr.auth.ee.lcs.FoldEvaluator;
import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.classifiers.populationcontrol.FixedSizeSetWorstFitnessDeletion;
import gr.auth.ee.lcs.data.AbstractUpdateStrategy;
import gr.auth.ee.lcs.data.representations.complex.SingleClassRepresentation;
import gr.auth.ee.lcs.data.updateAlgorithms.SSLCSUpdateAlgorithm;
import gr.auth.ee.lcs.evaluators.ExactMatchEvalutor;
import gr.auth.ee.lcs.geneticalgorithm.IGeneticAlgorithmStrategy;
import gr.auth.ee.lcs.geneticalgorithm.algorithms.SteadyStateGeneticAlgorithm;
import gr.auth.ee.lcs.geneticalgorithm.operators.SinglePointCrossover;
import gr.auth.ee.lcs.geneticalgorithm.operators.UniformBitMutation;
import gr.auth.ee.lcs.geneticalgorithm.selectors.TournamentSelector2;
import gr.auth.ee.lcs.utilities.SettingsLoader;

import java.io.IOException;

import weka.core.Instances;

/**
 * An SS-LCS implementation.
 * 
 * @author Miltiadis Allamanis
 * 
 */
public class SSLCS extends AbstractLearningClassifierSystem {
	/**
	 * The main for running SS-LCS.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		SettingsLoader.loadSettings();
		final String file = SettingsLoader.getStringSetting("filename", "");
		final SSLCS sslcs = new SSLCS();
		FoldEvaluator loader = new FoldEvaluator(10, sslcs, file);
		loader.evaluate();

	}

	/**
	 * The input file used (.arff).
	 */
	private final String inputFile;

	/**
	 * The number of full iterations to train the SS-LCS.
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
	 * The number of bits to use for representing continuous variables.
	 */
	private final int PRECISION_BITS = (int) SettingsLoader.getNumericSetting(
			"precisionBits", 5);

	/**
	 * The SSLCS penalty parameter.
	 */
	private final double SSLCS_PENALTY = SettingsLoader.getNumericSetting(
			"SSLCSPenaltyPercent", 10);

	/**
	 * The accuracy threshold parameter.
	 */
	private final double SSLCS_REWARD = SettingsLoader.getNumericSetting(
			"SSLCS_REWARD", 1);

	/**
	 * The SSLCS subsumption experience threshold.
	 */
	private final int SSLCS_EXPERIENCE_THRESHOLD = (int) SettingsLoader
			.getNumericSetting("SSLCSExperienceThreshold", 10);

	/**
	 * The SSLCS subsumption experience threshold.
	 */
	private final double SSLCS_FITNESS_THRESHOLD = SettingsLoader
			.getNumericSetting("SSLCSFitnessThreshold", .99);

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
	 * The problem representation.
	 */
	private final SingleClassRepresentation rep;

	/**
	 * The SS-LCS constructor.
	 * 
	 * @throws IOException
	 */
	public SSLCS() throws IOException {
		inputFile = SettingsLoader.getStringSetting("filename", "");
		iterations = (int) SettingsLoader.getNumericSetting("trainIterations",
				1000);
		populationSize = (int) SettingsLoader.getNumericSetting(
				"populationSize", 1500);

		final IGeneticAlgorithmStrategy ga = new SteadyStateGeneticAlgorithm(
				new TournamentSelector2(40, true,
						AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION),
				new SinglePointCrossover(this), CROSSOVER_RATE,
				new UniformBitMutation(MUTATION_RATE), THETA_GA, this);

		rep = new SingleClassRepresentation(inputFile, PRECISION_BITS,
				ATTRIBUTE_GENERALIZATION_RATE, this);
		rep.setClassificationStrategy(rep.new VotingClassificationStrategy());

		final SSLCSUpdateAlgorithm strategy = new SSLCSUpdateAlgorithm(
				SSLCS_REWARD, SSLCS_PENALTY, SSLCS_FITNESS_THRESHOLD,
				SSLCS_EXPERIENCE_THRESHOLD, MATCHSET_GA_RUN_PROBABILITY, ga,
				this);

		this.setElements(rep, strategy);

		rulePopulation = new ClassifierSet(
				new FixedSizeSetWorstFitnessDeletion(
						populationSize,
						new TournamentSelector2(80, true,
								AbstractUpdateStrategy.COMPARISON_MODE_DELETION)));
	}

	@Override
	public AbstractLearningClassifierSystem createNew() {
		try {
			return new SSLCS();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String[] getEvaluationNames() {
		String[] names = { "Accuracy" };
		return names;
	}

	@Override
	public double[] getEvaluations(Instances testSet) {
		double[] result = new double[1];
		final ExactMatchEvalutor testEval = new ExactMatchEvalutor(testSet,
				true, this);
		result[0] = testEval.evaluateSet(rulePopulation);
		return result;
	}

	/**
	 * Run the SS-LCS.
	 * 
	 */
	@Override
	public void train() {

		trainSet(iterations, rulePopulation);
		updatePopulation((int) (iterations * UPDATE_ONLY_ITERATION_PERCENTAGE),
				rulePopulation);

	}
}
