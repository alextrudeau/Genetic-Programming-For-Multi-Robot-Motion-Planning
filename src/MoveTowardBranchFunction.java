import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.epochx.epox.Node;

public class MoveTowardBranchFunction extends Node{
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
	public MoveTowardBranchFunction(final TrainingAndTestSet trainingAndTestSet) {
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
		//System.out.println("MOVE TOWARD BRANCH");
		this.robotIndex = this.robotsAndGraph.currentRobot;
		this.robot = this.robotsAndGraph.robotList.get(this.robotIndex);
		this.robot.goingToBranch = true;
		//System.out.println("ROBOT: " + this.robot.objective.value);
		//System.out.println("ROBOT CURR NODE: " + this.robot.currNode.value);
		
		ArrayList<GraphNode> branches = this.robot.getBranches(this.robot).get(0);
		
		HashMap<GraphNode, Integer> branchDistances = new HashMap<GraphNode, Integer>();
		if(branches.size() > 0){
			for(int i = 0; i < branches.size(); i++){
				if(!this.robot.gpBranchesVisited.contains(branches.get(i))){
					int distance = this.robot.adjList.Dijkstra(this.robot.currNode, branches.get(i)).size();
					branchDistances.put(branches.get(i), distance);
				}
			}
			
			if(branchDistances.size() > 0){
				int minDistance = Integer.MAX_VALUE;
				GraphNode nextBranch = null;
				
				Iterator hmIterator = branchDistances.entrySet().iterator();
				
				while (hmIterator.hasNext()) {
			        Map.Entry pair = (Map.Entry)hmIterator.next();
			        GraphNode key = (GraphNode)pair.getKey();
			        Integer value = (Integer)pair.getValue();
			        if((Integer)pair.getValue() < minDistance){
			        	minDistance = (Integer)pair.getValue();
			        	nextBranch = (GraphNode)pair.getKey();
			        }
			        hmIterator.remove();
			    }
				
				this.robot.gpBranchesVisited.add(nextBranch);
				
				//System.out.println("Robot " + this.robot.objective.value + " GOING TO BRANCH: " + nextBranch.value);
				
				//System.out.println("Closest Branch: " + closestBranch.value);
				if(nextBranch != this.robot.currNode){
					this.robot.path = this.robot.adjList.Dijkstra(this.robot.currNode, nextBranch);
					this.robot.path.remove(0);
					this.robot.step();
				}
				else{
					//System.out.println("Already at branch");
				}
			}
			else{
				//System.out.println("Clear Robot " + this.robot.objective.value + " VISITED BRANCHES");
				this.robot.gpBranchesVisited = new ArrayList<GraphNode>();
			}
		}
		
		return null;
	}

	/**
	 * Returns the identifier of this function which is MOVE.
	 */
	@Override
	public String getIdentifier() {
		return "MOVE-TOWARD-BRANCH";
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