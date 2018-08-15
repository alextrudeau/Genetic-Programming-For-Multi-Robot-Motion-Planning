
public class QueueNode<E> {

	public QueueNode<E> next;
	public E luggage;
	
	public QueueNode(E luggage){
		this.next = null;
		this.luggage = luggage;
	}
	
}
