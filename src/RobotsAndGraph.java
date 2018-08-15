import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.epochx.gp.representation.GPCandidateProgram;
import java.util.Random;

public class RobotsAndGraph {
	public int currentTimeStep;
	public AdjacencyList adjList;
	public ArrayList<Robot> robotList;
	public ArrayList<GraphNode> nodeL;
	public ArrayList<Edge> edgeL;
	public GraphPanel gp;
	public GPCandidateProgram bestProgram;
	public int currentRobot;
	public int nodeIndex;
	public int numLeaves;
	public int numBranches;
	public int numSwaps;
	public String gpSolvedTime;
	public String pswSolvedTime;
	
	public RobotsAndGraph(){
		this.currentTimeStep = 0;
		this.bestProgram = null;
		this.currentRobot = 0;
		this.numLeaves = 0;
		this.nodeIndex = 0;
		this.numSwaps = 0;
		this.gpSolvedTime = "N/A";
		this.pswSolvedTime = "N/A";
		
		this.nodeL = new ArrayList<GraphNode>();
		
		GraphNode node0 = new GraphNode(0, new Point(1200, 50), 60, Color.BLUE);
		GraphNode node1 = new GraphNode(1, new Point(600, 200), 60, Color.BLUE);
		GraphNode node2 = new GraphNode(2, new Point(1200, 200), 60, Color.BLUE);
		GraphNode node3 = new GraphNode(3, new Point(1800, 200), 60, Color.BLUE);
		/*
		GraphNode node4 = new GraphNode(4, new Point(600, 350), 60, Color.BLUE);
		GraphNode node5 = new GraphNode(5, new Point(1200, 350), 60, Color.BLUE);
		GraphNode node6 = new GraphNode(6, new Point(1650, 350), 60, Color.BLUE);
		GraphNode node7 = new GraphNode(7, new Point(1950, 350), 60, Color.BLUE);
		GraphNode node8 = new GraphNode(8, new Point(450, 500), 60, Color.BLUE);
		GraphNode node9 = new GraphNode(9, new Point(750, 500), 60, Color.BLUE);
		GraphNode node10 = new GraphNode(10, new Point(1200, 500), 60, Color.BLUE);
		GraphNode node11 = new GraphNode(11, new Point(1650, 750), 60, Color.BLUE);
		GraphNode node12 = new GraphNode(12, new Point(1950, 750), 60, Color.BLUE);
		*/
		
		this.nodeL.add(node0);
		this.nodeL.add(node1);
		this.nodeL.add(node2);
		this.nodeL.add(node3);
		/*
		this.nodeL.add(node4);
		this.nodeL.add(node5);
		this.nodeL.add(node6);
		this.nodeL.add(node7);
		this.nodeL.add(node8);
		this.nodeL.add(node9);
		this.nodeL.add(node10);
		this.nodeL.add(node11);
		this.nodeL.add(node12);
		/*
		this.nodeL.add(node13);
		this.nodeL.add(node14);
		this.nodeL.add(node15);
		this.nodeL.add(node16);
		this.nodeL.add(node17);
		*/
		
        this.edgeL = new ArrayList<Edge>();
        
        Edge edge0 = new Edge(node0, node1, 1);
        Edge edge1 = new Edge(node0, node2, 1);
        Edge edge2 = new Edge(node0, node3, 1);
        /*
        Edge edge3 = new Edge(node1, node4, 1);
        Edge edge4 = new Edge(node2, node5, 1);
        Edge edge5 = new Edge(node3, node6, 1);
        Edge edge6 = new Edge(node3, node7, 1);
        Edge edge7 = new Edge(node4, node8, 1);
        Edge edge8 = new Edge(node4, node9, 1);
        Edge edge9 = new Edge(node5, node10, 1);
        Edge edge10 = new Edge(node6, node11, 1);
        Edge edge11 = new Edge(node7, node12, 1);
        /*
        Edge edge12 = new Edge(node8, node13, 1);
        Edge edge13 = new Edge(node8, node14, 1);
        Edge edge14 = new Edge(node9, node15, 1);
        Edge edge15 = new Edge(node10, node16, 1);
        Edge edge16 = new Edge(node11, node17, 1);
        */
        
        this.edgeL.add(edge0);
        this.edgeL.add(edge1);
        this.edgeL.add(edge2);
        /*
        this.edgeL.add(edge3);
        this.edgeL.add(edge4);
        this.edgeL.add(edge5);
        this.edgeL.add(edge6);
        this.edgeL.add(edge7);
        this.edgeL.add(edge8);
        this.edgeL.add(edge9);
        this.edgeL.add(edge10);
        this.edgeL.add(edge11);
        /*
        this.edgeL.add(edge12);
        this.edgeL.add(edge13);
        this.edgeL.add(edge14);
        this.edgeL.add(edge15);
        this.edgeL.add(edge16);
        */
        
        this.adjList = mst(0);
        
        this.robotList = new ArrayList<Robot>();
        
        Robot robot0 = newRobot(3,1);
        Robot robot1 = newRobot(1,3);
        /*
        Robot robot2 = newRobot(6,7);
        Robot robot3 = newRobot(7,9);
        /*
        Robot robot4 = newRobot(3,7);
        Robot robot5 = newRobot(1,6);
        Robot robot6 = newRobot(0,13);
        */
        
        this.robotList.add(robot0);
        this.robotList.add(robot1);
        /*
        this.robotList.add(robot2);
        this.robotList.add(robot3);
        /*
        this.robotList.add(robot4);
        this.robotList.add(robot5);
        this.robotList.add(robot6);
        */
	}
	
	public Robot newRobot(int currNodeNum, int objectiveNodeNum){
		GraphNode objNode = this.adjList.nodeL[objectiveNodeNum];
    	GraphNode currNode = this.adjList.nodeL[currNodeNum];
    	
    	Robot newBot = new Robot(currNode, objNode, this.nodeL.size());
    	newBot.path = this.adjList.Dijkstra(currNode, objNode);
    	newBot.path.remove(0);
        currNode.robot = newBot;
        newBot.priority = objNode.value;
        currNode.robot = newBot;
        newBot.adjList = this.adjList;
        
        return newBot;
	}
	
	public AdjacencyList mst(int rootNum){
		int arraySize = 0;
		HashMap<String, Edge> hm = new HashMap<String, Edge>();
		
		for(int i = 0; i < this.edgeL.size(); i++){
			GraphNode n1 = this.edgeL.get(i).n1;
			GraphNode n2 = this.edgeL.get(i).n2;
			hm.put(n1.value + "-" + n2.value, this.edgeL.get(i));
			if(n1.value > arraySize){
				arraySize = n1.value;
			}
			if(n2.value > arraySize){
				arraySize = n2.value;
			}
		}
		arraySize++;
		
		GraphNode[] nodeArr = new GraphNode[arraySize];
		
		for(int i = 0; i < this.nodeL.size(); i++){
			nodeArr[this.nodeL.get(i).value] = this.nodeL.get(i);
		}
		
		this.adjList = new AdjacencyList(arraySize);
		
		for(int j = 0; j < this.edgeL.size(); j++){
			int source = this.edgeL.get(j).n1.value;
			int destination = this.edgeL.get(j).n2.value;
			this.adjList.graphArr[source].append(source, destination, this.edgeL.get(j).weight, this.edgeL.get(j).n1, this.edgeL.get(j).n2);
			this.adjList.graphArr[destination].append(destination, source, this.edgeL.get(j).weight, this.edgeL.get(j).n2, this.edgeL.get(j).n1);
			if(this.adjList.nodeL[source] == null){
				this.adjList.nodeL[source] = this.edgeL.get(j).n1;
			}
			if(this.adjList.nodeL[destination] == null){
				this.adjList.nodeL[destination] = this.edgeL.get(j).n2;
			}
		}
		
		
		this.adjList = this.adjList.mst();
		
		
		// Create MST. Then iterate through updated Adjacency List and remove edges from hashmap. Then convert hashmap into edge list
		this.edgeL.clear();
		
		for(int i = 0; i < this.adjList.numNodes; i++){
			LLNode currNode = this.adjList.graphArr[i].head;
			while(currNode != null){
				if(hm.containsKey(i + "-" + currNode.destination)){
					this.edgeL.add(hm.get(i + "-" + currNode.destination));
				}
				currNode = currNode.right;
			}
		}
		
		
		GraphNode root = this.adjList.createTree(rootNum);
		this.nodeL = this.adjList.getNodes();
		this.adjList = new AdjacencyList(this.adjList.numNodes);
		
		Boolean[] visited = new Boolean[this.adjList.numNodes];
		for(int j = 0; j < visited.length; j++){
			visited[j] = false;
		}
		
		this.adjList.makeAL(root, visited);
		
		this.edgeL = this.makeEdgeList(this.adjList);
		
		for(int a = 0; a < this.edgeL.size(); a++){
			GraphNode n1 = this.edgeL.get(a).n1;
			GraphNode n2 = this.edgeL.get(a).n2;
			n1.addNeighbor(n2);
			n2.addNeighbor(n1);
		}
		
		for(int i = 0; i < this.nodeL.size(); i++){
			nodeArr[this.nodeL.get(i).value] = this.nodeL.get(i); 
		}
		
		this.traverse(root, nodeArr);
		return this.adjList;
	}
	
	public ArrayList<Edge> makeEdgeList(AdjacencyList input){
		ArrayList<Edge> edgeL = new ArrayList<Edge>();
		boolean[][] lookup = new boolean[input.numNodes][input.numNodes];
		for(int i = 0; i < input.numNodes; i++){
			for(int j = 0; j < input.numNodes; j++){
				lookup[i][j] = false;
			}
		}
		
		for(int a = 0; a < input.numNodes; a++){
			LLNode currNode = input.graphArr[a].head;
			while(currNode != null){
				if(!lookup[a][currNode.destination]){
					lookup[a][currNode.destination] = true;
					lookup[currNode.destination][a] = true;
					Edge newEdge = new Edge(currNode.sourceNode, currNode.destNode);
					newEdge.weight = currNode.distance;
					edgeL.add(newEdge);
				}
				currNode = currNode.right;
			}
		}
		return edgeL;
	}
	
	public void traverse(GraphNode node, GraphNode[] nodeArr){
		if(node != null){
			// Branch & Twig : RED
			if(node.branch && node.twig){
				nodeArr[node.value].color = Color.RED;
			}
			// Leaf & Twig : BLUE
			else if(node.leaf && node.twig){
				nodeArr[node.value].color = Color.BLUE;
				this.numLeaves++;
			}
			// Branch
			else if(node.branch){
				nodeArr[node.value].color = Color.GREEN;
			}
			// Leaf
			else if(node.leaf){
				nodeArr[node.value].color = Color.YELLOW;
				this.numLeaves++;
			}
			// twig
			else if(node.twig){
				nodeArr[node.value].color = Color.MAGENTA;
			}
			// Normal GraphNode
			else{
				nodeArr[node.value].color = Color.ORANGE;
			}
			
			
			if(!(node.leaf)){
				for(int i = 0; i < node.children.size(); i++){
					this.traverse(node.children.get(i), nodeArr);
				}
			}
		}
	}
	
	public void runBestProgramOnGUI(GPCandidateProgram bestProgram, int maxTimeSteps){
		JFrame f = new JFrame("GraphPanel");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.gp = new GraphPanel();
        f.add(this.gp.control, BorderLayout.NORTH);
        f.add(new JScrollPane(this.gp), BorderLayout.CENTER);
        f.pack();
        f.setLocationByPlatform(true);
        f.setVisible(true);
		
        startingFormation();
        //startingFormation1();
		
		this.bestProgram = bestProgram;
        
        for(int k = 0; k < this.nodeL.size(); k++){
        	if(this.nodeL.get(k).robot != null){
        		//System.out.println("NODE: " + this.nodeL.get(k).value + " - ROBOT: " + this.nodeL.get(k).robot.objective.value);
        		//System.out.println("Num Moves: " + this.nodeL.get(k).robot.numMoves);
        		//System.out.println("Solved: " + this.nodeL.get(k).robot.solved);
        	}
        	else{
        		//System.out.println("NODE: " + this.nodeL.get(k).value + " - ROBOT: NONE");
        	}
        }
        
        this.gp.robotsAndGraph = this;
        this.gp.nodes = this.nodeL;
        this.gp.edges = this.edgeL;
        this.gp.robotL = this.robotList;
        this.gp.maxTimeSteps = maxTimeSteps;
        this.gp.robotsAndGraph.bestProgram = bestProgram;
        
        this.gp.repaint();
	}
	
	public GraphNode randomGraph(int numNodes){
		//System.out.println("NUM NODES: " + numNodes);
		if(numNodes <= 0){
			return null;
		}
		else{
			Random rand = new Random();
			int x = rand.nextInt(2700) + 40;
			int y = rand.nextInt(1600) + 40;
			GraphNode currentNode = new GraphNode(this.nodeIndex, new Point(x, y), 80, Color.BLUE);
			this.nodeIndex++;
			this.nodeL.add(currentNode);
			int numNeighbors = rand.nextInt(3) + 1;
			if(numNeighbors > numNodes){
				numNeighbors = rand.nextInt(numNodes) + 1;
			}
			//System.out.println("NUM NEIGHBORS: " + numNeighbors);
			for(int i = 0; i < numNeighbors; i++){
				if(numNodes - i == 0){
					break;
				}
				GraphNode neighbor = randomGraph(numNodes - numNeighbors);
				if(neighbor != null){
					this.edgeL.add(new Edge(currentNode, neighbor, 1));
				}
			}
			return currentNode;
		}
	}
	
	public RobotsAndGraph displayRandomGraph(int numNodes){
		this.nodeL = new ArrayList<GraphNode>();
		this.edgeL = new ArrayList<Edge>();
		this.robotList = new ArrayList<Robot>();
		
		this.nodeIndex = 0;
		this.numLeaves = 0;
		randomGraph(numNodes);
		
		System.out.println("NUMBER OF NODES: " + this.nodeL.size());
		
		JFrame f = new JFrame("GraphPanel");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.gp = new GraphPanel();
        f.add(this.gp.control, BorderLayout.NORTH);
        f.add(new JScrollPane(this.gp), BorderLayout.CENTER);
        f.pack();
        f.setLocationByPlatform(true);
        f.setVisible(true);
        
        this.gp.nodes = this.nodeL;
        this.gp.edges = this.edgeL;
        
        //System.out.println("MST");
        this.adjList = this.mst(0);
        
        boolean[] availableSource = new boolean[this.nodeL.size()];
        boolean[] availableDestination = new boolean[this.nodeL.size()];
        
        for(int j = 0; j < this.nodeL.size(); j++){
        	availableSource[j] = true;
        	availableDestination[j] = true;
        }
        
        Random rand = new Random();
        
        //System.out.println("PLACE BOTS");
        for(int i = 0; i < this.numLeaves - 1; i++){
        	//System.out.println("i: " + i);
        	boolean validSource = false;
        	boolean validDestination = false;
        	int source = 0;
        	int destination = 0;
        	while(!validSource){
        		source = rand.nextInt(this.nodeL.size());
        		if(availableSource[source]){
        			availableSource[source] = false;
        			validSource = true;
        		}
        	}
        	//System.out.println("source: " + source);
        	while(!validDestination){
        		destination = rand.nextInt(this.nodeL.size());
        		if(availableDestination[destination]){
        			availableDestination[destination] = false;
        			validDestination = true;
        		}
        	}
        	//System.out.println("destination: " + destination);
        	//System.out.println();
        	this.robotList.add(newRobot(source, destination));
        }
        
        System.out.println("NUMBER OF ROBOTS: " + this.robotList.size());
        
        this.gp.nodes = this.nodeL;
        this.gp.edges = this.edgeL;
        this.gp.robotL = this.robotList;
        this.gp.repaint();
		
        return this;
	}
	
	public RobotsAndGraph createRandomGraph(int numNodes){
		this.nodeL = new ArrayList<GraphNode>();
		this.edgeL = new ArrayList<Edge>();
		this.robotList = new ArrayList<Robot>();
		
		this.nodeIndex = 0;
		this.numLeaves = 0;
		
		randomGraph(numNodes);
		
		System.out.println("NUMBER OF NODES: " + this.nodeL.size());
        
        this.adjList = this.mst(0);
        
        boolean[] availableSource = new boolean[this.nodeL.size()];
        boolean[] availableDestination = new boolean[this.nodeL.size()];
        
        for(int j = 0; j < this.nodeL.size(); j++){
        	availableSource[j] = true;
        	availableDestination[j] = true;
        }
        
        Random rand = new Random();
        int numBots = 0;
        
        if(this.numLeaves > 2) {
	        while (numBots < 2) {
	        	numBots = rand.nextInt(this.numLeaves);
	        }
        }
        else {
        	numBots = this.numLeaves - 1;
        }
        
        for(int i = 0; i < numBots; i++){
        	boolean validSource = false;
        	boolean validDestination = false;
        	int source = 0;
        	int destination = 0;
        	while(!validSource){
        		source = rand.nextInt(this.nodeL.size());
        		if(availableSource[source]){
        			availableSource[source] = false;
        			validSource = true;
        		}
        	}
        	
        	while(!validDestination){
        		destination = rand.nextInt(this.nodeL.size());
        		if(availableDestination[destination] && destination != source){
        			availableDestination[destination] = false;
        			validDestination = true;
        		}
        	}
        	
        	this.robotList.add(newRobot(source, destination));
        }
        
        //System.out.println("ROBOT STARTING POSITIONS");
        for(int i = 0; i < this.robotList.size(); i++){
        	//System.out.println("ROBOT " + this.robotList.get(i).objective.value + " starts on node " + this.robotList.get(i).currNode.value);
        }
        
        System.out.println("NUMBER OF ROBOTS: " + this.robotList.size());
        
        return this;
	}
	
	public void startingFormation1(){
		this.currentTimeStep = 0;
		this.currentRobot = 0;
		this.numLeaves = 0;
		this.nodeIndex = 0;
		
		this.nodeL = new ArrayList<GraphNode>();

		GraphNode node0 = new GraphNode(0, new Point(1200, 50), 60, Color.BLUE);
		GraphNode node1 = new GraphNode(1, new Point(600, 200), 60, Color.BLUE);
		GraphNode node2 = new GraphNode(2, new Point(1200, 200), 60, Color.BLUE);
		GraphNode node3 = new GraphNode(3, new Point(1800, 200), 60, Color.BLUE);
		/*
		GraphNode node4 = new GraphNode(4, new Point(600, 350), 60, Color.BLUE);
		GraphNode node5 = new GraphNode(5, new Point(1200, 350), 60, Color.BLUE);
		GraphNode node6 = new GraphNode(6, new Point(1650, 350), 60, Color.BLUE);
		GraphNode node7 = new GraphNode(7, new Point(1950, 350), 60, Color.BLUE);
		GraphNode node8 = new GraphNode(8, new Point(450, 500), 60, Color.BLUE);
		GraphNode node9 = new GraphNode(9, new Point(750, 500), 60, Color.BLUE);
		GraphNode node10 = new GraphNode(10, new Point(1200, 500), 60, Color.BLUE);
		GraphNode node11 = new GraphNode(11, new Point(1650, 750), 60, Color.BLUE);
		GraphNode node12 = new GraphNode(12, new Point(1950, 750), 60, Color.BLUE);
		*/
		
		this.nodeL.add(node0);
		this.nodeL.add(node1);
		this.nodeL.add(node2);
		this.nodeL.add(node3);
		/*
		this.nodeL.add(node4);
		this.nodeL.add(node5);
		this.nodeL.add(node6);
		this.nodeL.add(node7);
		this.nodeL.add(node8);
		this.nodeL.add(node9);
		this.nodeL.add(node10);
		this.nodeL.add(node11);
		this.nodeL.add(node12);
		/*
		this.nodeL.add(node13);
		this.nodeL.add(node14);
		this.nodeL.add(node15);
		this.nodeL.add(node16);
		this.nodeL.add(node17);
		*/
		
        this.edgeL = new ArrayList<Edge>();
        
        Edge edge0 = new Edge(node0, node1, 1);
        Edge edge1 = new Edge(node0, node2, 1);
        Edge edge2 = new Edge(node0, node3, 1);
        /*
        Edge edge3 = new Edge(node1, node4, 1);
        Edge edge4 = new Edge(node2, node5, 1);
        Edge edge5 = new Edge(node3, node6, 1);
        Edge edge6 = new Edge(node3, node7, 1);
        Edge edge7 = new Edge(node4, node8, 1);
        Edge edge8 = new Edge(node4, node9, 1);
        Edge edge9 = new Edge(node5, node10, 1);
        Edge edge10 = new Edge(node6, node11, 1);
        Edge edge11 = new Edge(node7, node12, 1);
        /*
        Edge edge12 = new Edge(node8, node13, 1);
        Edge edge13 = new Edge(node8, node14, 1);
        Edge edge14 = new Edge(node9, node15, 1);
        Edge edge15 = new Edge(node10, node16, 1);
        Edge edge16 = new Edge(node11, node17, 1);
        */
        
        this.edgeL.add(edge0);
        this.edgeL.add(edge1);
        this.edgeL.add(edge2);
        /*
        this.edgeL.add(edge3);
        this.edgeL.add(edge4);
        this.edgeL.add(edge5);
        this.edgeL.add(edge6);
        this.edgeL.add(edge7);
        this.edgeL.add(edge8);
        this.edgeL.add(edge9);
        this.edgeL.add(edge10);
        this.edgeL.add(edge11);
        /*
        this.edgeL.add(edge12);
        this.edgeL.add(edge13);
        this.edgeL.add(edge14);
        this.edgeL.add(edge15);
        this.edgeL.add(edge16);
        */
        
        this.adjList = mst(0);
        
        this.robotList = new ArrayList<Robot>();
        
        Robot robot0 = newRobot(3,1);
        Robot robot1 = newRobot(1,3);
        /*
        Robot robot2 = newRobot(6,7);
        Robot robot3 = newRobot(7,9);
        /*
        Robot robot4 = newRobot(3,7);
        Robot robot5 = newRobot(1,6);
        Robot robot6 = newRobot(0,13);
        */
        
        this.robotList.add(robot0);
        this.robotList.add(robot1);
        /*
        this.robotList.add(robot2);
        this.robotList.add(robot3);
        /*
        this.robotList.add(robot4);
        this.robotList.add(robot5);
        this.robotList.add(robot6);
        */
	}
	
	public void startingFormation(){
		this.currentTimeStep = 0;
		this.currentRobot = 0;
		this.nodeIndex = 0;
		this.numSwaps = 0;
		
		// setting all node's robot value to null
		for(int i = 0; i < this.nodeL.size(); i++){
			this.nodeL.get(i).robot = null;
			this.nodeL.get(i).triedFreeNeighbor = false;
		}
		
		// Placing robots on their start node + setting start node's robot to be the robot
		for(int j = 0; j < this.robotList.size(); j++){
			this.robotList.get(j).solved = false;
			this.robotList.get(j).gpSolved = false;
			this.robotList.get(j).currNode = this.robotList.get(j).source;
			this.robotList.get(j).currNode.robot = this.robotList.get(j);
			this.robotList.get(j).b.x = this.robotList.get(j).currNode.b.x + this.robotList.get(j).currNode.r/2;
			this.robotList.get(j).b.y = this.robotList.get(j).currNode.b.y + this.robotList.get(j).currNode.r/2;
			this.robotList.get(j).nodesVisited = new ArrayList<GraphNode>();
			this.robotList.get(j).numMoves = 0;
			this.robotList.get(j).stepsAfterSolved = 0;
			this.robotList.get(j).goingToBranch = false;
			this.robotList.get(j).goingToTwig = false;
			this.robotList.get(j).gpBranchesVisited = new ArrayList<GraphNode>();
			this.robotList.get(j).leader = false;
			this.robotList.get(j).follower = false;
			this.robotList.get(j).swapping = false;
			this.robotList.get(j).branchesVisited = new ArrayList<GraphNode>();
			this.robotList.get(j).twigsVisited = new ArrayList<GraphNode>();
			this.robotList.get(j).visitedObjective = false;
			this.robotList.get(j).higherPriorityBotsSolved = false;
			this.robotList.get(j).onPathOfLowerPriorityBot = false;
			this.robotList.get(j).movedTowardsDestination = false;
			this.robotList.get(j).status = "NORMAL";
			this.robotList.get(j).ancestors = new ArrayList<GraphNode>();
			this.robotList.get(j).swappingBot = null;
			this.robotList.get(j).branch = null;
			this.robotList.get(j).directNetwork = new ArrayList<Robot>();
			this.robotList.get(j).completeNetwork = new ArrayList<Robot>();
			this.robotList.get(j).solvedRobots = new ArrayList<Robot>();
			this.robotList.get(j).twig = null;
			this.robotList.get(j).twigEnd = null;
			this.robotList.get(j).numSwaps = 0;
			this.robotList.get(j).path = this.robotList.get(j).adjList.Dijkstra(this.robotList.get(j).source, this.robotList.get(j).objective);
		}
	}
	
}
