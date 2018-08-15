import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.*;

public class Robot extends GraphNode{

	public GraphNode currNode;
	public int priority;
	public GraphNode objective;
	public ArrayList<Robot> directNetwork;
	public ArrayList<Robot> completeNetwork;
	public ArrayList<GraphNode> path;
	public String status;
	String statuses[] = {"NORMAL", "PAUSED", "WAITING", "PUSHED", "STUCK", "SWAP_SET", "SWAP_CONTINUE", "SWAP_FINISH", "SWAPPING"};
	public int radius = 2;
	public Rectangle b = new Rectangle();
	public boolean solved;
	public ArrayList<Robot> solvedRobots;
	public Robot swappingBot;
	public GraphNode twig;
	public GraphNode branch;
	public GraphNode twigEnd;
	public boolean leader;
	public boolean follower;
	public boolean swapping;
	public ArrayList<GraphNode> twigsVisited;
	public ArrayList<GraphNode> branchesVisited;
	public ArrayList<GraphNode> nodesVisited;
	public AdjacencyList adjList;
	public GraphNode source;
	public int numMoves;
	public int stepsAfterSolved;
	public boolean goingToBranch;
	public boolean goingToTwig;
	public ArrayList<GraphNode> gpBranchesVisited;
	public boolean visitedObjective;
	public boolean higherPriorityBotsSolved;
	public ArrayList<GraphNode> ancestors;
	public boolean onPathOfLowerPriorityBot;
	public boolean movedTowardsDestination;
	public boolean gpSolved;
	public int numSwaps;
	
	public Robot(GraphNode currNode, GraphNode objective, int numNodes){
		super(currNode.value, currNode.p, currNode.r/2, Color.BLACK);
		this.currNode = currNode;
		this.source = currNode;
		this.objective = objective;
		this.status = "NORMAL";
		this.priority = Integer.MAX_VALUE;
		this.path = new ArrayList<GraphNode>();
		this.solvedRobots = new ArrayList<Robot>();
		this.solved = false;
		this.swappingBot = null;
		this.twig = null;
		this.branch = null;
		this.twigEnd = null;
		this.leader = false;
		this.follower = false;
		this.swapping = false;
		this.twigsVisited = new ArrayList<GraphNode>();
		this.branchesVisited = new ArrayList<GraphNode>();
		this.adjList = new AdjacencyList(numNodes);
		this.nodesVisited = new ArrayList<GraphNode>();
		this.numMoves = 0;
		this.stepsAfterSolved = 0;
		this.goingToBranch = false;
		this.goingToTwig = false;
		this.gpBranchesVisited = new ArrayList<GraphNode>();
		this.visitedObjective = false;
		this.higherPriorityBotsSolved = false;
		GraphNode tempNode = this.currNode;
		this.ancestors = new ArrayList<GraphNode>();
		while(tempNode.root == false){
			this.ancestors.add(tempNode.parent);
			tempNode = tempNode.parent;
		}
		this.onPathOfLowerPriorityBot = false;
		this.movedTowardsDestination = false;
		this.gpSolved = false;
		this.numSwaps = 0;
		setBoundary(b);
	}
	
	public void plan(Robot highestPriority){
		// Probably best if we determine highestPriority robot before calling plan so we don't have to repeat this for each robot
		//System.out.println("PLAN");
		
		// Check each time step to see if robot is solved if it isn't already solved
		if(!this.solved){
			this.higherPriorityBotsSolved = true;
			this.onPathOfLowerPriorityBot = false;
			for(int i = 0; i < this.adjList.nodeL.length; i++){
				if(this.adjList.nodeL[i].robot != null && this.adjList.nodeL[i].robot.priority < this.priority && !this.adjList.nodeL[i].robot.solved){
					this.higherPriorityBotsSolved = false;
				}
				if(this.adjList.nodeL[i].robot != null){
					GraphNode tempNode = this.adjList.nodeL[i];
					this.adjList.nodeL[i].robot.ancestors = new ArrayList<GraphNode>();
					while(tempNode.root == false){
						this.adjList.nodeL[i].robot.ancestors.add(tempNode.parent);
						tempNode = tempNode.parent;
					}
					if(this.adjList.nodeL[i].robot.priority > this.priority && this.adjList.nodeL[i].robot.ancestors.contains(this.currNode)){
						this.onPathOfLowerPriorityBot = true;
						// Reset visited objective
						this.visitedObjective = false;
					}
				}
			}
			//System.out.println("Visited Objective: " + this.visitedObjective);
			//System.out.println("Higher Priority Bots Solved: " + this.higherPriorityBotsSolved);
			//System.out.println("On Path Of Lower Priority Bot: " + this.onPathOfLowerPriorityBot);
			if(this.visitedObjective && this.higherPriorityBotsSolved && !this.onPathOfLowerPriorityBot){
				//System.out.println("SOLVED");
				this.solved = true;
			}
			else{
				//System.out.println("NOT SOLVED");
			}
		}
		
		//System.out.println("TWIGS VISITED: ");
		for(int i = 0; i < this.twigsVisited.size(); i++){
			//System.out.println(this.twigsVisited.get(i).value);
		}
		
		// Add to solved if solved
		//System.out.println("currNode: " + this.currNode.value + " objective: " + this.objective.value);
		/*
		if(this.currNode == this.objective && !this.solved){
			System.out.println("Robot " + this.objective.value + " is solved");
			this.solved = true;
			this.solvedRobots.add(this);
		}
		*/
		
		this.directNetwork = this.adjList.directCommunication(this.currNode, 2);
		
		this.getCompleteNetwork();
		
		// Checking to see if any robots in this robot's C(r) are WAITING for a swapping robot not in C(r)
		boolean robotsWaiting = false;
		boolean swappingInNetwork = false;
		boolean swappingOutOfNetwork = false;
		
		ArrayList<Robot> completeNetworkAndRobot = new ArrayList<Robot>();
		completeNetworkAndRobot.add(this);
		for(int i = 0; i < this.completeNetwork.size(); i++){
			completeNetworkAndRobot.add(this.completeNetwork.get(i));
		}
		
		// making checkSwap call for later use
		ArrayList<Robot> tuple = this.checkSwap();
		//System.out.println("CHECK SWAP TUPLE");
		//System.out.println("ROBOT: " + tuple.get(0).objective.value);
		if(tuple.get(1) == null){
			//System.out.println("ROBOT: null");
		}
		else{
			//System.out.println("ROBOT: " + tuple.get(1).objective.value);
		}
		
		// EXPERIMENT
		highestPriority = tuple.get(0);
		
		//System.out.println("HIGHEST PRIORITY: " + highestPriority.objective.value);
		
		for(int i = 0; i < completeNetworkAndRobot.size(); i++){
			if(completeNetworkAndRobot.get(i).status == "WAITING"){
				//System.out.println("Robot: " + completeNetworkAndRobot.get(i).objective.value + " WAITING");
				robotsWaiting = true;
			}
			// r* can be either swapping robot or highest priority robot
			if(completeNetworkAndRobot.get(i).status.contains("SWAP") || completeNetworkAndRobot.get(i) == highestPriority){
				//System.out.println("Robot: " + completeNetworkAndRobot.get(i).objective.value + " Status: " + completeNetworkAndRobot.get(i).status);
				swappingInNetwork = true;
			}
		}
		
		if(swappingInNetwork){
			//System.out.println("Swapping in Complete Network or Highest Priority Bot in Complete Network");
		}
		else
		{
			//System.out.println("No swapping in Complete Network and Highest priority bot not in complete network");
		}
		
		if(robotsWaiting){
			//System.out.println("Robots Waiting in Complete Network + this robot");
		}
		else
		{
			//System.out.println("Robots NOT Waiting in Complete Network + this robot");
		}
		
		// If a robot in the complete network is waiting and there's no robot swapping in the complete network,
		// there must be a robot swapping outside of the network
		if(robotsWaiting && !swappingInNetwork){
			swappingOutOfNetwork = true;
		}
		
		//System.out.println("robotsWaiting: " + robotsWaiting);
		//System.out.println("swappingInNetwork: " + swappingInNetwork);
		
		// Robots in C(r) are waiting and swapping robot not in C(r)
		if(robotsWaiting && swappingOutOfNetwork){
			//System.out.println("ROBOTS WAITING IN NETWORK + ROBOT SWAPPING OUT OF NETWORK");
			// If this robot is WAITING, it remains WAITING
			if(this.status == "WAITING"){
				//System.out.println("CURRENT ROBOT WAITING");
				this.status = "WAITING";
			}
			// If another robot in C(r) is WAITING, r remains PAUSED
			else{
				//System.out.println("CURRENT ROBOT PAUSED");
				this.status = "PAUSED";
			}
			// In both cases, r remains idle
		}
		// If checkSwap returns a tuple containing r
		else if(tuple.contains(this)){
			if(tuple.get(1) != null){
				//System.out.println("TWO ROBOTS SWAPPING");
				if(!tuple.get(0).status.contains("SWAP") || !tuple.get(1).status.contains("SWAP")){
					tuple.get(0).status = "SWAP_SET";
					tuple.get(1).status = "SWAP_SET";
				}
				this.swap();
			}
			else{
				//System.out.println("CURRENT ROBOT IS HIGHEST PRIORITY UNSOLVED ROBOT AND NOT SWAPPING WITH OTHER BOT");
				this.path = this.adjList.Dijkstra(this.currNode, this.objective);
				this.path.remove(0);
				this.step();
				this.status = "NORMAL";
			}
		}
		// Swapping bot in C(r)
		else if(this.completeNetwork.contains(tuple.get(0)) || (tuple.get(1) != null && this.completeNetwork.contains(tuple.get(1)))){
			//System.out.println("SWAPPING BOT IN COMPLETE NETWORK"); 
			this.Pushed2(highestPriority);
		}
		else if(this.greaterPriority()){
			//System.out.println("GREATER PRIORITY");
			this.status = "PAUSED";
		}
		// Robot r moves one closer to its objective
		else{
			//System.out.println("ONE CLOSER TO OBJECTIVE");
			// System.out.println("CALC PATH 2");
			this.path = this.adjList.Dijkstra(this.currNode, this.objective);
			// System.out.println("Old Path");
			for(int i = 0; i < this.path.size(); i++){
				// System.out.println(this.path.get(i).value);
			}
			this.path = this.adjList.Dijkstra(this.currNode, this.objective);
			this.path.remove(0);
			this.step();
			this.status = "NORMAL";
		}
	}
	
	// Condition in Pushed algorithm
	// Checking to see if a robot in C(r) is on a higher priority branch than C(r)
	public boolean greaterPriority(){
		for(int i = 0; i < this.completeNetwork.size(); i++){
			if((this.completeNetwork.get(i).currNode.priority > this.currNode.priority) && this.completeNetwork.get(i).path.contains(this.path.get(1))){
				return true;
			}
		}
		return false;
	}
	
	
	public ArrayList<Robot> checkSwap(){
		//System.out.println("CHECK SWAP");
		
		ArrayList<Integer> objectives = new ArrayList<Integer>();
		ArrayList<Robot> completeNetworkAndRobot = new ArrayList<Robot>();
		
		completeNetworkAndRobot.add(this);
		objectives.add(this.objective.value);
		
		for(int i = 0; i < this.completeNetwork.size(); i++){
			completeNetworkAndRobot.add(this.completeNetwork.get(i));
			objectives.add(this.completeNetwork.get(i).objective.value);
		}
		
		ArrayList<Robot> sortedNetwork = new ArrayList<Robot>();
		
		Collections.sort(objectives);
		for(int i = 0; i < objectives.size(); i++){
			for(int j = 0; j < completeNetworkAndRobot.size(); j++){
				if(completeNetworkAndRobot.get(j).objective.value == objectives.get(i)){
					sortedNetwork.add(completeNetworkAndRobot.get(j));
					break;
				}
			}
		}
		
		//System.out.println("Ordered Robots:");
		for(int i = 0; i < sortedNetwork.size(); i++){
			//System.out.println(sortedNetwork.get(i).objective.value);
		}
		
		completeNetworkAndRobot = sortedNetwork;
		
		// Need to find highest priority unsolved robot in [r, C(r)]
		Robot highestPriorityInNetwork = null;
		//System.out.println("FIND HIGHEST PRIORITY UNSOLVED BOT:");
		// Get highest-priority unsolved robot
		for(int i = 0; i < completeNetworkAndRobot.size(); i++){
			//System.out.println("ROBOT: " + completeNetworkAndRobot.get(i).priority + " Solved: " + completeNetworkAndRobot.get(i).solved + " Visited Objective: " + completeNetworkAndRobot.get(i).visitedObjective);
			if(completeNetworkAndRobot.get(i).status.contains("SWAP")){
				//System.out.println("0: Current highest priority is: " + completeNetworkAndRobot.get(i).objective.value);
				highestPriorityInNetwork = completeNetworkAndRobot.get(i);
				break;
			}
			else if(completeNetworkAndRobot.get(i).solved){
				//System.out.println("Continue 1");
				continue;
			}
			else if(completeNetworkAndRobot.get(i).currNode == completeNetworkAndRobot.get(i).objective){
				//System.out.println("Continue 1");
				continue;
			}
			/*
			// First if statement is for the case where a robot reaches its objective but doesn't become solved because there's lower priority bots under it.
			else if(completeNetworkAndRobot.get(i).visitedObjective){
				System.out.println("Continue 2");
				continue;
			}
			*/
			else if (highestPriorityInNetwork == null){
				//System.out.println("1: Current highest priority is: " + completeNetworkAndRobot.get(i).objective.value);
				highestPriorityInNetwork = completeNetworkAndRobot.get(i);
			}
			/*
			else if(highestPriorityInNetwork == null && !completeNetworkAndRobot.get(i).solved){
				System.out.println("1: Current highest priority is: " + completeNetworkAndRobot.get(i).objective.value);
				highestPriorityInNetwork = completeNetworkAndRobot.get(i);
				break;
			}
			// Added this condition because Swapping bots aren't supposed to be pushed
			else if(completeNetworkAndRobot.get(i).solved && completeNetworkAndRobot.get(i).status.contains("SWAP")){
				System.out.println("2: Current highest priority is: " + completeNetworkAndRobot.get(i).objective.value);
				highestPriorityInNetwork = completeNetworkAndRobot.get(i);
			}
			else if(completeNetworkAndRobot.get(i).solved || completeNetworkAndRobot.get(i).visitedObjective){
				System.out.println("Continue 2");
				continue;
			}
			else if((completeNetworkAndRobot.get(i).priority < highestPriorityInNetwork.priority) && (!completeNetworkAndRobot.get(i).solved)){
				System.out.println("3: Current highest priority is: " + completeNetworkAndRobot.get(i).objective.value);
				highestPriorityInNetwork = completeNetworkAndRobot.get(i);
			}
			*/
		}
		
		ArrayList<Robot> tuple = new ArrayList<Robot>();
		ArrayList<Robot> canSwap = new ArrayList<Robot>();
		
		if(highestPriorityInNetwork != null){
			//System.out.println("HIGHEST PRIORITY IN NETWORK: " + highestPriorityInNetwork.objective.value + " STATUS: " + highestPriorityInNetwork.status);
			
		}
		else{
			//System.out.println("Only Solved Robots In Network. No Swaps.");
			tuple.add(this);
			tuple.add(null);
			return tuple;
		}
		
		// If highest priority robot is already swapping, return it and the bot it is swapping with
		if(highestPriorityInNetwork.status.contains("SWAP")){
			//System.out.println("Highest Priority Bot already swapping");
			tuple.add(highestPriorityInNetwork);
			
			// EXPERIMENT
			if(!highestPriorityInNetwork.swappingBot.status.contains("SWAP") && highestPriorityInNetwork.swappingBot.twig == null){
				highestPriorityInNetwork.swappingBot.status = "SWAP_SET";
			}
			else if(!highestPriorityInNetwork.swappingBot.status.contains("SWAP")) {
				highestPriorityInNetwork.swappingBot.status = "SWAP_CONTINUE";
			}
			
			tuple.add(highestPriorityInNetwork.swappingBot);
			return tuple;
		}
		else{
			// Checking to see if highestPriority bot should swap with neighbors
			//System.out.println("Num Neighbors: " + highestPriorityInNetwork.currNode.neighbors.size());
			//System.out.println("CURR NODE: " + highestPriorityInNetwork.currNode.value);
			ArrayList<GraphNode> botNeighbors = highestPriorityInNetwork.currNode.neighbors;
			
			// At least one bot that highestPriority could possibly swap with
			if(botNeighbors.size() > 0){
				for(int b = 0; b < botNeighbors.size(); b++){
					//System.out.println("Neighbor: " + botNeighbors.get(b).value);
					if(botNeighbors.get(b).robot != null && !botNeighbors.get(b).robot.solved){
						Robot neighborBot = botNeighbors.get(b).robot;
						//System.out.println("Priority Bot Path");
						highestPriorityInNetwork.printRobotPath();
						//System.out.println("Neighor Bot Path");
						neighborBot.printRobotPath();
						// Detects this!
						if(this.swapCond1(highestPriorityInNetwork, neighborBot)){
							//System.out.println("SWAP COND 1");
							canSwap.add(neighborBot);
						}
						// Detects this!
						else if(this.swapCond2(highestPriorityInNetwork, neighborBot)){
							//System.out.println("SWAP COND 2");
							canSwap.add(neighborBot);
						}
						// Detects this!
						else if(this.swapCond3(highestPriorityInNetwork, neighborBot)){
							//System.out.println("SWAP COND 3");
							canSwap.add(neighborBot);
						}
						// not detecting
						else if(this.swapCond4(highestPriorityInNetwork, neighborBot)){
							//System.out.println("SWAP COND 4");
							canSwap.add(neighborBot);
						}
					}
				}
			}
			// There exist adjacent robot(s) that should swap
			if(canSwap.size() > 0){
				//System.out.println("CAN SWAP LIST GREATER THAN 0");
				this.numSwaps += 1;
				// Sort neighbors by priority
				Collections.sort(canSwap);
				Robot swappingBot = canSwap.get(0);
				//System.out.println("SWAPPING WITH: " + swappingBot.objective.value);
				highestPriorityInNetwork.swappingBot = swappingBot;
				swappingBot.swappingBot = highestPriorityInNetwork;
				// Checking to see if Highest Priority Bot is at child node of a solved bot
				for(int c = 0; c < highestPriorityInNetwork.solvedRobots.size(); c++){
					if(highestPriorityInNetwork.currNode.parent == highestPriorityInNetwork.solvedRobots.get(c).objective){
						//System.out.println("return null, null");
						tuple.add(null);
						tuple.add(null);
						return tuple;
					}
				}
				// Setting Branches Visited to be empty
				highestPriorityInNetwork.branchesVisited = new ArrayList<GraphNode>();
				highestPriorityInNetwork.swappingBot.branchesVisited = new ArrayList<GraphNode>();
				highestPriorityInNetwork.twigsVisited = new ArrayList<GraphNode>();
				highestPriorityInNetwork.swappingBot.twigsVisited = new ArrayList<GraphNode>();
				highestPriorityInNetwork.branch = null;
				highestPriorityInNetwork.swappingBot.branch = null;
				
				tuple.add(highestPriorityInNetwork);
				tuple.add(highestPriorityInNetwork.swappingBot);
				//System.out.println("Returning two swapping bots: " + highestPriorityInNetwork.objective.value + " , " + swappingBot.objective.value);
				// highestPriorityInNetwork.status = "SWAP_SET";
				// highestPriorityInNetwork.swappingBot.status = "SWAP_SET";
				return tuple;
			}
			else{
				//System.out.println("Path clear");
				tuple.add(highestPriorityInNetwork);
				tuple.add(null);
				return tuple;
			}
		}
	}
	// Neighbor is in Highest Priority robot's path and vice-versa
	// WORKS
	// TODO: What if neighbor bot has already been solved? No path anymore?
	public boolean swapCond1(Robot priorityBot, Robot neighbor){
		ArrayList<GraphNode> priorityPath = this.adjList.Dijkstra(priorityBot.currNode, priorityBot.objective);
		ArrayList<GraphNode> neighborPath = this.adjList.Dijkstra(neighbor.currNode, neighbor.objective);
		if(priorityPath.contains(neighbor.currNode) && neighborPath.contains(priorityBot.currNode)){
			return true;
		}
		else if(priorityPath.contains(neighbor.currNode) && neighborPath.size() == 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	// Neighbor Robot's current node and it's objective are on the highest priority robot's path
	// WORKS
	public boolean swapCond2(Robot priorityBot, Robot neighbor){
		ArrayList<GraphNode> priorityPath = this.adjList.Dijkstra(priorityBot.currNode, priorityBot.objective);
		ArrayList<GraphNode> neighborPath = this.adjList.Dijkstra(neighbor.currNode, neighbor.objective);
		if(priorityPath.contains(neighbor.currNode) && priorityPath.contains(neighbor.objective)){
			return true;
		}
		else{
			return false;
		}
	}
	
	// Highest Priority Robot's current node and it's objective are on the neighbor robot's path
	// WORKS
	public boolean swapCond3(Robot priorityBot, Robot neighbor){
		ArrayList<GraphNode> priorityPath = this.adjList.Dijkstra(priorityBot.currNode, priorityBot.objective);
		ArrayList<GraphNode> neighborPath = this.adjList.Dijkstra(neighbor.currNode, neighbor.objective);
		if(neighborPath.contains(priorityBot.currNode) && neighborPath.contains(priorityBot.objective)){
			return true;
		}
		else{
			return false;
		}
	}
	
	// Neighbor is STUCK and in priorityBot's path
	// TODO: IS THIS THE CORRECT INTERPRETATION OF SWAP CONDITION 4?
	public boolean swapCond4(Robot priorityBot, Robot neighbor){
		ArrayList<GraphNode> priorityPath = this.adjList.Dijkstra(priorityBot.currNode, priorityBot.objective);
		ArrayList<GraphNode> neighborPath = this.adjList.Dijkstra(neighbor.currNode, neighbor.objective);
		if(priorityPath.contains(neighbor.currNode) && neighbor.status == "STUCK"){
			return true;
		}
		else{
			return false;
		}
	}
	
	public void swap(){
		if(this.status == "SWAP_SET"){
			Robot priorityBot = null;
			if(this.swappingBot == null){
				priorityBot = this;
			}
			else if(this.priority < this.swappingBot.priority){
				priorityBot = this;
			}
			else{
				priorityBot = this.swappingBot;
			}
			//System.out.println("PRIORITY BOT IN SWAP: " + priorityBot.objective.value);
			this.startSwap(priorityBot);
		}
		else if(this.status == "SWAP_CONTINUE"){
			this.continueSwap();
		}
		else if(this.status == "SWAP_FINISH"){
			if(this.leader){
				this.FinishSwapLeader();
			}
			else{
				this.FinishSwapFollower();
			}
		}
	}
	
	public void startSwap(Robot priorityBot){
		//System.out.println("START SWAP");
		
		Robot leader = null;
		Robot follower = null;
		
		if(this.swappingBot.branch != null){
			this.branch = this.swappingBot.branch;
			this.twigEnd = this.swappingBot.twigEnd;
			
			if(this.swappingBot.leader){
				this.leader = false;
				this.follower = true;
				leader = this.swappingBot;
				follower = this;
			}
			else if(this.swappingBot.follower){
				this.leader = true;
				this.follower = false;
				leader = this;
				follower = this.swappingBot;
			}
		}
		else{
			// Get branches and twigs in tree
			ArrayList<ArrayList<GraphNode>> branchesAndTwigs = this.getBranches(priorityBot);
			ArrayList<GraphNode> branches = branchesAndTwigs.get(0);
			ArrayList<GraphNode> twigs = branchesAndTwigs.get(1);
			
			for(int i = 0; i < branches.size(); i++){
				//System.out.println("BRANCH: " + branches.get(i).value);
			}
			
			for(int i = 0; i < twigs.size(); i++){
				//System.out.println("TWIG: " + twigs.get(i).value);
			}
			
			GraphNode branchNode = null;
			for(int i = 0; i < branches.size(); i++){
				if(!this.branchesVisited.contains(branches.get(i))){
					branchNode = branches.get(i);
					this.branchesVisited.add(branchNode);
					//System.out.println("BRANCH NODE: " + branchNode.value);
					// TODO: IS THIS WHAT IS MEANT BY REMOVING PARENT(S) FROM VISITED LIST?
					ArrayList<GraphNode> branchAncestors = branchNode.ancestors;
					for(int j = 0; j < branchAncestors.size(); j++){
						if(this.branchesVisited.contains(branchAncestors.get(j))){
							branchesVisited.remove(branchAncestors.get(j));
							//System.out.println("REMOVE ANCESTOR: " + branchAncestors.get(j).value + " FROM VISITED");
						}
					}
					break;
				}
				else{
					//System.out.println("Branch " + branches.get(i).value + " already visited");
				}
			}
			
			// TODO: Not sure if 'this' should set branch to branchNode
			this.branch = branchNode;
			
			//System.out.println("PRIORITY BOT - SOURCE: " + priorityBot.currNode.value + ", DESTINATION: " + this.branch.value);
			//System.out.println("SWAPPING BOT - SOURCE: " + priorityBot.swappingBot.currNode.value + ", DESTINATION: " + this.branch.value);
			
			// Now determining paths from highest priority robot to branch and from swapping bot to branch
			ArrayList<GraphNode> priorityBotToBranch = this.adjList.Dijkstra(priorityBot.currNode, this.branch);
			ArrayList<GraphNode> swappingBotToBranch = this.adjList.Dijkstra(priorityBot.swappingBot.currNode, this.branch);
			//System.out.println("PRIORITY BOT TO BRANCH PATH:");
			for(int i = 0; i < priorityBotToBranch.size(); i++){
				//System.out.println(priorityBotToBranch.get(i).value);
			}
			
			//System.out.println("LOWER PRIORITY BOT TO BRANCH PATH:");
			for(int i = 0; i < swappingBotToBranch.size(); i++){
				//System.out.println(swappingBotToBranch.get(i).value);
			}
			
			if(priorityBotToBranch.size() <= swappingBotToBranch.size()){
				leader = priorityBot;
				follower = priorityBot.swappingBot;
			}
			else{
				leader = priorityBot.swappingBot;
				follower = priorityBot;
			}
			
			if(this == leader){
				this.leader = true;
				this.follower = false;
				//System.out.println("Current robot is leader");
			}
			
			if(this == follower){
				this.follower = true;
				this.leader = false;
				//System.out.println("Current robot is follower");
			}
			
			ArrayList<GraphNode> branchToFollowerPath = this.adjList.Dijkstra(this.branch, follower.currNode);
			GraphNode twigEnd = branchToFollowerPath.get(1);
			//System.out.println("Twig End: " + twigEnd.value);
			
			leader.twigEnd = twigEnd;
			follower.twigEnd = twigEnd;
		}
		
		// twig1 and twig2 are the branches attached to branchNode that are not twigEnd
		ArrayList<GraphNode> otherTwigs = new ArrayList<GraphNode>();
		for(int i = 0; i < this.branch.neighbors.size(); i++){
			if(this.branch.neighbors.get(i) != twigEnd){
				otherTwigs.add(this.branch.neighbors.get(i));
			}
		}
		Collections.sort(otherTwigs);
		GraphNode twig1HighPriority = otherTwigs.get(0);
		GraphNode twig2LowPriority = otherTwigs.get(1);
		
		//System.out.println("TWIG 1: " + twig1HighPriority.value);
		//System.out.println("TWIG 2: " + twig2LowPriority.value);
		
		// TODO: Not clear how robot chooses which twig to set its path towards
		// Is r guaranteed to be either rLeader or rFollower?
		if(this.leader){
			if(this.objective.value < this.swappingBot.objective.value){
				//System.out.println("ROBOT " + this.objective.value + " DESTINATION IS " + twig1HighPriority.value);
				this.path = this.adjList.Dijkstra(this.currNode, twig1HighPriority);
				this.path.remove(0);
				this.step();
				this.twig = twig1HighPriority;
			}
			else{
				//System.out.println("ROBOT " + this.objective.value + " DESTINATION IS " + twig2LowPriority.value);
				this.path = this.adjList.Dijkstra(this.currNode, twig2LowPriority);
				this.path.remove(0);
				this.step();
				this.twig = twig2LowPriority;
			}
		}
		else if(this.follower){
			if(this.objective.value < this.swappingBot.objective.value){
				//System.out.println("ROBOT " + this.objective.value + " DESTINATION IS " + twig1HighPriority.value);
				this.path = this.adjList.Dijkstra(this.currNode, twig1HighPriority);
				this.path.remove(0);
				this.step();
				this.twig = twig1HighPriority;
			}
			else{
				//System.out.println("ROBOT " + this.objective.value + " DESTINATION IS " + twig2LowPriority.value);
				this.path = this.adjList.Dijkstra(this.currNode, twig2LowPriority);
				this.path.remove(0);
				this.step();
				this.twig = twig2LowPriority;
			}
		}
		this.twigsVisited.add(this.twig);
		
		//System.out.println("Finished Start Swap");
		this.status = "SWAP_CONTINUE";
	}
	
	public void continueSwap(){
		//System.out.println("CONTINUE SWAP");
		if(this.swappingBot.status == "SWAP_SET"){
			//System.out.println("Still Swap Set");
			this.startSwap(this);
		}
		else if(this.currNode == this.twig){
			//System.out.println("At twig");
			this.path = this.adjList.Dijkstra(this.currNode, this.twig);
			this.status = "SWAP_FINISH";
		}
		else{
			//System.out.println("CONTINUE SWAP ELSE");
			//System.out.println("ROBOT'S TWIG: " + this.twig.value);
			boolean twigStuck = false;
			//System.out.println("CURRNODE VALUE OF OTHER ROBOTS IN COMPLETE NETWORK");
			for(int i = 0; i < this.completeNetwork.size(); i++){
				//System.out.println(this.completeNetwork.get(i).currNode.value);
				if((this.completeNetwork.get(i).currNode == this.twig) && ((this.completeNetwork.get(i).status == "STUCK") || (this.completeNetwork.get(i).status == "SWAP_FINISH"))){
					twigStuck = true;
				}
			}
			if(twigStuck){
				//System.out.println("TWIG STUCK");
				GraphNode newTwig = null;
				for(int j = 0; j < this.branch.neighbors.size(); j++){
					if((this.branch.neighbors.get(j) != this.twigEnd) && (this.branch.neighbors.get(j) != this.twig) && !this.twigsVisited.contains(this.branch.neighbors.get(j))){
						newTwig = this.branch.neighbors.get(j);
						this.twigsVisited.add(this.branch.neighbors.get(j));
						break;
					}
				}
				if(newTwig != null){
					//System.out.println("NEW TWIG: " + newTwig.value);
					this.twig = newTwig;
					this.path = this.adjList.Dijkstra(this.currNode, newTwig);
					this.path.remove(0);
					this.step();
					this.status = "SWAP_CONTINUE";
				}
				else{
					//System.out.println("ALL TWIGS FULL. FIND NEW BRANCH");
					this.status = "SWAP_SET";
					this.branch = null;
				}
				
			}
			else{
				//System.out.println("TAKE STEP SWAP CONTINUE");
				this.step();
				this.status = "SWAP_CONTINUE";
			}
			/*
			boolean takeStep = true;
			for(int i = 0; i < this.completeNetwork.size(); i++){
				if((this.completeNetwork.get(i).currNode == this.twig) && ((this.completeNetwork.get(i).status == "STUCK") || (this.completeNetwork.get(i).status == "SWAP_FINISH"))){
					System.out.println("Finished or stuck");
					takeStep = false;
					GraphNode twigNew = null;
					for(int j = 0; j < this.branch.neighbors.size(); j++){
						if((this.branch.neighbors.get(j) != this.twig) && (this.branch.neighbors.get(j) != this.twigEnd)){
							twigNew = this.branch.neighbors.get(j);
						}
					}
					if(twigNew != null){
						System.out.println("New twig");
						this.path = adjList.Dijkstra(this.currNode, twigNew);
						this.status = "SWAP_CONTINUE";
					}
					else{
						System.out.println("IDLE");
						// robot stays idle
						this.status = "SWAP_SET";
					}
				}
			}
			// ADDING MYSELF...DON'T SEE IN CODE. HOW ELSE DO ROBOTS TAKE STEP TO TWIG?
			if(takeStep){
				System.out.println("TAKE STEP SWAP CONTINUE");
				this.step();
				this.status = "SWAP_CONTINUE";
			}
			*/
		}
	}
	
	public void FinishSwapLeader(){
		//System.out.println("FINISH SWAP LEADER: " + this.objective.value);
		if(this.currNode == this.twig){
			//System.out.println("AT TWIG");
			if(this.swappingBot.status == "SWAP_SET"){
				//System.out.println("OTHER BOT SWAP_SET");
				this.branch = null;
				this.startSwap(this);
			}
			else if(this.swappingBot.currNode == this.swappingBot.twig){
				//System.out.println("OTHER SWAPPING BOT AT TWIG");
				this.path = this.adjList.Dijkstra(this.currNode, this.twigEnd);
				// Need to take step here?
				this.path.remove(0);
				this.step();
				this.status = "SWAP_FINISH";
			}
			else{
				// remain idle
				//System.out.println("REMAIN IDLE 1");
				this.status = "SWAP_FINISH";
			}
		}
		else if(this.currNode == this.twigEnd){
			//System.out.println("AT TWIG END");
			if(this.swappingBot.currNode == this.branch){
				//System.out.println("AT BRANCH");
				this.path = this.adjList.Dijkstra(this.currNode, this.objective);
				// this.path.remove(0);
				this.branchesVisited = new ArrayList<GraphNode>();
				this.status = "NORMAL";
				// Seems like this is necessary
				this.swappingBot.status = "NORMAL";
				this.swappingBot.path = this.adjList.Dijkstra(this.swappingBot.currNode, this.swappingBot.objective);
				this.swappingBot.branchesVisited = new ArrayList<GraphNode>();
			}
			else{
				//System.out.println("REMAIN IDLE 2");
				// remain idle
				this.status = "SWAP_FINISH";
			}
		}
		// r heading to twigEnd
		else{
			//System.out.println("HEADING TO TWIG END");
			this.path = this.adjList.Dijkstra(this.currNode, this.twigEnd);
			this.path.remove(0);
			this.step();
			this.status = "SWAP_FINISH";
		}
	}
	
	public void FinishSwapFollower(){
		//System.out.println("FINISH SWAP FOLLOWER: " + this.objective.value);
		if(this.currNode == this.twig){
			//System.out.println("AT TWIG");
			if(this.swappingBot.currNode == this.twigEnd){
				//System.out.println("SWAPPING BOT AT TWIG END");
				this.path = this.adjList.Dijkstra(this.currNode, this.branch);
				this.path.remove(0);
				this.step();
				this.status = "SWAP_FINISH";
			}
			else{
				//System.out.println("REMAIN IDLE 3");
				// remain idle
				this.status = "SWAP_FINISH";
			}
		}
		else if(this.currNode == this.branch){
			//System.out.println("AT BRANCH. GO TO OBJ");
			this.path = this.adjList.Dijkstra(this.currNode, this.objective);
			// this.path.remove(0);
			this.branchesVisited = new ArrayList<GraphNode>();
			// this.step();
			this.status = "NORMAL";
		}
	}
	
	// TODO Current bug: Robot not being pushed to lowest priority branch
	public void Pushed2(Robot highestPriority){
		//System.out.println("PUSHED2");
		boolean highestPriorityInDirectNetwork = false;
		for(int i = 0; i < this.directNetwork.size(); i++){
			if(this.directNetwork.get(i) == highestPriority){
				highestPriorityInDirectNetwork = true;
			}
		}
		boolean robotOnPathPushedSwapping = false;
		//System.out.println("Current Robot (" + this.objective.value + ") path:");
		for(int k = 0; k < this.path.size(); k++){
			//System.out.println(this.path.get(k).value);
		}
		for(int i = 0; i < this.completeNetwork.size(); i++){
			//System.out.println("Robot " + this.completeNetwork.get(i).objective.value + "'s path:");
			for(int j = 0; j < this.completeNetwork.get(i).path.size(); j++){
				//System.out.println(this.completeNetwork.get(i).path.get(j).value);
			}
			// Added "Normal" status to deal with Swap Condition 4
			if(this.completeNetwork.get(i).path.contains(this.currNode) && (this.completeNetwork.get(i).status == "PUSHED" || this.completeNetwork.get(i).status.contains("SWAP") || this.completeNetwork.get(i) == highestPriority)){
				//System.out.println("Current Robot On Path of Swapping/Pushed/highestPriority bot");
				robotOnPathPushedSwapping = true;
			}
		}
		ArrayList<GraphNode> freeNeighbors = this.getFreeNeighborsForPush(highestPriority);
		if(robotOnPathPushedSwapping){
			//System.out.println("robotOnPathPushedSwapping");
			if(freeNeighbors.size() > 0){
				//System.out.println("Free Neighbors:");
				GraphNode lowestPriorityNeighbor = freeNeighbors.get(0);
				for(int i = 0; i < freeNeighbors.size(); i++){
					//System.out.println(freeNeighbors.get(i).value);
					if(freeNeighbors.get(i).value > lowestPriorityNeighbor.value){
						lowestPriorityNeighbor = freeNeighbors.get(i);
					}
				}
				lowestPriorityNeighbor.triedFreeNeighbor = true;
				//System.out.println("Lowest Priority Neighbor: " + lowestPriorityNeighbor.value);
				this.path = this.adjList.Dijkstra(this.currNode, lowestPriorityNeighbor);
				this.path.remove(0);
				GraphNode tempNode = this.currNode;
				this.step();
				if(this.currNode != tempNode){
					//System.out.println("Resetting free neighbors");
					for(int i = 0; i < freeNeighbors.size(); i++){
						freeNeighbors.get(i).triedFreeNeighbor = false;
					}
				}
				this.status = "PUSHED";
			}
			else{
				//System.out.println("Stuck");
				this.status = "STUCK";
			}
		}
		else if(highestPriorityInDirectNetwork){
			//System.out.println("Highest Priority Bot In Direct Network");
			// Checking to see if highest priority bot is going towards its objective node
			ArrayList<GraphNode> highestPriorityToObjective = this.adjList.Dijkstra(highestPriority.currNode, highestPriority.objective);
			//System.out.println("HIGHEST PRIORITY PATH TO OBJECTIVE");
			for(int i = 0; i < highestPriorityToObjective.size(); i++){
				//System.out.println(highestPriorityToObjective.get(i).value);
			}
			//System.out.println("HIGHEST PRIORITY CURRENT PATH");
			for(int i = 0; i < highestPriority.path.size(); i++){
				//System.out.println(highestPriority.path.get(i).value);
			}
			if(highestPriority.path.size() == 0){
				//System.out.println("Highest Priority Bot Not Going To Objective");
				//System.out.println("Set status to WAITING");
				this.status = "WAITING";
			}
			// highestPriority.path.get(highestPriority.path.size() - 1) == highestPriorityToObjective.get(highestPriorityToObjective.size() - 1)
			// highestPriority.path.get(0) == highestPriorityToObjective.get(0)
			// else if(highestPriority.path.get(highestPriority.path.size() - 1) == highestPriorityToObjective.get(highestPriorityToObjective.size() - 1)){
			else if(highestPriority.movedTowardsDestination){
				//System.out.println("Highest Priority Bot Going To Objective");
				//System.out.println("Set status to PAUSED");
				this.status = "PAUSED";
			}
			else{
				//System.out.println("Highest Priority Bot Not Going To Objective");
				//System.out.println("Set status to WAITING");
				this.status = "WAITING";
			}
		}
		else if(this.status == "WAITING"){
			//System.out.println("Waiting");
			this.status = "WAITING";
		}
		else{
			//System.out.println("Else Paused");
			this.status = "PAUSED";
		}
	}
	
	public void Pushed(Robot highestPriority){
		//System.out.println("PUSHED");
		for(int i = 0; i < this.completeNetwork.size(); i++){
			Robot netBot = this.completeNetwork.get(i);
			if((netBot.path.contains(this.currNode)) && (netBot.status == "PUSHED" || netBot.status.contains("SWAP"))){
				ArrayList<GraphNode> freeNeighbors = netBot.getFreeAdjacentNodes();
				if(freeNeighbors.size() > 0){
					// getting lowest priority neighbor and setting position of robot to it
					Collections.sort(freeNeighbors);
					this.currNode = freeNeighbors.get(0);
					this.status = "PUSHED";
				}
				else{
					// remain idle
					this.status = "STUCK";
				}
			}
			else if(this.directNetwork.contains(highestPriority)){
				// next node on path of highest priority unsolved robot
				if(this.adjList.Dijkstra(highestPriority.currNode, highestPriority.objective).get(1) == highestPriority.path.get(1)){
					// remain idle
					this.status = "PAUSED";
				}
				else{
					// remain idle
					this.status = "WAITING";
				}
			}
			else if(this.status == "WAITING"){
				// remain idle
				this.status = "WAITING";
			}
			else{
				// remain idle
				this.status = "PAUSED";
			}
			
		}
	}
	// TODO
	public ArrayList<GraphNode> getFreeNeighborsForPush(Robot highestPriority){
		//System.out.println("Get Free Neighbors");
		ArrayList<GraphNode> freeNodes = new ArrayList<GraphNode>();
		ArrayList<GraphNode> neighbors = this.currNode.neighbors;
		ArrayList<GraphNode> path = this.adjList.Dijkstra(this.currNode, highestPriority.currNode);
		//System.out.println("Path from Swapped Bot To Pushed Bot:");
		for(int j = 0; j < path.size(); j++){
			//System.out.println(path.get(j).value);
		}
		for(int i = 0; i < neighbors.size(); i++){
			if(!path.contains(neighbors.get(i))){
				if(neighbors.get(i).robot == null){
					freeNodes.add(neighbors.get(i));
				}
				else if(neighbors.get(i).robot.status != "STUCK" && neighbors.get(i).triedFreeNeighbor == false){
					freeNodes.add(neighbors.get(i));
				}
			}
		}
		return freeNodes;
	}
	
	public ArrayList<GraphNode> getFreeAdjacentNodes(){
		ArrayList<GraphNode> freeNodes = new ArrayList<GraphNode>();
		ArrayList<GraphNode> neighbors = this.currNode.neighbors;
		for(int i = 0; i < neighbors.size(); i++){
			if(neighbors.get(i).robot == null){
				freeNodes.add(neighbors.get(i));
			}
		}
		return freeNodes;
	}
	
	public ArrayList<ArrayList<GraphNode>> getBranches(Robot priorityBot){
		boolean[] visited = new boolean[this.adjList.numNodes];
		int numVisited = 0;
		for(int i = 0; i < visited.length; i++){
			visited[i] = false;
		}
		
		ArrayList<GraphNode> branches = new ArrayList<GraphNode>();
		ArrayList<GraphNode> twigs = new ArrayList<GraphNode>();
		
		Queue<GraphNode> queue = new Queue<GraphNode>();
		queue.enqueue(priorityBot.currNode);
		while(queue.length != 0){
			GraphNode currNode = (GraphNode) queue.dequeue().luggage;
			int currVal = currNode.value;
			visited[currVal] = true;
			if(currNode.branch){
				branches.add(currNode);
			}
			if(currNode.twig){
				twigs.add(currNode);
			}
			LLNode listNode = this.adjList.graphArr[currVal].head;
			while(listNode != null){
				if(!visited[listNode.destination]){
					queue.enqueue(listNode.destNode);
				}
				listNode = listNode.right;
			}
		}
		ArrayList<ArrayList<GraphNode>> result = new ArrayList<ArrayList<GraphNode>>();
		result.add(branches);
		result.add(twigs);
		return result;
	}
	
	public void setBoundary(Rectangle b) {
        b.setBounds(p.x - r, p.y - r, 2 * r, 2 * r);
    }
	
	public void draw(Graphics g) {
        g.setColor(color.BLACK);
        g.fillOval(b.x, b.y, b.width, b.height);
        g.setColor(Color.white);
    	g.drawString("" + this.objective.value, b.x + b.width/2, b.y + b.height/2);
    }
	
	public void step(){
		if(this.path.size() > 0){
			// Only take step if the node is free
			if(this.path.get(0).robot == null){
				if(!this.nodesVisited.contains(this.currNode)){
					this.nodesVisited.add(this.currNode);
					//System.out.println("ADD TO VISITED");
				}
				//System.out.println("ROBOT: " + this.objective.value + " CURRENT DESTINATION: " + this.path.get(this.path.size() - 1).value);
				//System.out.println("GOING TO NODE: " + this.path.get(0).value);
				
				// Is robot taking step toward objective?
				if(this.path.get(this.path.size() - 1) == this.objective){
					this.movedTowardsDestination = true;
				}
				else{
					this.movedTowardsDestination = false;
				}
				
				this.currNode.robot = null;
				GraphNode next = this.path.remove(0);
				next.robot = this;
				this.currNode = next;
				this.p = this.currNode.p;
				this.numMoves++;
				if(this.solved){
					this.stepsAfterSolved++;
				}
				if(this.currNode == this.objective){
					this.visitedObjective = true;
				}
				this.higherPriorityBotsSolved = true;
				this.onPathOfLowerPriorityBot = false;
				for(int i = 0; i < this.adjList.nodeL.length; i++){
					if(this.adjList.nodeL[i].robot != null && this.adjList.nodeL[i].robot.priority < this.priority && !this.adjList.nodeL[i].robot.solved){
						this.higherPriorityBotsSolved = false;
					}
					if(this.adjList.nodeL[i].robot != null){
						//System.out.println("Robot: " + this.adjList.nodeL[i].robot.objective.value + " Ancestors length: " + this.adjList.nodeL[i].robot.ancestors.size());
						//System.out.println("Priority: " + this.adjList.nodeL[i].robot.priority);
						//System.out.println("This.Priority: " + this.priority);
						//System.out.println("This.currnode: " + this.currNode.value);
						GraphNode tempNode = this.adjList.nodeL[i];
						this.adjList.nodeL[i].robot.ancestors = new ArrayList<GraphNode>();
						while(tempNode.root == false){
							this.adjList.nodeL[i].robot.ancestors.add(tempNode.parent);
							tempNode = tempNode.parent;
						}
						if(this.adjList.nodeL[i].robot.priority > this.priority && this.adjList.nodeL[i].robot.ancestors.contains(this.currNode)){
							this.onPathOfLowerPriorityBot = true;
							// Reset visited objective
							this.visitedObjective = false;
						}
					}
				}
				//System.out.println("Visited Objective: " + this.visitedObjective);
				//System.out.println("Higher Priority Bots Solved: " + this.higherPriorityBotsSolved);
				//System.out.println("On Path Of Lower Priority Bot: " + this.onPathOfLowerPriorityBot);
				if(this.visitedObjective && this.higherPriorityBotsSolved && !this.onPathOfLowerPriorityBot){
					//System.out.println("SOLVED");
					this.solved = true;
				}
				else{
					//System.out.println("NOT SOLVED");
				}
				setBoundary(b);
			}
			else{
				//System.out.println("ROBOT: " + this.objective.value + " CAN'T STEP. PATH BLOCKED");
				this.movedTowardsDestination = false;
			}
		}
		else{
			//System.out.println("ROBOT: " + this.objective.value + " AT OBJECTIVE. STAY AT NODE");
			this.movedTowardsDestination = false;
		}
	}
	
	public ArrayList<Robot> getCompleteNetwork(){
		completeNetwork = new ArrayList<Robot>();
		HashMap<Robot, Boolean> hmap = new HashMap<Robot, Boolean>();
		Queue<Robot> netQ = new Queue<Robot>();
		for(int i = 0; i < this.directNetwork.size(); i++) {
			this.directNetwork.get(i).directNetwork = this.directNetwork.get(i).adjList.directCommunication(this.directNetwork.get(i).currNode, 2);
			netQ.enqueue(this.directNetwork.get(i));
		}
		
		hmap.put(this, true);
		
		while(netQ.length > 0) {
			Robot currBot = netQ.head.luggage;
			if(!hmap.containsKey(currBot)) {
				hmap.put(currBot, true);
				completeNetwork.add(currBot);
				for(int j = 0 ; j < currBot.directNetwork.size(); j++) {
					currBot.directNetwork.get(j).directNetwork = currBot.directNetwork.get(j).adjList.directCommunication(currBot.directNetwork.get(j).currNode, 2);
					netQ.enqueue(currBot.directNetwork.get(j));
				}
			}
			netQ.dequeue();
		}
		
		return completeNetwork;
	}
	
	public int compareTo(Robot compareBot) {
		if(this.priority > compareBot.priority){
			return 1;
		}
		else if(this.priority == compareBot.priority){
			return 0;
		}
		else{
			return -1;
		}
	}
	
	public void printRobotPath(){
		//System.out.println("ROBOT " + this.objective.value + " PATH:");
		for(int i = 0; i < this.path.size(); i++){
			//System.out.println(this.path.get(i).value);
		}
	}
	
}
