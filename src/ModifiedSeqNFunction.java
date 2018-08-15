import org.epochx.epox.Node;
import org.epochx.tools.util.TypeUtils;

/**
 * A function node which provides the facility to sequence a specific number of
 * instructions, specified at construction. Each of the instructions may be any
 * other function or terminal node with a Void return type. This is the same
 * function that Koza calls progN in his work.
 */
public class ModifiedSeqNFunction extends Node {
	
	private RobotsAndGraph robotsAndGraph;
	private int n;
	
	/**
	 * Constructs a SeqNFunction with the given number of <code>null</code>
	 * children.
	 * @param n the arity of the function.
	 */
	public ModifiedSeqNFunction(final int n, RobotsAndGraph robotsAndGraph) {
		this((Node) null);
		
		this.robotsAndGraph = robotsAndGraph;
		this.n = n;
		setChildren(new Node[n]);
	}

	/**
	 * Constructs a SeqNFunction with the given children. When evaluated, each
	 * child will be evaluated in sequence.
	 * 
	 * @param children The child nodes to be executed in sequence.
	 */
	public ModifiedSeqNFunction(final Node ... children) {
		super(children);
	}

	/**
	 * Evaluates this function. Each of the children is evaluated in sequence.
	 * After evaluating its children, this method will return null.
	 */
	@Override
	public Void evaluate() {
		final int arity = getArity();
		
		for (int i = 0; i < arity; i++) {
			getChild(i).evaluate();
		}
		
		return null;
	}

	/**
	 * Returns the identifier of this function which is SEQN.
	 */
	@Override
	public String getIdentifier() {
		return "SEQN"+this.n;
	}

	/**
	 * Returns this function node's return type for the given child input types.
	 * If there is the correct number of inputs of Void type, then the
	 * return type of this function is Void. Otherwise this method will return
	 * <code>null</code> to indicate that the inputs are invalid.
	 * 
	 * @return The Void class or null if the input type is invalid.
	 */
	@Override
	public Class<?> getReturnType(final Class<?> ... inputTypes) {
		if ((inputTypes.length == getArity()) && TypeUtils.allEqual(inputTypes, Void.class)) {
			return Void.class;
		} else {
			return null;
		}
	}
}
