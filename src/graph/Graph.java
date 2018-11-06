package graph;

import java.util.List;

import graph.Edge;
import graph.Node;
import repast.simphony.space.grid.GridPoint;

public class Graph {
    private final List<Node> nodes;
    private final List<Edge> edges;

    public Graph(List<Node> nodes, List<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }
    
    public Node getNode(GridPoint pt) {
    	for (Node node : this.nodes) 
    		if (pt.getX() == node.getX() && pt.getY() == node.getY()) 
    			return node;
    	return null;
    }
    
}