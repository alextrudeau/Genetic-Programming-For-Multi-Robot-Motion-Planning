// Object used in SetList. Each SetNode represents a GraphNode in a Set of GraphNodes in a MST that is being constructed
public class SetNode {
	public int value;
	public SetNode left;
	public SetNode right;
	public GraphNode gNode;

	public SetNode(int value, GraphNode node){
		this.value = value;
		this.left = null;
		this.right = null;
		this.gNode = node;
	}
	
}
