
public class LLNode implements Comparable<LLNode>{
	public LLNode left;
	public LLNode right;
	public GraphNode sourceNode;
	public GraphNode destNode;
	public int distance;
	public int source;
	public int destination;
	
	
	public LLNode(int source, int destination, int distance, GraphNode sourceNode, GraphNode destNode) {
		this.source = source;
		this.destination = destination;
	    this.distance = distance;
	    this.left = null;
	    this.right = null;
	    this.sourceNode = sourceNode;
	    this.destNode = destNode;
	  }
	
	public LLNode(LLNode original){
		this.source = original.source;
		this.destination = original.destination;
		this.distance = original.distance;
		this.left = null;
		this.right = null;
		this.sourceNode = original.sourceNode;
		this.destNode = original.destNode;
	}
	
	// Function used to help sort the LinkedList of edges
	public int compareTo(LLNode compareNode) {
		if(this.distance > compareNode.distance){
			return 1;
		}
		else if(this.distance == compareNode.distance){
			return 0;
		}
		else{
			return -1;
		}
	}
	
}
