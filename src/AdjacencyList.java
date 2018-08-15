import java.util.Arrays;
import java.util.ArrayList;
import java.awt.Color;
import java.util.*;

public class AdjacencyList {
	public LinkedList[] graphArr;
	public int numNodes;
	public GraphNode[] nodeL;
	int orderCounter = 0;
	int childrenAdd;
	
	// AdjacencyList constructor
	// graphArr: Array of LinkedLists that represents the adjacency lists
	// nodeL: Array of all GraphNodes in the graph
	public AdjacencyList(int numNodes){
		this.graphArr = new LinkedList[numNodes];
		this.numNodes = numNodes;
		this.nodeL = new GraphNode[numNodes];
		int childrenAdd = 0;
		
		for(int i = 0; i < this.numNodes; i++){
			this.graphArr[i] = new LinkedList();
			this.nodeL[i] = null;
		}
	}
	
	// Prints the AdjacencyList as a table
	public void print(){
		for(int i = 0; i < this.numNodes; i++){
			String output = i + ": ";
			LLNode currNode = this.graphArr[i].head;
			while(currNode != null){
				output += (currNode.destination + " ");
				currNode = currNode.right;
			}
			System.out.println(output);
		}
	}
	
	// Given a GraphNode and an ArrayList of Sets, this function returns the index of the Set that contains the GraphNode
	private int getSet(GraphNode node, ArrayList<SetList> sets){
		for(int b = 0; b < sets.size(); b++){
			SetList currList = sets.get(b);
			SetNode currNode = currList.head;
			while(currNode != null){
				if(currNode.gNode == node){
					return b;
				}
				currNode = currNode.right;
			}
		}
		return sets.size();
	}
	
	// This function returns an AdjacencyList that represents the Minimum Spanning Tree
	public AdjacencyList mst(){
		// Building Minimum Spanning Tree from current adjacency list representation of graph
		int numEdges = 0;
		
		// Going through each index of adjacency lists and summing number of edges (each edge counted twice)
		for(int i = 0; i < this.numNodes; i++){
			numEdges += this.graphArr[i].length;
		}
		
		// Dividing number of edges to get correct number of edges
		LLNode[] edgeArray = new LLNode[numEdges/2];
		
		// Initializing edge array that will be used in Kruskal's algorithm
		for(int r = 0; r < edgeArray.length; r++){
			edgeArray[r] = null;
		}
		
		// Initializing edge table that keeps tracks of which edges have already been considered
		boolean[][] edgeTable = new boolean[numNodes][numNodes];
		for(int k = 0; k < this.numNodes; k++){
			for(int l = 0; l < this.numNodes; l++){
				edgeTable[k][l] = false;
			}
		}
		
		int edgeCount = 0;
		
		// Going through each linked list in adjacency lists and adding each edge (once) to the edgeArray
		for(int j = 0; j < this.numNodes; j++){
			LLNode currNode = this.graphArr[j].head;
			while(currNode != null){
				if(edgeTable[currNode.destination][j] == false){
					edgeArray[edgeCount] = currNode;
					edgeCount++;
					edgeTable[currNode.destination][j] = true;
					edgeTable[j][currNode.destination] = true;
				}
				currNode = currNode.right;
			}
		}
		
		// using LLNode's compareTo function
		Arrays.sort(edgeArray);
		
		LinkedList spanTree = new LinkedList();
		
		boolean[] setArr = new boolean[this.numNodes];
		
		for(int a = 0; a < setArr.length; a++){
			setArr[a] = false;
		}
		
		ArrayList<SetList> nodeSets = new ArrayList<SetList>();
		
		// Kruskal's Algorithm
		// 'Sets' refer to a group of nodes that are currently accessible to one another in the MST at this point
		for(int g = 0; g < edgeArray.length; g++){
			LLNode currEdge = edgeArray[g];
			GraphNode source = currEdge.sourceNode;
			GraphNode destination = currEdge.destNode;
			int startSetIndex = this.getSet(source, nodeSets);
			int destSetIndex = this.getSet(destination, nodeSets);
			
			// neither node is part of any set
			if(startSetIndex == nodeSets.size() && destSetIndex == nodeSets.size()){
				SetNode startNode = new SetNode(source.value, source);
				SetNode destNode = new SetNode(destination.value, destination);
				SetList newList = new SetList(startNode);
				newList.append(destNode);
				nodeSets.add(newList);
				LLNode deepCopy = new LLNode(currEdge);
				spanTree.append(deepCopy);
			}
			// start is in a set but dest isn't
			else if(destSetIndex == nodeSets.size()){
				SetNode destNode = new SetNode(destination.value, destination);
				nodeSets.get(startSetIndex).append(destNode);
				LLNode deepCopy = new LLNode(currEdge);
				spanTree.append(deepCopy);
			}
			// dest is in a set but start isn't
			else if(startSetIndex == nodeSets.size()){
				SetNode startNode = new SetNode(source.value, source);
				nodeSets.get(destSetIndex).append(startNode);
				LLNode deepCopy = new LLNode(currEdge);
				spanTree.append(deepCopy);
			}
			// start and dest are in different sets
			else if(startSetIndex != destSetIndex){
				nodeSets.get(startSetIndex).appendList(nodeSets.get(destSetIndex));
				nodeSets.remove(destSetIndex);
				LLNode deepCopy = new LLNode(currEdge);
				spanTree.append(deepCopy);
			}
			// Don't consider case where source and dest are already in same set because that would create a cycle
			if(nodeSets.get(0).length == this.numNodes){
				break;
			}
		}
		
		// making new Adjacency list that is the MST
		AdjacencyList mstAdj = new AdjacencyList(this.numNodes);
		LLNode currSpanNode = spanTree.head;
		while(currSpanNode != null){
			int source = currSpanNode.source;
			int destination = currSpanNode.destination;
			int distance = currSpanNode.distance;
			GraphNode sourceNode = currSpanNode.sourceNode;
			GraphNode destNode = currSpanNode.destNode;
			GraphNode sourceNodeCopy = new GraphNode(sourceNode.value, sourceNode.p, sourceNode.r, sourceNode.color);
			GraphNode destNodeCopy = new GraphNode(destNode.value, destNode.p, destNode.r, destNode.color);
			mstAdj.graphArr[source].append(source, destination, distance, sourceNodeCopy, destNodeCopy);
			mstAdj.graphArr[destination].append(destination, source, distance, destNodeCopy, sourceNodeCopy);
			if(mstAdj.nodeL[source] == null){
				mstAdj.nodeL[source] = sourceNodeCopy;
			}
			if(mstAdj.nodeL[destination] == null){
				mstAdj.nodeL[destination] = destNodeCopy;
			}
			currSpanNode = currSpanNode.right;
		}
		
		return mstAdj;
	}
	
	// Function that prints the nodes in a Set
	public void printSet(ArrayList<SetList> setL){
		System.out.println("SETS");
		for(int a = 0; a < setL.size(); a++){
			System.out.println("SET " + a + ":");
			SetNode currNode = setL.get(a).head;
			while(currNode != null){
				System.out.println(currNode.value);
				currNode = currNode.right;
			}
		}
	}
	
	// This function takes a MST and turns it into a tree with a root of node rootNum
	// GraphNodes are labeled as roots, branches, and/or twigs
	public GraphNode createTree(int rootNum){
		Queue<GraphNode> queue = new Queue<GraphNode>();
		boolean[] visitedNodes = new boolean[this.numNodes];
		
		for(int i = 0; i < visitedNodes.length; i++){
			visitedNodes[i] = false;
		}
		
		GraphNode root = this.nodeL[rootNum];
		root.root = true;
		queue.enqueue(root);
		visitedNodes[rootNum] = true;
		
		while(queue.length != 0){
			GraphNode currNode = (GraphNode) queue.dequeue().luggage;
			int currVal = currNode.value;
			LLNode listNode = this.graphArr[currVal].head;
			int counter = 0;
			while(listNode != null){
				if(!visitedNodes[listNode.destination]){
					counter++;
					GraphNode destNode = listNode.destNode;
					//System.out.println("counter: " + this.childrenAdd);
					this.childrenAdd++;
					currNode.children.add(destNode);
					destNode.parent = currNode;
					destNode.parentDist = listNode.distance;
					queue.enqueue(destNode);
					visitedNodes[listNode.destination] = true;
					
					if(currNode.root){
						destNode.ancestors.add(currNode);
					}
					else{
						destNode.ancestors.add(currNode);
						destNode.ancestors.add(currNode.parent);
					}
				}
				listNode = listNode.right;
			}
			if(((counter > 1) && (!currNode.root)) || ((counter > 2) && currNode.root)){
				currNode.branch = true;
			}
			else if((counter == 0) && (!currNode.root)){
				currNode.leaf = true;
			}
		}
		
		preorder(root);
		orderCounter = 0;
		root = postorder(root);
		
		// printTree(root);
		
		return root;
		
	}
	
	public void makeAL(GraphNode node, Boolean[] visitedNodes){
		// System.out.println("NODE: " + node.value);
		this.nodeL[node.value] = node;
		for(int i = 0; i < node.children.size(); i++){
			// System.out.println("CHILD: " + node.children.get(i).value);
			this.graphArr[node.value].append(node.value, node.children.get(i).value, node.children.get(i).parentDist, node, node.children.get(i));
			this.graphArr[node.children.get(i).value].append(node.children.get(i).value, node.value, node.children.get(i).parentDist, node.children.get(i), node);
			if(!visitedNodes[node.children.get(i).value]){
				visitedNodes[node.children.get(i).value] = true;
				makeAL(node.children.get(i), visitedNodes);
			}
		}
	}
	
	public void printTree(GraphNode node){
		String output = node.value + " ";
		if(node.branch){
			output += "branch ";
		}
		if(node.leaf){
			output += "leaf ";
		}
		if(node.twig){
			output += "twig ";
		}
		output += " |  ";
		System.out.print(output);
		for(int i = 0; i < node.children.size(); i++){
			printTree(node.children.get(i));
		}
		System.out.println();
	}
	
	public void preorder(GraphNode node){
		if(node != null){
			if(!node.root){
				if(node.parent.branch){
					node.twig = true;
				}
			}
			if(!(node.leaf)){
				for(int i = 0; i < node.children.size(); i++){
					if(node.children.get(i).branch){
						node.twig = true;
					}
					this.preorder(node.children.get(i));
				}
			}
		}
	}
	
	// Establishing GraphNode priorities
	public GraphNode postorder(GraphNode node){
		if(node.leaf){
			node.value = orderCounter;
			orderCounter++;
			return node;
		}
		else{
			for(int i = 0; i < node.children.size(); i++){
				this.postorder(node.children.get(i));
			}
			node.value = orderCounter;
			orderCounter++;
			return node;
		}
	}
	
	
	public ArrayList<Robot> directCommunication(GraphNode currNode, int radius){
		int nodeNum = currNode.value;
		ArrayList<Robot> commNet = new ArrayList<Robot>();
		boolean[] visited = new boolean[this.numNodes];
		for(int i = 0; i < this.numNodes; i++){
			visited[i] = false;
		}
		visited[nodeNum] = true;
		// commNet.add(currNode.robot);
		commNet = directCommRecurse(nodeNum, radius, visited, commNet);
		// commNet.remove(currNode.robot);
		return commNet;
	}
	
	public ArrayList<Robot> directCommRecurse(int nodeNum, int radius, boolean[] visited, ArrayList<Robot> net){
		// System.out.println("nodeNum: " + nodeNum);
		// System.out.println("radius: " + radius);
		for(int a = 0; a < visited.length; a++){
			// System.out.println("visited " + a + ": " + visited[a]);
		}
		for(int b = 0; b < net.size(); b++){
			// System.out.println("net: " + net.get(b).value);
		}
		if(radius == 0){
			return net;
		}
		else{
			LLNode layer1Node = this.graphArr[nodeNum].head;
			while(layer1Node != null){
				if(!visited[layer1Node.destination] && layer1Node.destNode.robot != null){
					net.add(layer1Node.destNode.robot);
					visited[layer1Node.destination] = true;
				}
				net = directCommRecurse(layer1Node.destination, radius-1, visited, net);
				layer1Node = layer1Node.right;
			}
			return net;
		}
	}
	
	public ArrayList<GraphNode> getNodes(){
		ArrayList<GraphNode> nodeList = new ArrayList<GraphNode>();
		for(int i = 0; i < this.nodeL.length; i++){
			nodeList.add(this.nodeL[i]);
		}
		return nodeList;
	}
	
	public ArrayList<GraphNode> Dijkstra(GraphNode source, GraphNode destination){
		if (source == destination){
			// System.out.println("SOURCE = DESTINATION");
			ArrayList<GraphNode> atObjective = new ArrayList<GraphNode>();
			atObjective.add(source);
			return atObjective;
		}
		MinHeap heap = new MinHeap(this.numNodes);
		boolean[] visited = new boolean[this.numNodes];
		GraphNode[] route = new GraphNode[this.numNodes];
		for(int j = 0; j < visited.length; j++){
			visited[j] = false;
			route[j] = null;
		}
		
		source.DijkDistance = 0;
		heap.insert(source);
		
		GraphNode currNode = null;
		while(currNode != destination){
			// heap.printHeap();
			currNode = heap.deleteMin();
			visited[currNode.value] = true;
			LinkedList reachable = this.graphArr[currNode.value];
			LLNode listNode = reachable.head;
			while(listNode != null){
				int nodeIndex = heap.getIndex(listNode.destNode);
				if(nodeIndex == -1){
					if(!visited[listNode.destination]){
						// System.out.println("Node Not In Heap");
						listNode.destNode.DijkDistance = listNode.distance;
						heap.insert(listNode.destNode);
						route[listNode.destination] = currNode;
					}
				}
				else{
					if(listNode.distance < heap.heap[nodeIndex].DijkDistance){
						// System.out.println("Updating Distance");
						heap.delete(nodeIndex);
						listNode.destNode.DijkDistance = listNode.distance;
						heap.insert(listNode.destNode);
						route[listNode.destination] = currNode;
					}
					// System.out.println("Not Updating Distance");
				}
				listNode = listNode.right;
			}
		}
		
		return createPath(route, source, destination);
	}
	
	public ArrayList<GraphNode> createPath(GraphNode[] map, GraphNode source, GraphNode destination){
		ArrayList<GraphNode> path = new ArrayList<GraphNode>();
		path.add(destination);
		int currNode = destination.value;
		while(currNode != source.value){
			path.add(map[currNode]);
			currNode = map[currNode].value;
		}
		Collections.reverse(path);
		return path;
	}

}
