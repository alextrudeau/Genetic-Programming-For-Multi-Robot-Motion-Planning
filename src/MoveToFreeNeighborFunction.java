import java.util.ArrayList;
import java.util.Random;

import org.epochx.epox.Node;

public class MoveToFreeNeighborFunction extends Node{
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
	public MoveToFreeNeighborFunction(final TrainingAndTestSet trainingAndTestSet) {
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
		//System.out.println("MOVE TO FREE NEIGHBOR");
		this.robotIndex = this.robotsAndGraph.currentRobot;
		this.robot = this.robotsAndGraph.robotList.get(this.robotIndex);
		//System.out.println("ROBOT: " + this.robot.objective.value);
		//System.out.println("ROBOT CURR NODE: " + this.robot.currNode.value);
		
		// Check to see if all neighbors have been visited -> reset list
		ArrayList<GraphNode> neighbors = this.robot.currNode.neighbors;
		boolean visitedAllNeighbors = true;
		for(int i = 0; i < neighbors.size(); i++){
			if(!this.robot.nodesVisited.contains(neighbors.get(i))){
				visitedAllNeighbors = false;
			}
		}
		
		if(visitedAllNeighbors){
			for(int i = 0; i < neighbors.size(); i++){
				this.robot.nodesVisited.remove(neighbors.get(i));
			}
		}
		
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
		
		boolean neighborFound = false;
		
		for(int i = 0; i < freeNeighbors.size(); i++){
			//System.out.println("Free Neighbor: " + freeNeighbors.get(i).value);
			if(!this.robot.path.contains(freeNeighbors.get(i))){
				neighborFound = true;
				//System.out.println("TAKING STEP TO FREE NEIGHBOR");
				// Take step to free neighbor
				this.robot.path = this.robot.adjList.Dijkstra(this.robot.currNode, freeNeighbors.get(i));
				this.robot.path.remove(0);
				this.robot.step();
				
				// Restore Robot's path toward objective
				this.robot.path = this.robot.adjList.Dijkstra(this.robot.currNode, this.robot.objective);
				this.robot.path.remove(0);
				break;
			}
			else{
				//System.out.println("FREE NEIGHBOR ON PATH");
			}
		}
		
		
		// Should Robot take step to a visited node if all unvisited neighbors are covered?
		
		if(!neighborFound && visitedNeighbors.size() > 0){
			int visitedNeighborIndex = 0;
			//System.out.println("GOING TO VISITED NODE: " + visitedNeighbors.get(visitedNeighborIndex).value);
			this.robot.path = this.robot.adjList.Dijkstra(this.robot.currNode, visitedNeighbors.get(visitedNeighborIndex));
			this.robot.path.remove(0);
			this.robot.step();
			
			// Restore Robot's path toward objective
			this.robot.path = this.robot.adjList.Dijkstra(this.robot.currNode, this.robot.objective);
			this.robot.path.remove(0);
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
		return "MOVE-TO-FREE-NEIGHBOR";
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
