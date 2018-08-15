// Typical LinkedList structure that has been modified for an AdjacencyList
public class LinkedList {
	public LLNode head;
	public LLNode tail;
	public int length;
	
	public LinkedList(){
		this.head = null;
		this.tail = null;
		this.length = 0;
	}
	
	public LinkedList(LLNode first){
		this.head = first;
		this.tail = first;
		this.length = 1;
	}
	
	public void append(LLNode newNode){
		if(this.head == null){
			this.head = newNode;
			this.tail = newNode;
		}
		else{
			this.tail.right = newNode;
			newNode.left = this.tail;
			this.tail = newNode;
		}
		this.length++;
	}
	
	public void append(int source, int destination, int distance, GraphNode sourceNode, GraphNode destNode){
		LLNode newNode = new LLNode(source, destination, distance, sourceNode, destNode);
		if(this.head == null){
			this.head = newNode;
			this.tail = newNode;
		}
		else{
			this.tail.right = newNode;
			newNode.left = this.tail;
			this.tail = newNode;
		}
		this.length++;
	}
	
}
