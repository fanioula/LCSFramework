package gr.auth.ee.lcs.geneticalgorithm;

import gr.auth.ee.lcs.classifiers.Classifier;
import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.classifiers.DummySizeControlStrategy;



/** 
 *  A steady-stage GA that selects two individuals from a set (with probability proportional to their total fitness) and performs a crossover and mutation corrects the classifier (if needed) and adds it to the set
 *  @author Miltos Allamanis
 * 
 */
public class NichedSteadyStateFitnessProportionalGeneticAlgorithm implements IGeneticAlgorithmStrategy {

  protected INaturalSelector gaSelector;

  protected IBinaryGeneticOperator crossoverOp;
  
  protected IUnaryGeneticOperator mutationOp;
  
  private int timestamp=0;
  
  public NichedSteadyStateFitnessProportionalGeneticAlgorithm(INaturalSelector gaSelector, IBinaryGeneticOperator crossoverOp, IUnaryGeneticOperator mutationOp){
	  this.gaSelector=gaSelector;
	  this.crossoverOp=crossoverOp;
	  this.mutationOp=mutationOp;
  }
  
  @Override
  /**
   * Evovles a set
   * If the set is empty an exception will be thrown
   */
  public void evolveSet(ClassifierSet evolveSet, ClassifierSet population) {
	 
	  timestamp++;
	  
	for (int i=0;i<evolveSet.getNumberOfMacroclassifiers();i++){
		evolveSet.getClassifier(i).experience++;
		evolveSet.getClassifier(i).timestamp=timestamp;
	}
	  
	ClassifierSet parents=new ClassifierSet(new DummySizeControlStrategy());
	//Select parents
	gaSelector.select(1, evolveSet, parents);
	Classifier parentA=parents.getClassifier(0);
	parents.deleteClassifier(0);
	gaSelector.select(1, evolveSet, parents);
	Classifier parentB=parents.getClassifier(0);
	parents.deleteClassifier(0);
	
	//Reproduce
	for (int i=0;i<2;i++){
		//produce a child
		Classifier child=crossoverOp.operate(parentA, parentB);
		child=mutationOp.operate(child);
		population.addClassifier(child,1);
	}
  }

}