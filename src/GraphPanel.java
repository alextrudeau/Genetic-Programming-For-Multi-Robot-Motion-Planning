
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.HashMap;
import java.util.Random;
import javax.swing.*;
import javax.swing.event.*;

import org.epochx.gp.representation.GPCandidateProgram;
import org.epochx.representation.CandidateProgram;

public class GraphPanel extends JComponent {

    private static final int WIDE = 2800;
    private static final int HIGH = 1700;
    private static final int RADIUS = 80;
    private static final Random rnd = new Random();
    public ControlPanel control = new ControlPanel();
    private int radius = RADIUS;
    public List<GraphNode> nodes = new ArrayList<GraphNode>();
    private List<GraphNode> selected = new ArrayList<GraphNode>();
    public List<Edge> edges = new ArrayList<Edge>();
    private Point mousePt = new Point(WIDE / 2, HIGH / 2);
    private Rectangle mouseRect = new Rectangle();
    private boolean selecting = false;
    
    // Alex Trudeau additions
    public int nodeCount = 0;
    public int timeCount = 0;
    private List<GraphNode> graphNodes = new ArrayList<GraphNode>();
    public AdjacencyList adjList;
    public ArrayList<Robot> robotL = new ArrayList<Robot>(); // List of Robot Objects
    public int communicationRadius = 3;
    public RobotsAndGraph robotsAndGraph;
    public int gpCurrentTime = 0;
    public int maxTimeSteps;
    public int time;
    public RobotsAndGraphList robotsAndGraphList;
    public GPCandidateProgram bestProgram;
    public boolean[] solvedArr;
    
    public static void main(String[] args) throws Exception {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                JFrame f = new JFrame("GraphPanel");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                GraphPanel gp = new GraphPanel();
                f.add(gp.control, BorderLayout.NORTH);
                f.add(new JScrollPane(gp), BorderLayout.CENTER);
                f.getRootPane().setDefaultButton(gp.control.newNodeButton);
                f.pack();
                f.setLocationByPlatform(true);
                f.setVisible(true);
            }
        });
    }

    public GraphPanel() {
        this.setOpaque(true);
        this.addMouseListener(new MouseHandler());
        this.addMouseMotionListener(new MouseMotionHandler());
        time = 0;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDE, HIGH);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(new Color(0x00f0f0f0));
        g.fillRect(0, 0, getWidth(), getHeight());
        for (Edge e : edges) {
            e.draw(g);
        }
        for (GraphNode n : nodes) {
            n.draw(g);
        }
        for(int i = 0; i < robotL.size(); i++){
            robotL.get(i).draw(g);
        }
        if (selecting) {
            g.setColor(Color.darkGray);
            g.drawRect(mouseRect.x, mouseRect.y,
                mouseRect.width, mouseRect.height);
        }
    }
    
    public AdjacencyList mst(int rootNum){
		int arraySize = 0;
		HashMap<String, Edge> hm = new HashMap<String, Edge>();
		
		for(int i = 0; i < edges.size(); i++){
			GraphNode n1 = edges.get(i).n1;
			GraphNode n2 = edges.get(i).n2;
			hm.put(n1.value + "-" + n2.value, edges.get(i));
			if(n1.value > arraySize){
				arraySize = n1.value;
			}
			if(n2.value > arraySize){
				arraySize = n2.value;
			}
		}
		arraySize++;
		
		GraphNode[] nodeArr = new GraphNode[arraySize];
		
		for(int i = 0; i < nodes.size(); i++){
			nodeArr[nodes.get(i).value] = nodes.get(i);
		}
		
		adjList = new AdjacencyList(arraySize);
		
		for(int j = 0; j < edges.size(); j++){
			int source = edges.get(j).n1.value;
			int destination = edges.get(j).n2.value;
			adjList.graphArr[source].append(source, destination, edges.get(j).weight, edges.get(j).n1, edges.get(j).n2);
			adjList.graphArr[destination].append(destination, source, edges.get(j).weight, edges.get(j).n2, edges.get(j).n1);
			if(adjList.nodeL[source] == null){
				adjList.nodeL[source] = edges.get(j).n1;
			}
			if(adjList.nodeL[destination] == null){
				adjList.nodeL[destination] = edges.get(j).n2;
			}
		}
		
		//System.out.println("ORIGINAL GRAPH");
		//adjList.print();
		
		adjList = adjList.mst();
		//System.out.println("MST");
		//adjList.print();
		
		
		
		// Create MST. Then iterate through updated Adjacency List and remove edges from hashmap. Then convert hashmap into edge list
		edges.clear();
		//System.out.println("KEYS");
		//System.out.println(hm.keySet());
		
		for(int i = 0; i < adjList.numNodes; i++){
			//System.out.println("i: " + i);
			LLNode currNode = adjList.graphArr[i].head;
			while(currNode != null){
				if(hm.containsKey(i + "-" + currNode.destination)){
					//System.out.println(i + "-" + currNode.destination);
					edges.add(hm.get(i + "-" + currNode.destination));
				}
				currNode = currNode.right;
			}
		}
		
		// System.out.println("BUILD TREE");
		
		GraphNode root = adjList.createTree(rootNum);
		nodes = adjList.getNodes();
		adjList = new AdjacencyList(adjList.numNodes);
		
		Boolean[] visited = new Boolean[adjList.numNodes];
		for(int j = 0; j < visited.length; j++){
			visited[j] = false;
		}
		
		adjList.makeAL(root, visited);
		
		edges = this.makeEdgeList(adjList);
		
		for(int a = 0; a < edges.size(); a++){
			GraphNode n1 = edges.get(a).n1;
			GraphNode n2 = edges.get(a).n2;
			n1.addNeighbor(n2);
			n2.addNeighbor(n1);
			// System.out.println(edges.get(a).n1.value + " - " + edges.get(a).n2.value);
		}
		
		for(int i = 0; i < nodes.size(); i++){
			nodeArr[nodes.get(i).value] = nodes.get(i); 
		}
		
		this.traverse(root, nodeArr);
		repaint();
		return adjList;
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
			}
			// Branch
			else if(node.branch){
				nodeArr[node.value].color = Color.GREEN;
			}
			// Leaf
			else if(node.leaf){
				nodeArr[node.value].color = Color.YELLOW;
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

    private class MouseHandler extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            selecting = false;
            mouseRect.setBounds(0, 0, 0, 0);
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
            e.getComponent().repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            mousePt = e.getPoint();
            if (e.isShiftDown()) {
                GraphNode.selectToggle(nodes, mousePt);
            } else if (e.isPopupTrigger()) {
            	GraphNode.selectOne(nodes, mousePt);
                showPopup(e);
            } else if (GraphNode.selectOne(nodes, mousePt)) {
                selecting = false;
            } else {
            	GraphNode.selectNone(nodes);
                selecting = true;
            }
            e.getComponent().repaint();
        }

        private void showPopup(MouseEvent e) {
            control.popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private class MouseMotionHandler extends MouseMotionAdapter {

        Point delta = new Point();

        @Override
        public void mouseDragged(MouseEvent e) {
            if (selecting) {
                mouseRect.setBounds(
                    Math.min(mousePt.x, e.getX()),
                    Math.min(mousePt.y, e.getY()),
                    Math.abs(mousePt.x - e.getX()),
                    Math.abs(mousePt.y - e.getY()));
                GraphNode.selectRect(nodes, mouseRect);
            } else {
                delta.setLocation(
                    e.getX() - mousePt.x,
                    e.getY() - mousePt.y);
                GraphNode.updatePosition(nodes, delta);
                mousePt = e.getPoint();
            }
            e.getComponent().repaint();
        }
    }

    public JToolBar getControlPanel() {
        return control;
    }

    private class ControlPanel extends JToolBar {

        private Action newNode = new NewNodeAction("New");
        private Action gpTimeStep = new GPTimeStepAction("GP Time Step");
        private Action neighbors = new NeighborAction("Neighbors");
        private Action freeNeighbors = new FreeNeighborAction("FreeNeighbors");
        private Action children = new ChildrenAction("Children");
        private Action move = new MoveAction("Time Step");
        private Action newRobot = new NewRobotAction("NewRobot");
        private Action robotNetwork = new RobotNetworkAction("RobotNetwork");
        private Action directNetwork = new DirectNetworkAction("DirectNetwork");
        private Action completeNetwork = new CompleteNetworkAction("CompleteNetwork");
        private Action mst = new MSTAction("MST");
        private Action clearAll = new ClearAction("Clear");
        private Action color = new ColorAction("Color");
        private Action connect = new ConnectAction("Connect");
        private Action delete = new DeleteAction("Delete");
        private Action ancestors = new AncestorsAction("Ancestors");
        private Action random = new RandomAction("Random");
        private Action nodeValue = new NodeValueAction("NodeValue");
        private Action shortestPath = new ShortestPathAction("ShortestPath");
        private Action previousTestExample = new PreviousTestExampleAction("PreviousTestExample");
        private Action nextTestExample = new NextTestExampleAction("NextTestExample");
        private Action gpTestSetStep = new GPTestSetStepAction("GPTestSetStep");
        
        private JButton newNodeButton = new JButton(newNode);
        private JButton moveButton = new JButton(move);
        private JButton gpTimeStepButton = new JButton(gpTimeStep);
        private JButton previousTestExampleButton = new JButton(previousTestExample);
        private JButton nextTestExampleButton = new JButton(nextTestExample);
        private JButton gpTestSetStepButton = new JButton(gpTestSetStep);
        private ColorIcon hueIcon = new ColorIcon(Color.blue);
        private JPopupMenu popup = new JPopupMenu();

        ControlPanel() {
            this.setLayout(new FlowLayout(FlowLayout.LEFT));
            this.setBackground(Color.lightGray);

            this.add(newNodeButton);
            this.add(new JButton(clearAll));
            this.add(new JButton(color));
            this.add(new JLabel(hueIcon));
            JSpinner js = new JSpinner();
            js.setModel(new SpinnerNumberModel(RADIUS, 5, 100, 5));
            js.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    JSpinner s = (JSpinner) e.getSource();
                    radius = (Integer) s.getValue();
                    GraphNode.updateRadius(nodes, radius);
                    GraphPanel.this.repaint();
                }
            });
            this.add(new JLabel("Size:"));
            this.add(js);
            this.add(new JButton(random));
            this.add(new JButton(mst));
            this.add(moveButton);
            this.add(gpTimeStepButton);
            this.add(previousTestExampleButton);
            this.add(nextTestExampleButton);
            this.add(gpTestSetStepButton);
            
            popup.add(new JMenuItem(color));
            popup.add(new JMenuItem(connect));
            //popup.add(new JMenuItem(delete));
            popup.add(new JMenuItem(newRobot));
            popup.add(new JMenuItem(robotNetwork));
            popup.add(new JMenuItem(directNetwork));
            popup.add(new JMenuItem(completeNetwork));
            popup.add(new JMenuItem(nodeValue));
            popup.add(new JMenuItem(ancestors));
            popup.add(new JMenuItem(shortestPath));
            popup.add(new JMenuItem(neighbors));
            popup.add(new JMenuItem(freeNeighbors));
            popup.add(new JMenuItem(children));
        }
    }
    
    private class NeighborAction extends AbstractAction {

        public NeighborAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
        	GraphNode.getSelected(nodes, selected);
        	if(selected.size() == 1){
        		System.out.println("NEIGHBORS");
        		System.out.println("NUM NEIGHBORS: " + selected.get(0).neighbors.size());
        		for(int i = 0; i < selected.get(0).neighbors.size(); i++){
        			System.out.println(selected.get(0).neighbors.get(i).value);
        		}
        	}
        }
    }
    
    private class FreeNeighborAction extends AbstractAction {

        public FreeNeighborAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
        	GraphNode.getSelected(nodes, selected);
        	if(selected.size() == 1){
        		System.out.println("FREE NEIGHBORS");
        		ArrayList<GraphNode> freeNeighbors = selected.get(0).robot.getFreeAdjacentNodes();
        		for(int i = 0; i < freeNeighbors.size(); i++){
        			System.out.println(freeNeighbors.get(i).value);
        		}
        	}
        }
    }
    
    private class ChildrenAction extends AbstractAction {

        public ChildrenAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
        	GraphNode.getSelected(nodes, selected);
        	if(selected.size() == 1){
        		System.out.println("CHILDREN:");
        		System.out.println("NUM CHILDREN: " + selected.get(0).children.size());
        		for(int i = 0; i < selected.get(0).children.size(); i++){
        			System.out.println(selected.get(0).children.get(i).value);
        		}
        	}
        }
    }

    private class ClearAction extends AbstractAction {

        public ClearAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            nodes.clear();
            edges.clear();
            repaint();
        }
    }
    
    private class ShortestPathAction extends AbstractAction {

        public ShortestPathAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
        	if(selected.size() == 1 && selected.get(0).robot != null){
        		GraphNode source = selected.get(0);
        		GraphNode destination = source.robot.objective;
        		ArrayList<GraphNode> path = adjList.Dijkstra(source, destination);
        		Collections.reverse(path);
        		path.remove(0);
        		for(int k = 0; k < path.size(); k++){
        			System.out.println(path.get(k).value);
        		}
        	}
        }
    }
    
    private class RobotNetworkAction extends AbstractAction {

        public RobotNetworkAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
        	for(int i = 0; i < robotL.size(); i++){
        		Robot robot = robotL.get(i);
        		ArrayList<Robot> network = adjList.directCommunication(robot.currNode, communicationRadius);
        		robot.directNetwork = network;
        	}
        	GraphNode.getSelected(nodes, selected);
        	if(selected.size() == 1 && selected.get(0).robot != null){
        		Robot robot = selected.get(0).robot;
        		robot.getCompleteNetwork();
        		for(int k = 0; k < robot.completeNetwork.size(); k++){
        			robot.completeNetwork.get(k).color = Color.GRAY;
        		}
        		for(int j = 0; j < robot.directNetwork.size(); j++){
        			robot.directNetwork.get(j).color = Color.CYAN;
        		}
        	}
            repaint();
        }
    }
    
    private class DirectNetworkAction extends AbstractAction {

        public DirectNetworkAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
        	GraphNode.getSelected(nodes, selected);
        	if(selected.size() == 1){
        		Robot robot = selected.get(0).robot;
        		ArrayList<Robot> directNetwork = adjList.directCommunication(robot.currNode, 2);
        		System.out.println("Robot " + robot.objective.value + "'s Network: ");
        		for(int j = 0; j < directNetwork.size(); j++){
        			System.out.println(directNetwork.get(j).objective.value);
        		}
        	}
        }
    }
    
    private class CompleteNetworkAction extends AbstractAction {

        public CompleteNetworkAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
        	GraphNode.getSelected(nodes, selected);
        	if(selected.size() == 1){
        		for(int i = 0; i < robotL.size(); i++) {
        			robotL.get(i).directNetwork = adjList.directCommunication(robotL.get(i).currNode, 2);
        		}
        		Robot robot = selected.get(0).robot;
        		robot.getCompleteNetwork();
        		System.out.println("Robot " + robot.objective.value + "'s Complete Network: ");
        		for(int j = 0; j < robot.completeNetwork.size(); j++){
        			System.out.println(robot.completeNetwork.get(j).objective.value);
        		}
        	}
        }
    }
    
    private class AncestorsAction extends AbstractAction {

        public AncestorsAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
        	GraphNode.getSelected(nodes, selected);
        	if(selected.size() == 1){
        		for(int i = 0; i < selected.get(0).ancestors.size(); i++){
        			
        			System.out.println("Ancestor: " + selected.get(0).ancestors.get(i).value);
        		}
        	}
        }
    }
    
    
    private class MSTAction extends AbstractAction {
    	
    	public MSTAction(String name) {
    		super(name);
    	}
    	
    	public void mst(){
    		int rootNum = Integer.parseInt(JOptionPane.showInputDialog("Enter Root Number"));
    		int arraySize = 0;
    		HashMap<String, Edge> hm = new HashMap<String, Edge>();
    		
    		for(int i = 0; i < edges.size(); i++){
    			GraphNode n1 = edges.get(i).n1;
    			GraphNode n2 = edges.get(i).n2;
    			hm.put(n1.value + "-" + n2.value, edges.get(i));
    			if(n1.value > arraySize){
    				arraySize = n1.value;
    			}
    			if(n2.value > arraySize){
    				arraySize = n2.value;
    			}
    		}
    		arraySize++;
    		
    		GraphNode[] nodeArr = new GraphNode[arraySize];
    		
    		for(int i = 0; i < nodes.size(); i++){
    			nodeArr[nodes.get(i).value] = nodes.get(i);
    		}
    		
    		adjList = new AdjacencyList(arraySize);
    		
    		for(int j = 0; j < edges.size(); j++){
    			int source = edges.get(j).n1.value;
    			int destination = edges.get(j).n2.value;
				adjList.graphArr[source].append(source, destination, edges.get(j).weight, edges.get(j).n1, edges.get(j).n2);
				adjList.graphArr[destination].append(destination, source, edges.get(j).weight, edges.get(j).n2, edges.get(j).n1);
				if(adjList.nodeL[source] == null){
					adjList.nodeL[source] = edges.get(j).n1;
				}
				if(adjList.nodeL[destination] == null){
					adjList.nodeL[destination] = edges.get(j).n2;
				}
    		}
    		
    		//System.out.println("ORIGINAL GRAPH");
    		//adjList.print();
    		
    		adjList = adjList.mst();
    		//System.out.println("MST");
    		//adjList.print();
    		
    		
    		
    		// Create MST. Then iterate through updated Adjacency List and remove edges from hashmap. Then convert hashmap into edge list
    		edges.clear();
    		//System.out.println("KEYS");
    		//System.out.println(hm.keySet());
    		
    		for(int i = 0; i < adjList.numNodes; i++){
    			// System.out.println("i: " + i);
    			LLNode currNode = adjList.graphArr[i].head;
    			while(currNode != null){
    				if(hm.containsKey(i + "-" + currNode.destination)){
    					// System.out.println(i + "-" + currNode.destination);
    					edges.add(hm.get(i + "-" + currNode.destination));
    				}
    				currNode = currNode.right;
    			}
    		}
    		
    		// System.out.println("BUILD TREE");
    		
    		GraphNode root = adjList.createTree(rootNum);
    		nodes = adjList.getNodes();
    		adjList = new AdjacencyList(adjList.numNodes);
    		
    		Boolean[] visited = new Boolean[adjList.numNodes];
    		for(int j = 0; j < visited.length; j++){
    			visited[j] = false;
    		}
    		
    		adjList.makeAL(root, visited);
    		
    		edges = this.makeEdgeList(adjList);
    		
    		for(int a = 0; a < edges.size(); a++){
    			GraphNode n1 = edges.get(a).n1;
    			GraphNode n2 = edges.get(a).n2;
				n1.addNeighbor(n2);
				n2.addNeighbor(n1);
    			// System.out.println(edges.get(a).n1.value + " - " + edges.get(a).n2.value);
    		}
    		
    		for(int i = 0; i < nodes.size(); i++){
    			nodeArr[nodes.get(i).value] = nodes.get(i); 
    		}
    		
    		this.traverse(root, nodeArr);
    		
    		// Generating list of ancestors for each node in the graph
    		for(int i = 0; i < nodes.size(); i++){
    			GraphNode tempNode = nodes.get(i);
    			nodes.get(i).ancestors = new ArrayList<GraphNode>();
    			while(tempNode.root == false){
    				nodes.get(i).ancestors.add(tempNode.parent);
    				tempNode = tempNode.parent;
    			}
    		}
    		
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
    	
    	public void actionPerformed(ActionEvent e) {
            this.mst();
            repaint();
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
    			}
    			// Branch
    			else if(node.branch){
    				nodeArr[node.value].color = Color.GREEN;
    			}
    			// Leaf
    			else if(node.leaf){
    				nodeArr[node.value].color = Color.YELLOW;
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
    }
    

    private class ColorAction extends AbstractAction {

        public ColorAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            Color color = control.hueIcon.getColor();
            color = JColorChooser.showDialog(
                GraphPanel.this, "Choose a color", color);
            if (color != null) {
            	GraphNode.updateColor(nodes, color);
                control.hueIcon.setColor(color);
                control.repaint();
                repaint();
            }
        }
    }
    
    private class NodeValueAction extends AbstractAction {

        public NodeValueAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
        	GraphNode.getSelected(nodes, selected);
        	for(int i = 0; i < selected.size(); i++){
        		System.out.println("NODE VALUE: " + selected.get(i).value);
        	}
        }
    }
    
    private class NewRobotAction extends AbstractAction {

        public NewRobotAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            GraphNode.getSelected(nodes, selected);
            if (selected.size() == 1) {
            	int obj = Integer.parseInt(JOptionPane.showInputDialog("Enter objective node"));
            	GraphNode objNode = adjList.nodeL[obj];
            	System.out.println("OBJ NODE: " + objNode.value);
            	Robot newRobot = new Robot(selected.get(0), objNode, nodes.size());
            	newRobot.color = Color.black;
                GraphNode currNode = selected.get(0);
                ArrayList<GraphNode> path = adjList.Dijkstra(currNode, objNode);
        		path.remove(0);
        		for(int k = 0; k < path.size(); k++){
        			System.out.println(path.get(k).value);
        		}
        		newRobot.path = path;
                currNode.robot = newRobot;
                newRobot.priority = obj;
                robotL.add(newRobot);
            }
            repaint();
        }
    }
    
    private class ConnectAction extends AbstractAction {

        public ConnectAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
        	GraphNode.getSelected(nodes, selected);
            if (selected.size() > 1) {
                for (int i = 0; i < selected.size() - 1; ++i) {
                	GraphNode n1 = selected.get(i);
                	GraphNode n2 = selected.get(i + 1);
                    Edge newEdge = new Edge(n1, n2);
                    newEdge.weight = Integer.parseInt(JOptionPane.showInputDialog("Enter an edge weight"));
                    edges.add(newEdge);
                }
            }
            repaint();
        }
    }
    

    private class DeleteAction extends AbstractAction {

        public DeleteAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            ListIterator<GraphNode> iter = nodes.listIterator();
            while (iter.hasNext()) {
            	GraphNode n = iter.next();
                if (n.isSelected()) {
                    deleteEdges(n);
                    iter.remove();
                }
            }
            repaint();
        }

        private void deleteEdges(GraphNode n) {
            ListIterator<Edge> iter = edges.listIterator();
            while (iter.hasNext()) {
                Edge e = iter.next();
                if (e.n1 == n || e.n2 == n) {
                    iter.remove();
                }
            }
        }
    }
    
    private class NextTestExampleAction extends AbstractAction {

        public NextTestExampleAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
    		robotsAndGraphList.currExample++;
    		System.out.println("Test Graph: " + robotsAndGraphList.currExample);
    		robotsAndGraph = robotsAndGraphList.list[robotsAndGraphList.currExample];
    		robotsAndGraph.bestProgram = bestProgram;
    		
    		nodes = robotsAndGraph.nodeL;
            edges = robotsAndGraph.edgeL;
            robotL = robotsAndGraph.robotList;
            maxTimeSteps = robotsAndGraph.currentTimeStep;
            robotsAndGraph.currentTimeStep = 0;
            
            solvedArr = new boolean[robotsAndGraph.robotList.size()];
            
            for(int a = 0; a < solvedArr.length; a++) {
    			solvedArr[a] = false;
    		}
            
            robotsAndGraph.startingFormation();
            
            repaint();
        }
    }
    
    private class PreviousTestExampleAction extends AbstractAction {

        public PreviousTestExampleAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
    		robotsAndGraphList.currExample--;
    		System.out.println("Test Graph: " + robotsAndGraphList.currExample);
    		robotsAndGraph = robotsAndGraphList.list[robotsAndGraphList.currExample];
    		robotsAndGraph.bestProgram = bestProgram;
    		
    		nodes = robotsAndGraph.nodeL;
            edges = robotsAndGraph.edgeL;
            robotL = robotsAndGraph.robotList;
            maxTimeSteps = robotsAndGraph.currentTimeStep;
            robotsAndGraph.currentTimeStep = 0;
            
            solvedArr = new boolean[robotsAndGraph.robotList.size()];
            
            for(int a = 0; a < solvedArr.length; a++) {
    			solvedArr[a] = false;
    		}
            
            robotsAndGraph.startingFormation();
            
            repaint();
        }
    }
    
    private class GPTestSetStepAction extends AbstractAction {

        public GPTestSetStepAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
        	if(robotsAndGraph.currentTimeStep < maxTimeSteps){
        		System.out.println("CURRENT TIME: " + robotsAndGraph.currentTimeStep);
        		for(int i = 0; i < robotsAndGraph.robotList.size(); i++){
    				robotsAndGraph.currentRobot = i;
    				//System.out.println("ROBOT: " + robotsAndGraph.robotList.get(i).objective.value + " on node " + robotsAndGraph.robotList.get(i).currNode.value);
    				robotsAndGraph.bestProgram.evaluate();
    			}
        		
        		for(int j = 0; j < robotsAndGraph.robotList.size(); j++) {
        			if(robotsAndGraph.robotList.get(j).currNode == robotsAndGraph.robotList.get(j).objective) {
        				solvedArr[j] = true;
        				robotsAndGraph.robotList.get(j).gpSolved = true;
        			}
        			else {
        				solvedArr[j] = robotsAndGraph.robotList.get(j).gpSolved;
        			}
        		}
        		
        		for(int k = 0; k < robotsAndGraph.robotList.size(); k++) {
        			System.out.println("Robot: " + robotsAndGraph.robotList.get(k).objective.value + " solved: " + robotsAndGraph.robotList.get(k).gpSolved);
        		}
        		
    			robotsAndGraph.currentTimeStep++;
                repaint();
                
        	}
        	else{
        		int finalDistance = 0;
        		int totalMoves = 0;
        		for(int i = 0; i < robotsAndGraph.robotList.size(); i++){
        			ArrayList<GraphNode> path = robotsAndGraph.robotList.get(i).adjList.Dijkstra(robotsAndGraph.robotList.get(i).currNode, robotsAndGraph.robotList.get(i).objective);
        			System.out.println("Robot: " + robotsAndGraph.robotList.get(i).objective.value);
        			for(int j = 0; j < path.size(); j++){
        				System.out.println(path.get(j).value);
        			}
        			finalDistance += path.size() - 1;
        			totalMoves += robotsAndGraph.robotList.get(i).numMoves;
        		}
        		
        		System.out.println("ACTUAL FITNESS: " + finalDistance*(finalDistance + totalMoves));
        		
        		robotsAndGraphList.currExample++;
        		robotsAndGraph = robotsAndGraphList.list[robotsAndGraphList.currExample];
        		
        		nodes = robotsAndGraph.nodeL;
                edges = robotsAndGraph.edgeL;
                robotL = robotsAndGraph.robotList;
                maxTimeSteps = robotsAndGraph.currentTimeStep;
                robotsAndGraph.currentTimeStep = 0;
                robotsAndGraph.bestProgram = bestProgram;
                
                robotsAndGraph.startingFormation();
                
                repaint();
        	}
        }
    }
    
    private class GPTimeStepAction extends AbstractAction {

        public GPTimeStepAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
        	if(robotsAndGraph.currentTimeStep < maxTimeSteps){
        		System.out.println("CURRENT TIME: " + robotsAndGraph.currentTimeStep);
        		for(int i = 0; i < robotsAndGraph.robotList.size(); i++){
    				robotsAndGraph.currentRobot = i;
    				System.out.println("ROBOT: " + robotsAndGraph.robotList.get(i).objective.value + " on node " + robotsAndGraph.robotList.get(i).currNode.value);
    				robotsAndGraph.bestProgram.evaluate();
    			}
        		System.out.println();
    			robotsAndGraph.currentTimeStep++;
                repaint();
                
        	}
        	else{
        		int finalDistance = 0;
        		int totalMoves = 0;
        		for(int i = 0; i < robotsAndGraph.robotList.size(); i++){
        			ArrayList<GraphNode> path = robotsAndGraph.robotList.get(i).adjList.Dijkstra(robotsAndGraph.robotList.get(i).currNode, robotsAndGraph.robotList.get(i).objective);
        			System.out.println("Robot: " + robotsAndGraph.robotList.get(i).objective.value);
        			for(int j = 0; j < path.size(); j++){
        				System.out.println(path.get(j).value);
        			}
        			finalDistance += path.size() - 1;
        			totalMoves += robotsAndGraph.robotList.get(i).numMoves;
        		}
        		
        		System.out.println("ACTUAL FITNESS: " + finalDistance*(finalDistance + totalMoves));
        	}
        }
    }

    private class NewNodeAction extends AbstractAction {

        public NewNodeAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
        	GraphNode.selectNone(nodes);
            Point p = mousePt.getLocation();
            Color color = control.hueIcon.getColor();
            GraphNode n = new GraphNode(nodeCount, p, radius, color);
            nodeCount++;
            n.setSelected(true);
            nodes.add(n);
            repaint();
        }
    }
    
    private class MoveAction extends AbstractAction {

        public MoveAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
        	boolean allSolved = true;
        	
        	for(int i = 0; i < robotL.size(); i++){
        		if(!robotL.get(i).solved){
        			allSolved = false;
        		}
        	}
        	
        	if(allSolved){
        		System.out.println("Graph Solved in " + time + " time steps");
        	}
        	else{
	        	time += 1;
	        	System.out.println("TIME STEP: " + time);
	        	for(int j = 0; j < robotL.size(); j++){
	        		robotL.get(j).adjList = adjList;
	        		robotL.get(j).directNetwork = adjList.directCommunication(robotL.get(j).currNode, 2);
	        	}
	        	
	        	Robot highestPriority = null;
	    		// Get highest-priority unsolved robot
	    		for(int i = 0; i < robotL.size(); i++){
	    			if(timeCount == 0){
	    				System.out.println("Robot: " + robotL.get(i).objective.value + " CurrNode: " + robotL.get(i).currNode);
	    				robotL.get(i).directNetwork = adjList.directCommunication(robotL.get(i).currNode, 2);
	    			}
	    			
	    			System.out.println("i: " + i);
	    			System.out.println("Current Bot: current node: " + robotL.get(i).currNode.value + ", objective: " + robotL.get(i).objective.value + ", priority: " + robotL.get(i).priority);
	    			if(highestPriority == null){
	    				System.out.println("current highest priority: null");
	    			}
	    			else{
	    				System.out.println("current highest priority: " + highestPriority.objective.value);
	    			}
	    			if(highestPriority == null && !robotL.get(i).solved){
	    				highestPriority = robotL.get(i);
	    			}
	    			else if(highestPriority == null){
	    				continue;
	    			}
	    			else if((robotL.get(i).priority < highestPriority.priority) && (!robotL.get(i).solved)){
	    				highestPriority = robotL.get(i);
	    			}
	    		}
	    		
	    		/*
	    		// Check if Highest Priority Unsolved Robot is solved
	    		if(highestPriority.currNode == highestPriority.objective && !highestPriority.solved){
	    			boolean higherPrioritySolved = true;
	    			for(int i = 0; i < robotL.size(); i++){
	    				if(robotL.get(i).priority < highestPriority.priority && robotL.get(i).solved){
	    					higherPrioritySolved = higherPrioritySolved && true;
	    				}
	    				else if(robotL.get(i).priority < highestPriority.priority && !robotL.get(i).solved){
	    					higherPrioritySolved = higherPrioritySolved && false;
	    				}
	    			}
	    			if(higherPrioritySolved){
	    				System.out.println("Robot " + highestPriority.objective.value + " is solved");
	        			highestPriority.solved = true;
	        			highestPriority.solvedRobots.add(highestPriority);
	    			}
	    			else{
	    				highestPriority.solved = false;
	    			}
	    		}
	    		*/
	    		
	    		if(highestPriority != null){
	    			System.out.println("Highest Priority Bot: " + highestPriority.objective.value);
	    		}
	    		else{
	    			System.out.println("No Highest Priority Bot in Communication Network");
	    		}
	    		
	    		
	    		// According to research paper, highest priority bot is 'SWAPPING'
	    		// highestPriority.status = "SWAPPING";
	    		
	    		// Call plan() on each robot
	        	for(int i = 0; i < robotL.size(); i++){
	        		System.out.println("CURRENT ROBOT: OBJECTIVE: " + robotL.get(i).objective.value + " CURRENT NODE: " + robotL.get(i).currNode.value + " STATUS: " + robotL.get(i).status + " SOLVED: " + robotL.get(i).solved);
	        		robotL.get(i).color = Color.BLACK;
	        		robotL.get(i).directNetwork = adjList.directCommunication(robotL.get(i).currNode, robotL.get(i).radius);
	        		System.out.println("DIRECT NETWORK:");
	        		for(int k = 0; k < robotL.get(i).directNetwork.size(); k++){
	        			System.out.println(robotL.get(i).directNetwork.get(k).objective.value);
	        		}
	        		robotL.get(i).completeNetwork = robotL.get(i).getCompleteNetwork();
	        		System.out.println("COMPLETE NETWORK:");
	        		for(int l = 0; l < robotL.get(i).completeNetwork.size(); l++){
	        			System.out.println(robotL.get(i).completeNetwork.get(l).objective.value);
	        		}
	        		robotL.get(i).plan(highestPriority);;
	        	}
	        	
	        	/*
	        	for(int a = 0; a < robotL.size(); a++){
	        		System.out.println("ROBOT: " + robotL.get(a).objective.value);
	        		System.out.println("CUR NODE: " + robotL.get(a).currNode.value);
	        	}
	        	
	        	for(int b = 0; b < nodes.size(); b++){
	        		System.out.println("NODE: " + nodes.get(b).value);
	        		if(nodes.get(b).robot != null){
	        			System.out.println("ROBOT: " + nodes.get(b).robot.objective.value);
	        		}
	        		else{
	        			System.out.println("ROBOT: null");
	        		}
	        	}
	        	*/
	            repaint();
        	}
        }
    }

    private class RandomAction extends AbstractAction {

        public RandomAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < 16; i++) {
                Point p = new Point(rnd.nextInt(getWidth()), rnd.nextInt(getHeight()));
                // I THINK nodeCount has incorrect value at this point
                nodes.add(new GraphNode(nodeCount, p, radius, new Color(rnd.nextInt())));
            }
            repaint();
        }
    }

    private static class ColorIcon implements Icon {

        private static final int WIDE = 20;
        private static final int HIGH = 20;
        private Color color;

        public ColorIcon(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x, y, WIDE, HIGH);
        }

        public int getIconWidth() {
            return WIDE;
        }

        public int getIconHeight() {
            return HIGH;
        }
    }
    
    private class CirclePanel extends JPanel {
    	@Override
    	protected void paintComponent(Graphics g) { 
    	    super.paintComponent(g);

    	    g.drawOval(0, 0, g.getClipBounds().width, g.getClipBounds().height);
    	    }
    	}
    
}

