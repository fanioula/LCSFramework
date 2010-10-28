package gr.auth.ee.lcs.data;

import gr.auth.ee.lcs.classifiers.ClassifierSet;
import gr.auth.ee.lcs.classifiers.Classifier;

/** 
 *  An abstract *S-LCS update algorithm as described in Tzima-Mitkas paper
 */
public abstract class AbstractSLCSUpdateAlgorithm extends UpdateAlgorithmFactoryAndStrategy {

  /** 
   *  The abstract function used to calculate the fitness of a classifier
   */
  public abstract void updateFitness(Classifier aClassifier, ClassifierSet correctSet);

  
  public Object createStateClassifierObject() {
	  //TODO: Intial parameters
	return new GenericSLCSClassifierData();
  }

  
  /**
   * Updates the set
   * setA is the match set
   * setB is the correct set  
   */
  public final void updateSet(ClassifierSet setA, ClassifierSet setB){
	  for (int i=0;i<setA.getNumberOfMacroclassifiers();i++){
		  Classifier cl=setA.getClassifier(i);
		  GenericSLCSClassifierData data=((GenericSLCSClassifierData)cl.updateData);
		  data.ns=(data.ns*data.msa+setB.getTotalNumerosity())/(data.msa+1);
		  data.msa++;
		  updateFitness(cl,setB);
		  this.updateSubsumption(cl);
	  }
  
  }

}