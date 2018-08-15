import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.epochx.epox.Node;

public class IfTwoRobotsOnEachOthersPath extends Node{
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
	public IfTwoRobotsOnEachOthersPath(final Node robotsAndGraphList, final Node child1, final Node child2) {
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
	public IfTwoRobotsOnEachOthersPath(final TrainingAndTestSet trainingAndTestSet, final Node child1, final Node child2) {
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
	public IfTwoRobotsOnEachOthersPath(final TrainingAndTestSet trainingAndTestSet) {
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
		//System.out.println("IF TWO ROBOTS ON EACH OTHER'S PATH");
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
		
		if(onEachOthersPath){
			//System.out.println("ON EACH OTHER'S PATH");
			child1.evaluate();
		}
		else{
			//System.out.println("NOT ON EACH OTHER'S PATH");
			child2.evaluate();
		}

		return null;
	}

	/**
	 * Returns the identifier of this function which is IF-NEIGHBOR-ON-PATH-IS-FREE.
	 */
	@Override
	public String getIdentifier() {
		return "IF-TWO-ROBOTS-ON-EACH-OTHERS-PATH";
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
