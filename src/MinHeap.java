import java.util.Arrays;
import java.util.NoSuchElementException;
 
/** Class BinaryHeap **/
class MinHeap    
{
    /** The number of children each node has **/
    private static final int d = 2;
    private int heapSize;
    public GraphNode[] heap;
 
    /** Constructor **/    
    public MinHeap(int capacity)
    {
        heapSize = 0;
        heap = new GraphNode[capacity + 1];
        for(int i = 0; i < heap.length; i++){
        	heap[i] = null;
        }
    }
 
    /** Function to check if heap is empty **/
    public boolean isEmpty( )
    {
        return heapSize == 0;
    }
 
    /** Check if heap is full **/
    public boolean isFull( )
    {
        return heapSize == heap.length;
    }
 
    /** Clear heap */
    public void makeEmpty( )
    {
        heapSize = 0;
    }
 
    /** Function to  get index parent of i **/
    private int parent(int i) 
    {
        return (i - 1)/d;
    }
 
    /** Function to get index of k th child of i **/
    private int kthChild(int i, int k) 
    {
        return d * i + k;
    }
 
    /** Function to insert element */
    public void insert(GraphNode x)
    {
        if (isFull( ) )
            throw new NoSuchElementException("Overflow Exception");
        /** Percolate up **/
        heap[heapSize++] = x;
        heapifyUp(heapSize - 1);
    }
 
    /** Function to find least element **/
    public GraphNode findMin( )
    {
        if (isEmpty() )
            throw new NoSuchElementException("Underflow Exception");           
        return heap[0];
    }
 
    /** Function to delete min element **/
    public GraphNode deleteMin()
    {
        GraphNode keyItem = heap[0];
        delete(0);
        return keyItem;
    }
 
    /** Function to delete element at an index **/
    public GraphNode delete(int ind)
    {
        if (isEmpty() )
            throw new NoSuchElementException("Underflow Exception");
        GraphNode keyItem = heap[ind];
        heap[ind] = heap[heapSize - 1];
        heapSize--;
        heapifyDown(ind);        
        return keyItem;
    }
 
    /** Function heapifyUp  **/
    private void heapifyUp(int childInd)
    {
        GraphNode tmp = heap[childInd];    
        while (childInd > 0 && tmp.DijkDistance < heap[parent(childInd)].DijkDistance)
        {
            heap[childInd] = heap[ parent(childInd) ];
            childInd = parent(childInd);
        }                   
        heap[childInd] = tmp;
    }
 
    /** Function heapifyDown **/
    private void heapifyDown(int ind)
    {
        int child;
        GraphNode tmp = heap[ind];
        while (kthChild(ind, 1) < heapSize)
        {
            child = minChild(ind);
            if (heap[child].DijkDistance < tmp.DijkDistance)
                heap[ind] = heap[child];
            else
                break;
            ind = child;
        }
        heap[ind] = tmp;
    }
 
    /** Function to get smallest child **/
    private int minChild(int ind) 
    {
        int bestChild = kthChild(ind, 1);
        int k = 2;
        int pos = kthChild(ind, k);
        while ((k <= d) && (pos < heapSize)) 
        {
            if (heap[pos].DijkDistance < heap[bestChild].DijkDistance) 
                bestChild = pos;
            pos = kthChild(ind, k++);
        }    
        return bestChild;
    }
    
    public int getIndex(GraphNode node){
    	for(int i = 0; i < this.heap.length; i++){
    		if(this.heap[i] == node){
    			return i;
    		}
    	}
    	return -1;
    }
 
    /** Function to print heap **/
    public void printHeap()
    {
    	int level = 0;
    	int lastNode = 0;
        System.out.println("Heap");
        System.out.println("level: " + level);
        for(int i = 0; i < heapSize; i++){
        	System.out.print("Value: " + heap[i].value + " DijkDistance: " + heap[i].DijkDistance);
        	if(i == Math.pow(2, level) - 1 + lastNode){
        		System.out.println();
        		level++;
        		lastNode = i+1;
        		System.out.println("level: " + level);
        	}
        }
        System.out.println();
    }     
}
