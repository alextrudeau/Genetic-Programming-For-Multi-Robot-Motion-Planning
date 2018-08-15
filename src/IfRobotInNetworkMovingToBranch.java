import org.epochx.epox.Node;

public class IfRobotInNetworkMovingToBranch extends Node{
	// This may remain null, depending on the constructor used.
	private TrainingAndTestSet trainingAndTestSet;
	private RobotsAndGraphList robotsAndGraphList;
	private RobotsAndGraph robotsAndGraph;
	private Robot robot;
	private int robotIndex;
	
	/**
	 * Constructs an IfNeighborOnPathIsFree with three child nodes. The first child
	 * must have a return-type of Robot.
	 * 
	 * @param robot the robot child upon which the condition is made.
	 * @param child1 The first conditionally evaluated child node.
	 * @param child2 The second conditionally evaluated child node.
	 */
	public IfRobotInNetworkMovingToBranch(final Node robotsAndGraphList, final Node child1, final Node child2) {
		super(robotsAndGraphList, child1, child2);
	}

	/**
	 * Constructs an <code>IfNeighborOnPathIsFree</code> with two child nodes, and
	 * a robot which will be held internally. This makes the function with arity
	 * of two. Note that this differs from the alternative constructors which
	 * take three child nodes, one of which has a Robot return type.
	 * 
	 * @param robot the robot instance that should be operated upon when this node
	 * is evaluated. An exception will be thrown if this argument is null.
	 * @param child1 The first conditionally evaluated child node.
	 * @param child2 The second conditionally evaluated child node.
	 */
	public IfRobotInNetworkMovingToBranch(final TrainingAndTestSet trainingAndTestSet, final Node child1, final Node child2) {
		super(child1, child2);
		
		if (trainingAndTestSet == null) {
			throw new IllegalArgumentException("trainingAndTestSet must not be null");
		}
		
		this.trainingAndTestSet = trainingAndTestSet;
		this.robotsAndGraphList = this.trainingAndTestSet.robotsAndGraphList;
		this.robotsAndGraph = this.robotsAndGraphList.list[this.robotsAndGraphList.currExample];
	}
	
	/**
	 * Constructs an <code>IfFoodAheadFunction</code> with two null child nodes,
	 * and an ant which will be held internally. This makes the function with
	 * arity of two. Note that this differs from the alternative constructors
	 * which take three child nodes, one of which has an Ant return type.
	 * 
	 * @param ant the ant instance that should be operated upon when this node
	 * is evaluated. An exception will be thrown if this argument is null.
	 */
	public IfRobotInNetworkMovingToBranch(final TrainingAndTestSet trainingAndTestSet) {
		this(trainingAndTestSet, null, null);
	}

	/**
	 * Evaluates this function. The robot checks whether the next node on its path is free.
	 * If the adjacent node is free, the first child is evaluated. Otherwise the second child
	 * is evaluated. The return type of this function node is Void,
	 * and so the value returned from this method is undefined.
	 */
	@Override
	public Void evaluate() {
		if(this.trainingAndTestSet.testBool) {
			this.robotsAndGraphList = this.trainingAndTestSet.testSet;
		}
		
		this.robotsAndGraph = this.robotsAndGraphList.list[this.robotsAndGraphList.currExample];
		//System.out.println("IF ROBOT IN NETWORK MOVING TO BRANCH");
		//System.out.println("NODE CURR EXAMPLE: " + this.robotsAndGraphList.currExample);
		RobotsAndGraph evalRobotsAndGraph;
		Node child1;
		Node child2;

		if (getArity() == 2) {
			evalRobotsAndGraph = this.robotsAndGraph;
			child1 = getChild(0);
			child2 = getChild(1);
		} else {
			evalRobotsAndGraph = (RobotsAndGraph) getChild(0).evaluate();
			child1 = getChild(1);
			child2 = getChild(2);
		}
		
		this.robotIndex = this.robotsAndGraph.currentRobot;
		this.robot = evalRobotsAndGraph.robotList.get(this.robotIndex);
		
		//System.out.println("ROBOT: " + this.robot.objective.value);
		
		this.robot.directNetwork = evalRobotsAndGraph.adjList.directCommunication(this.robot.currNode, 2);
		this.robot.directNetwork.remove(this.robot);
		
		boolean neighborMovingToBranch = false;
		
		for(int j = 0; j < this.robot.directNetwork.size(); j++){
			if(this.robot.directNetwork.get(j).goingToBranch){
				neighborMovingToBranch = true;
			}
		}
		
		if(neighborMovingToBranch){
			//System.out.println("ROBOT IN NETWORK MOVING TO BRANCH");
			child1.evaluate();
		}
		else{
			//System.out.println("NO ROBOT IN NETWORK MOVING TO BRANCH");
			child2.evaluate();
		}

		return null;
	}

	/**
	 * Returns the identifier of this function which is IF-NEIGHBOR-ON-PATH-IS-FREE.
	 */
	@Override
	public String getIdentifier() {
		return "IF-ROBOT-IN-NETWORK-MOVING-TO-BRANCH";
	}

	/**
	 * Returns this function node's return type for the given child input types.
	 * If the arity of this node is two, and there are two inputs of type Void,
	 * then the return type will be Void. If the arity is three, and there are
	 * three input types, the first of which is a sub-type of Robot then the
	 * return type of this function will be Void. In all other cases this method
	 * will return <code>null</code> to indicate that the inputs are invalid.
	 * 
	 * @return The Void class or null if the input type is invalid.
	 */
	@Override
	public Class<?> getReturnType(final Class<?> ... inputTypes) {
		if ((getArity() == 2)
				&& (inputTypes.length == 2)
				&& (inputTypes[0] == Void.class)
				&& (inputTypes[1] == Void.class)) {
			return Void.class;
		} else if ((getArity() == 3) 
				&& (inputTypes.length == 3)
				&& Robot.class.isAssignableFrom(inputTypes[0])
				&& (inputTypes[1] == Void.class)
				&& (inputTypes[2] == Void.class)) {
			return Void.class;
		} else {
			return null;
		}
	}
}