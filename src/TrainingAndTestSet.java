import java.util.Random;

public class TrainingAndTestSet {

	public RobotsAndGraphList trainingSet;
	public RobotsAndGraphList testSet;
	public RobotsAndGraphList robotsAndGraphList;
	public boolean testBool;
	
	public TrainingAndTestSet(RobotsAndGraphList training, RobotsAndGraphList test){
		this.trainingSet = training;
		this.testSet = test;
		// this.robotsAndGraphList points to either trainingSet or testSet depending upon whether we're in the training or test phase
		this.robotsAndGraphList = this.trainingSet;
		this.testBool = false;
	}
	
	public void switchToTest() {
		this.testBool = true;
		this.robotsAndGraphList = this.testSet;
	}
	
}
