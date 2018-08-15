import org.epochx.epox.Node;

public class StayFunction extends Node{
	// This may remain null, depending on the constructor used.
	private TrainingAndTestSet trainingAndTestSet;
	private RobotsAndGraphList robotsAndGraphList;
	private RobotsAndGraph robotsAndGraph;
	int robotIndex;
	Robot robot;
	
	/**
	 * Constructs a <code>StayFunction</code> with no child nodes, but the
	 * given robot which will be held internally. This makes the function a
	 * terminal node with arity zero.
	 * 
	 * @param robot the robot instance that should be operated upon when this node
	 * is evaluated. An exception will be thrown if this argument is null.
	 */
	public StayFunction(final TrainingAndTestSet trainingAndTestSet) {
		super();
		
		if (trainingAndTestSet == null) {
			throw new IllegalArgumentException("trainingAndTestSet must not be null");
		}
		
		this.trainingAndTestSet = trainingAndTestSet;
		this.robotsAndGraphList = this.trainingAndTestSet.robotsAndGraphList;
		this.robotsAndGraph = this.robotsAndGraphList.list[this.robotsAndGraphList.currExample];
	}
	
	/**
	 * Evaluates this function. The robot stays at the same node.
	 * The return type of this function node is Void,
	 * and so the value returned from this method is undefined.
	 */
	@Override
	public Void evaluate() {
		if(this.trainingAndTestSet.testBool) {
			this.robotsAndGraphList = this.trainingAndTestSet.testSet;
		}
		
		this.robotsAndGraph = this.robotsAndGraphList.list[this.robotsAndGraphList.currExample];
		this.robotIndex = this.robotsAndGraph.currentRobot;
		this.robot = this.robotsAndGraph.robotList.get(this.robotIndex);
		// Robot just stays at the same node
		//System.out.println("ROBOT: " + this.robot.objective.value + " STAY");
		
		return null;
	}

	/**
	 * Returns the identifier of this function which is MOVE.
	 */
	@Override
	public String getIdentifier() {
		return "STAY";
	}
	
	@Override
	public Class<?> getReturnType(final Class<?> ... inputTypes) {
		if ((getArity() == 0) && (inputTypes.length == 0)){
			return Void.class;
		}
		else{
			return null;
		}
	}
}


