import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.epochx.epox.Node;

public class IfTwoBotsOnEachOthersPath extends Node{
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
	public IfTwoBotsOnEachOthersPath(final TrainingAndTestSet trainingAndTestSet, final Node child1, final Node child2) {
		super(child1, child2);
		
		if (trainingAndTestSet == null) {
			throw new IllegalArgumentException("trainingAndTestSet must not be null");
		}
		
		this.trainingAndTestSet = trainingAndTestSet;
		this.robotsAndGraphList = this.trainingAndTestSet.robotsAndGraphList;
		this.robotsAndGraph = this.robotsAndGraphList.list[this.robotsAndGraphList.currExample];
	}
	
	public IfTwoBotsOnEachOthersPath(final TrainingAndTestSet trainingAndTestSet) {
		this(trainingAndTestSet, null, null);
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
		//System.out.println("IF TWO ROBOTS ON EACH OTHER'S PATH");
		//System.out.println("NODE CURR EXAMPLE: " + this.robotsAndGraphList.currExample);
		RobotsAndGraph evalRobotsAndGraph = this.robotsAndGraph;

		this.robotIndex = this.robotsAndGraph.currentRobot;
		this.robot = evalRobotsAndGraph.robotList.get(this.robotIndex);
		
		//System.out.println("ROBOT: " + this.robot.objective.value);
		
		this.robot.directNetwork = evalRobotsAndGraph.adjList.directCommunication(this.robot.currNode, 2);
		this.robot.directNetwork.remove(this.robot);
		
		/*
		for(int j = 0; j < this.robot.directNetwork.size(); j++){
			this.robot.directNetwork.get(j).path = evalRobotsAndGraph.adjList.Dijkstra(this.robot.directNetwork.get(j).currNode, this.robot.directNetwork.get(j).objective);
			this.robot.directNetwork.get(j).path.remove(0);
		}
		*/
		
		boolean onEachOthersPath = false;
		
		if(this.robot.directNetwork.size() < 1){
			//System.out.println("DIRECT NETWORK EMPTY");
		}
		
		for(int i = 0; i < this.robot.directNetwork.size(); i++){
			//System.out.println("ROBOT " + this.robot.directNetwork.get(i).objective.value + " IN NETWORK, CURR NODE: " + this.robot.directNetwork.get(i).currNode.value + ", OBJECTIVE: " + this.robot.directNetwork.get(i).objective.value);
			//System.out.println("ROBOT " + this.robot.directNetwork.get(i).objective.value + "'S PATH:");
			
			for(int j = 0; j < this.robot.directNetwork.get(i).path.size(); j++){
				//System.out.println(this.robot.directNetwork.get(i).path.get(j).value);
			}
			
			if(this.robot.directNetwork.get(i).path.contains(this.robot.currNode) && this.robot.path.contains(this.robot.directNetwork.get(i).currNode)){
				onEachOthersPath = true;
				
				ArrayList<GraphNode> branches = this.robot.getBranches(this.robot).get(0);
				
				HashMap<GraphNode, Integer> branchDistances = new HashMap<GraphNode, Integer>();
				if(branches.size() > 0){
					for(int k = 0; k < branches.size(); k++){
						if(!this.robot.gpBranchesVisited.contains(branches.get(k))){
							int distance = this.robot.adjList.Dijkstra(this.robot.currNode, branches.get(k)).size();
							branchDistances.put(branches.get(k), distance);
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
						
						this.robot.goingToBranch = true;
						
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
						
						this.robot.directNetwork.get(i).goingToBranch = true;
						this.robot.directNetwork.get(i).path = this.robot.directNetwork.get(i).adjList.Dijkstra(this.robot.directNetwork.get(i).currNode, nextBranch);
						this.robot.directNetwork.get(i).path.remove(0);
						
						//System.out.println("ROBOT " + this.robot.objective.value + " ON NODE: " + this.robot.currNode.value);
						//System.out.println("ROBOT " + this.robot.directNetwork.get(i).objective.value + " ON NODE: " + this.robot.directNetwork.get(i).currNode.value);
						//System.out.println("ROBOTS " + this.robot.objective.value + " AND " + this.robot.directNetwork.get(i).objective.value + " GOING TO BRANCH: " + nextBranch.value);
					}
					else{
						//System.out.println("Clear Robot " + this.robot.objective.value + " VISITED BRANCHES");
						this.robot.gpBranchesVisited = new ArrayList<GraphNode>();
					}
				}
				
				break;
			}
		}

		return null;
	}

	/**
	 * Returns the identifier of this function which is MOVE.
	 */
	@Override
	public String getIdentifier() {
		return "IF-TWO-BOTS-ON-EACH-OTHERS-PATH";
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