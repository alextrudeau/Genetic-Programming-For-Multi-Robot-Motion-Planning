import org.epochx.epox.Node;

public class MoveTowardObjectiveFunction extends Node{
	private TrainingAndTestSet trainingAndTestSet;
	private RobotsAndGraphList robotsAndGraphList;
	private RobotsAndGraph robotsAndGraph;
	private Robot robot;
	private int robotIndex;
	
	/**
	 * Constructs a <code>MoveTowardObjectiveFunction</code> with no child nodes, but the
	 * given robot which will be held internally. This makes the function a
	 * terminal node with arity zero.
	 * 
	 * @param robot the robot instance that should be operated upon when this node
	 * is evaluated. An exception will be thrown if this argument is null.
	 */
	public MoveTowardObjectiveFunction(final TrainingAndTestSet trainingAndTestSet) {
		super();
		
		if (trainingAndTestSet == null) {
			throw new IllegalArgumentException("trainingAndTestSet must not be null");
		}
		
		this.trainingAndTestSet = trainingAndTestSet;
		this.robotsAndGraphList = this.trainingAndTestSet.robotsAndGraphList;
		this.robotsAndGraph = this.robotsAndGraphList.list[this.robotsAndGraphList.currExample];
	}
	
	/**
	 * Evaluates this function. The robot is moved to the adjacent node on its current path.
	 * The return type of this function node is Void,
	 * and so the value returned from this method is undefined.
	 */
	@Override
	public Void evaluate() {
		if(this.trainingAndTestSet.testBool) {
			this.robotsAndGraphList = this.trainingAndTestSet.testSet;
		}
		
		this.robotsAndGraph = this.robotsAndGraphList.list[this.robotsAndGraphList.currExample];
		//System.out.println("MOVE TOWARD OBJECTIVE");
		this.robotIndex = this.robotsAndGraph.currentRobot;
		this.robot = this.robotsAndGraph.robotList.get(this.robotIndex);
		//System.out.println("ROBOT: " + this.robot.objective.value);
		//System.out.println("ROBOT CURR NODE: " + this.robot.currNode.value);
		
		//this.robot.path = this.robotsAndGraph.adjList.Dijkstra(this.robot.currNode, this.robot.objective);
		//this.robot.path.remove(0);
		//System.out.println("Take step to: " + this.robot.path.get(0).value);
		this.robot.step();
		
		// Check if robot is solved
		if(this.robot.currNode == this.robot.objective){
			//System.out.println("AT OBJECTIVE");
			this.robot.solved = true;
		}
		
		/*
		if(this.robotsAndGraph.seqn > 0){
			this.robotsAndGraph.seqn--;
		}
		else if(this.robotsAndGraph.seqn == 1){
			this.robotsAndGraph.seqn--;
			this.robotsAndGraph.currentTimeStep++;
		}
		else{
			this.robotsAndGraph.currentTimeStep++;
		}
		*/
		return null;
	}

	/**
	 * Returns the identifier of this function which is MOVE.
	 */
	@Override
	public String getIdentifier() {
		return "MOVE-TOWARD-OBJECTIVE";
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
