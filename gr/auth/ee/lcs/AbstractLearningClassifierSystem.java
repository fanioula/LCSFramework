/**
 * 
 */
package gr.auth.ee.lcs;

import gr.auth.ee.lcs.classifiers.Classifier;
import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.classifiers.populationcontrol.InadequeteClassifierDeletionStrategy;
import gr.auth.ee.lcs.classifiers.populationcontrol.PostProcessPopulationControl;
import gr.auth.ee.lcs.classifiers.populationcontrol.SortPopulationControl;
import gr.auth.ee.lcs.data.AbstractUpdateStrategy;
import gr.auth.ee.lcs.data.ClassifierTransformBridge;
import gr.auth.ee.lcs.data.IEvaluator;
import gr.auth.ee.lcs.utilities.ExtendedBitSet;
import gr.auth.ee.lcs.utilities.SettingsLoader;

import java.util.Vector;

import weka.core.Instances;

/**
 * An abstract LCS class to be implemented by all LCSs.
 * 
 * @author Miltiadis Allamanis
 * 
 */
public abstract class AbstractLearningClassifierSystem {

	/**
	 * The train set.
	 */
	public double[][] instances;

	/**
	 * The LCS instance transform bridge.
	 */
	private ClassifierTransformBridge transformBridge;

	/**
	 * The Abstract Update Algorithm Strategy of the LCS.
	 */
	private AbstractUpdateStrategy updateStrategy;

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
	 * The rule population.
	 */
	protected ClassifierSet rulePopulation;

	/**
	 * A vector of all evaluator hooks.
	 */
	private final Vector<IEvaluator> hooks;

	/**
	 * Frequency of the hook callback execution.
	 */
	private final int hookCallbackRate;

	/**
	 * Constructor.
	 * 
	 */
	public AbstractLearningClassifierSystem() {
		hooks = new Vector<IEvaluator>();
		hookCallbackRate = (int) SettingsLoader.getNumericSetting(
				"callbackRate", 100);
	}

	public abstract AbstractLearningClassifierSystem createNew();

	/**
	 * Return the LCS's classifier transform bridge.
	 * 
	 * @return the lcs's classifier transform bridge
	 */
	public final ClassifierTransformBridge getClassifierTransformBridge() {
		return transformBridge;
	}

	public abstract String[] getEvaluationNames();

	public abstract double[] getEvaluations(Instances testSet);

	/**
	 * Create a new classifier for the specific LCS.
	 * 
	 * @return the new classifier.
	 */
	public final Classifier getNewClassifier() {
		return Classifier.createNewClassifier(this);
	}

	/**
	 * Return a new classifier object for the specific LCS given a chromosome.
	 * 
	 * @param chromosome
	 *            the chromosome to be replicated
	 * @return a new classifier containing information about the LCS
	 */
	public final Classifier getNewClassifier(final ExtendedBitSet chromosome) {
		return Classifier.createNewClassifier(this, chromosome);
	}

	/**
	 * Returns the LCS's update strategy.
	 * 
	 * @return the update strategy
	 */
	public final AbstractUpdateStrategy getUpdateStrategy() {
		return updateStrategy;
	}

	/**
	 * Post-process results.
	 */
	public final void postProcess() {
		final PostProcessPopulationControl postProcess = new PostProcessPopulationControl(
				POSTPROCESS_EXPERIENCE_THRESHOLD,
				POSTPROCESS_COVERAGE_THRESHOLD, POSTPROCESS_FITNESS_THRESHOLD,
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLOITATION);
		final SortPopulationControl sort = new SortPopulationControl(
				AbstractUpdateStrategy.COMPARISON_MODE_EXPLOITATION);
		postProcess.controlPopulation(rulePopulation);
		sort.controlPopulation(rulePopulation);
	}

	public final void printSet() {
		rulePopulation.print();
	}

	/**
	 * Register an evaluator to be called during training.
	 * 
	 * @param evaluator
	 *            the evaluator to register
	 * @return true if the evaluator has been registered successfully
	 */
	public final boolean registerHook(final IEvaluator evaluator) {
		return hooks.add(evaluator);
	}

	/**
	 * Save the rules to the given filename.
	 * 
	 * @param filename
	 */
	public final void saveRules(String filename) {
		ClassifierSet.saveClassifierSet(rulePopulation, filename);
	}

	/**
	 * Constructor.
	 * 
	 * @param bridge
	 *            the classifier transform bridge
	 * @param update
	 *            the update strategy
	 */
	public final void setElements(final ClassifierTransformBridge bridge,
			final AbstractUpdateStrategy update) {
		transformBridge = bridge;
		updateStrategy = update;
	}

	/**
	 * Run the LCS and train it.
	 */
	public abstract void train();

	/**
	 * Train a classifier set with all train instances.
	 * 
	 * @param iterations
	 *            the number of full iterations (one iteration the LCS is
	 *            trained with all instances) to train the LCS
	 * @param population
	 *            the population of the classifiers to train.
	 * @param evolve
	 *            set true to evolve population, false to only update it
	 */
	public final void trainSet(final int iterations,
			final ClassifierSet population, final boolean evolve) {

		final int numInstances = instances.length;
		final InadequeteClassifierDeletionStrategy del = new InadequeteClassifierDeletionStrategy(
				0);

		int repetition = 0;
		int trainsBeforeHook = 0;
		final double instanceProb = (1. / (numInstances));
		while (repetition < iterations) {
			while ((trainsBeforeHook < hookCallbackRate)
					&& (repetition < iterations)) {
				System.out.print('.');

				for (int i = 0; i < numInstances; i++) {
					trainWithInstance(population, i);
					if (Math.random() < instanceProb)
						del.controlPopulation(population);
				}
				repetition++;
				trainsBeforeHook++;

			}
			executeCallbacks(population);
			trainsBeforeHook = 0;

		}

	}

	/**
	 * Train with instance main template. Trains the classifier set with a
	 * single instance.
	 * 
	 * @param population
	 *            the classifier's popoulation
	 * @param dataInstanceIndex
	 *            the index of the training data instance
	 */
	public final void trainWithInstance(final ClassifierSet population,
			final int dataInstanceIndex) {

		final ClassifierSet matchSet = population
				.generateMatchSet(dataInstanceIndex);

		getUpdateStrategy().updateSet(population, matchSet, dataInstanceIndex,
				true);

	}

	/**
	 * Unregister an evaluator.
	 * 
	 * @param evaluator
	 *            the evaluator to register
	 * @return true if the evaluator has been unregisterd successfully
	 */
	public final boolean unregisterEvaluator(final IEvaluator evaluator) {
		return hooks.remove(evaluator);
	}

	/**
	 * Update population with all train instances but do not perform evolution.
	 * 
	 * @param iterations
	 *            the number of full iterations (one iteration the LCS is
	 *            trained with all instances) to update the LCS
	 * @param population
	 *            the population of the classifiers to update.
	 */
	public final void updatePopulation(final int iterations,
			final ClassifierSet population) {
		trainSet(iterations, population, false);
	}

	/**
	 * Execute hooks.
	 * 
	 * @param aSet
	 *            the set on which to run the callbacks
	 */
	private void executeCallbacks(final ClassifierSet aSet) {
		for (int i = 0; i < hooks.size(); i++) {
			hooks.elementAt(i).evaluateSet(aSet);
		}
	}

	/**
	 * Train population with all train instances and perform evolution.
	 * 
	 * @param iterations
	 *            the number of full iterations (one iteration the LCS is
	 *            trained with all instances) to train the LCS
	 * @param population
	 *            the population of the classifiers to train.
	 */
	protected final void trainSet(final int iterations,
			final ClassifierSet population) {
		trainSet(iterations, population, true);
	}
}
