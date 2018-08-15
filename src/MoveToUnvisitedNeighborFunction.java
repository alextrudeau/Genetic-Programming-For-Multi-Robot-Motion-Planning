import java.util.ArrayList;
import java.util.Random;

import org.epochx.epox.Node;

public class MoveToUnvisitedNeighborFunction extends Node{
	// This may remain null, depending on the constructor used.
	private TrainingAndTestSet trainingAndTestSet;
	private RobotsAndGraphList robotsAndGraphList;
	private RobotsAndGraph robotsAndGraph;
	private Robot robot;
	private int robotIndex;
	
	/**
	 * Constructs a <code>MoveToFreeNeighborFunction</code> with no child nodes, but the
	 * given robot which will be held internally. This makes the function a
	 * terminal node with arity zero.
	 * 
	 * @param robot the robot instance that should be operated upon when this node
	 * is evaluated. An exception will be thrown if this argument is null.
	 */
	public MoveToUnvisitedNeighborFunction(final TrainingAndTestSet trainingAndTestSet) {
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
		//System.out.println("MOVE TO UNVISITED NEIGHBOR");
		this.robotIndex = this.robotsAndGraph.currentRobot;
		this.robot = this.robotsAndGraph.robotList.get(this.robotIndex);
		//System.out.println("ROBOT: " + this.robot.objective.value);
		//System.out.println("ROBOT CURR NODE: " + this.robot.currNode.value);
		
		ArrayList<GraphNode> freeNeighbors = this.robot.getFreeAdjacentNodes();
		ArrayList<GraphNode> visitedNeighbors = new ArrayList<GraphNode>();
		
		for(int i = 0; i < freeNeighbors.size(); i++){
			if(this.robot.nodesVisited.contains(freeNeighbors.get(i))){
				//System.out.println("ADD VISITED NEIGHBOR");
				visitedNeighbors.add(freeNeighbors.get(i));
				freeNeighbors.remove(i);
			}
		}
		
		if(freeNeighbors.size() < 1){
			//System.out.println("NO FREE NEIGHBORS");
		}
		
		if(freeNeighbors.size() > 0){
			Random rand = new Random();
			int neighborIndex = rand.nextInt(freeNeighbors.size());
			
			this.robot.path = this.robot.adjList.Dijkstra(this.robot.currNode, freeNeighbors.get(neighborIndex));
			this.robot.path.remove(0);
			this.robot.step();
			//System.out.println("GOING TO UNVISITED FREE NEIGHBOR");
			// Restore Robot's path toward objective
			this.robot.path = this.robot.adjList.Dijkstra(this.robot.currNode, this.robot.objective);
			this.robot.path.remove(0);
		}
		else if(visitedNeighbors.size() > 0){
			Random rand = new Random();
			int neighborIndex = rand.nextInt(visitedNeighbors.size());
			
			this.robot.path = this.robot.adjList.Dijkstra(this.robot.currNode, visitedNeighbors.get(neighborIndex));
			this.robot.path.remove(0);
			this.robot.step();
			//System.out.println("GOING TO VISITED FREE NEIGHBOR");
			// Restore Robot's path toward objective
			this.robot.path = this.robot.adjList.Dijkstra(this.robot.currNode, this.robot.objective);
			this.robot.path.remove(0);
		}
		
		return null;
	}

	/**
	 * Returns the identifier of this function which is MOVE.
	 */
	@Override
	public String getIdentifier() {
		return "MOVE-TO-UNVISITED-NEIGHBOR";
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