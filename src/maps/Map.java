package maps;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import graph.Dijkstra;
import graph.Edge;
import graph.Graph;
import graph.Vertex;

public class Map {
	
	private List<Vertex> nodes;
    private List<Edge> edges;
    private Graph graph;
    private Dijkstra dijkstra;
    
    public Map() {
    	nodes = new ArrayList<Vertex>();
        edges = new ArrayList<Edge>();
        for (int y = 0; y < 50; y++) {
            for (int x = 0; x < 50; x++) {
            	Vertex location = new Vertex("Node_" + x + "_" + y, "Node_" + x + "_" + y);
            	nodes.add(location); 	
            }
        }
        
        // Temporary edge creation
        // txt creation suggestion, example: 20 12 0 0 1 0 (x y top right bottom left)
        for (int i = 1; i < 50; i++) {
            for (int j = 1; j < 50; j++) {
        		Edge top    = new Edge("Edge_" + i + "_" + j + "<->" + (i-1) + "_" + j, nodes.get((i-1)*j), nodes.get((i-1)*j), 1 );
        		Edge bottom = new Edge("Edge_" + i + "_" + j + "<->" + (i+1) + "_" + j, nodes.get((i+1)*j), nodes.get((i+1)*j), 1 );
        		Edge left   = new Edge("Edge_" + i + "_" + j + "<->" + i + "_" + (j-1), nodes.get((j-1)*i), nodes.get((j-1)*i), 1 );
        		Edge right  = new Edge("Edge_" + i + "_" + j + "<->" + i + "_" + (j+1), nodes.get((j+1)*i), nodes.get((j+1)*i), 1 );
        		edges.add(top); edges.add(bottom); edges.add(left); edges.add(right);            
            }
        }

        graph = new Graph(nodes, edges);
        dijkstra = new Dijkstra(graph);
        /*
        dijkstra.execute(nodes.get(0));
        LinkedList<Vertex> path = dijkstra.getPath(nodes.get(10));
        */
    }
    
	public Dijkstra getDijkstra() {
		return dijkstra;
	}
	
	public Graph getGraph() {
		return graph;
	}
}
