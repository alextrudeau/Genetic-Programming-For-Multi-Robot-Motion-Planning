import java.util.ArrayList;
import java.util.List;

import java.awt.*;

// GraphNode is the object used to represent a node in the GUI and that is manipulated in the MST algorithm

public class GraphNode implements Comparable<GraphNode>{
	public int value;
	public int priority;
	public boolean twig;
	public boolean root;
	public boolean branch;
	public boolean leaf;
	public ArrayList<GraphNode> children;
	public GraphNode parent;
	public Point p;
    public int r;
    public Color color;
    private boolean selected = false;
    public Rectangle b = new Rectangle();
    public Robot robot;
    int parentDist;
    // Additions
    public ArrayList<GraphNode> neighbors = new ArrayList<GraphNode>();
    public ArrayList<GraphNode> ancestors = new ArrayList<GraphNode>();
    int DijkDistance;
    boolean triedFreeNeighbor;
	
	public GraphNode(int value, Point p, int r, Color color){
		this.value = value;
		this.priority = 0;
		this.twig = false;
		this.root = false;
		this.leaf = false;
		this.branch = false;
		this.children = new ArrayList<GraphNode>();
		this.ancestors = new ArrayList<GraphNode>();
		this.parent = null;
		this.parentDist = 0;
		this.robot = null;
		this.p = p;
		this.r = r;
		this.color = color;
		this.DijkDistance = Integer.MAX_VALUE;
		this.triedFreeNeighbor = false;
		setBoundary(b);
	}
	
	public GraphNode(GraphNode oldNode){
		this.value = oldNode.value;
		this.priority = oldNode.priority;
		this.twig = oldNode.twig;
		this.root = oldNode.root;
		this.leaf = oldNode.leaf;
		this.branch = oldNode.branch;
		this.children = oldNode.children;
		this.parent = oldNode.parent;
		this.parentDist = oldNode.parentDist;
		this.robot = null;
		this.p = oldNode.p;
		this.r = oldNode.r;
		this.color = oldNode.color;
		this.DijkDistance = oldNode.DijkDistance;
		this.triedFreeNeighbor = oldNode.triedFreeNeighbor;
		setBoundary(b);
	}
	
	
	/**
     * Calculate this node's rectangular boundary.
     */
    public void setBoundary(Rectangle b) {
        b.setBounds(p.x - r, p.y - r, 2 * r, 2 * r);
    }

    /**
     * Draw this node.
     */
    public void draw(Graphics g) {
        g.setColor(this.color);
        g.fillOval(b.x, b.y, b.width, b.height);
        if (selected) {
            g.setColor(Color.darkGray);
            g.drawRect(b.x, b.y, b.width, b.height);
        }
        g.setColor(Color.BLACK);
        g.drawString("" + this.value, b.x + b.width/2, b.y + b.height/2);
        /*
        if(this.robot != null){
        	g.setColor(color.BLACK);
        	this.robot.draw(g);
        	g.setColor(Color.white);
        	g.drawString("" + this.robot.objective.value, b.x + b.width/2, b.y + b.height/2);
        }
        */
    }

    /**
     * Return this node's location.
     */
    public Point getLocation() {
        return p;
    }

    /**
     * Return true if this node contains p.
     */
    public boolean contains(Point p) {
        return b.contains(p);
    }

    /**
     * Return true if this node is selected.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Mark this node as selected.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Collected all the selected nodes in list.
     */
    public static void getSelected(List<GraphNode> list, List<GraphNode> selected) {
        selected.clear();
        for (GraphNode n : list) {
            if (n.isSelected()) {
                selected.add(n);
            }
        }
    }

    /**
     * Select no nodes.
     */
    public static void selectNone(List<GraphNode> list) {
        for (GraphNode n : list) {
            n.setSelected(false);
        }
    }

    /**
     * Select a single node; return true if not already selected.
     */
    public static boolean selectOne(List<GraphNode> list, Point p) {
        for (GraphNode n : list) {
            if (n.contains(p)) {
                if (!n.isSelected()) {
                    GraphNode.selectNone(list);
                    n.setSelected(true);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Select each node in r.
     */
    public static void selectRect(List<GraphNode> list, Rectangle r) {
        for (GraphNode n : list) {
            n.setSelected(r.contains(n.p));
        }
    }

    /**
     * Toggle selected state of each node containing p.
     */
    public static void selectToggle(List<GraphNode> list, Point p) {
        for (GraphNode n : list) {
            if (n.contains(p)) {
                n.setSelected(!n.isSelected());
            }
        }
    }

    /**
     * Update each node's position by d (delta).
     */
    public static void updatePosition(List<GraphNode> list, Point d) {
        for (GraphNode n : list) {
            if (n.isSelected()) {
                n.p.x += d.x;
                n.p.y += d.y;
                n.setBoundary(n.b);
            }
        }
    }

    /**
     * Update each node's radius r.
     */
    public static void updateRadius(List<GraphNode> list, int r) {
        for (GraphNode n : list) {
            if (n.isSelected()) {
                n.r = r;
                n.setBoundary(n.b);
            }
        }
    }

    /**
     * Update each node's color.
     */
    public static void updateColor(List<GraphNode> list, Color color) {
        for (GraphNode n : list) {
            if (n.isSelected()) {
                n.color = color;
            }
        }
    }
    
    public void addNeighbor(GraphNode neighbor){
    	this.neighbors.add(neighbor);
    }
    
    @Override
	public int compareTo(GraphNode compareNode) {
		if(this.value > compareNode.value){
			return 1;
		}
		else if(this.value == compareNode.value){
			return 0;
		}
		else{
			return -1;
		}
	}
	
}

