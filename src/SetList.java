
public class SetList {
	public SetNode head;
	public SetNode tail;
	public int length;
	
	public SetList(){
		this.head = null;
		this.tail = null;
	}
	
	public SetList(SetNode firstNode){
		this.head = firstNode;
		this.tail = firstNode;
		this.length = 1;
	}
	
	public void append(SetNode newNode){
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
	
	public void appendList(SetList list){
		this.tail.right = list.head;
		this.tail = list.tail;
		this.length += list.length;
	}
	
}
