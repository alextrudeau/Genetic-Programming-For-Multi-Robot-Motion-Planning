import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

/**
     * An Edge is a pair of Nodes.
     */
    public class Edge {

        public GraphNode n1;
        public GraphNode n2;
        public int weight;

        public Edge(GraphNode n1, GraphNode n2) {
            this.n1 = n1;
            this.n2 = n2;
        }
        
        public Edge(GraphNode n1, GraphNode n2, int weight) {
            this.n1 = n1;
            this.n2 = n2;
            this.weight = weight;
        }

        public void draw(Graphics g) {
            Point p1 = n1.getLocation();
            Point p2 = n2.getLocation();
            g.setColor(Color.darkGray);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
            g.setColor(Color.BLACK);
            g.drawString("" + this.weight, (p1.x + p2.x) / 2 , ((p1.y + p2.y) / 2) - 5);
        }
    }