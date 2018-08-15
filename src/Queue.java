
public class Queue<E> {
	public QueueNode<E> head;
	public QueueNode<E> tail;
	public int length;
	
	public Queue(){
		this.head = null;
		this.tail = null;
		this.length = 0;
	}
	
	public void enqueue(E luggage){
		QueueNode<E> newNode = new QueueNode<E>(luggage);
		if(this.length == 0){
			this.head = newNode;
			this.tail = newNode;
			this.length = 1;
		}
		else{
			this.tail.next = newNode;
			this.tail = newNode;
			this.length++;
		}
	}
	
	public QueueNode dequeue(){
		QueueNode<E> temp = new QueueNode<E>(this.head.luggage);
		this.head = this.head.next;
		this.length--;
		return temp;
	}
	
}
