package maps;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import graph.Dijkstra;
import graph.Edge;
import graph.Graph;
import graph.Node;

public class Map {
	
	private List<Node> nodes;
    private List<Edge> edges;
    private Graph graph;
    private Dijkstra dijkstra;
    
    public Map() {
    	nodes = new ArrayList<Node>();
        edges = new ArrayList<Edge>();
        
        try {
        	BufferedReader bufferNodes = new BufferedReader(new FileReader(new File("/home/j-seixas/Documents/eclipse-workspace/aiad-source/src/maps/nodes.txt"))); 
        	String line; 
        	while ((line = bufferNodes.readLine()) != null) {
        		String[] splitLine = line.split(";");
        		nodes.add(new Node(splitLine[0], Integer.parseInt(splitLine[1]), Integer.parseInt(splitLine[2])));
        	} 
        	bufferNodes.close();
        
        	BufferedReader bufferEdges = new BufferedReader(new FileReader(new File("/home/j-seixas/Documents/eclipse-workspace/aiad-source/src/maps/edges.txt"))); 
        	int i = 0;
        	while ((line = bufferEdges.readLine()) != null) {
        		String[] splitLine = line.split(";");
        		edges.add(new Edge("Edge" + i, nodes.get(Integer.parseInt(splitLine[0])), nodes.get(Integer.parseInt(splitLine[1])), 1));
        		i = i + 1;
        		edges.add(new Edge("Edge" + i, nodes.get(Integer.parseInt(splitLine[1])), nodes.get(Integer.parseInt(splitLine[0])), 1));
        		i++;
        	} 
        	bufferEdges.close();
        } catch(IOException e) {
        	e.printStackTrace();
        }

        graph = new Graph(nodes, edges);
        dijkstra = new Dijkstra(graph);
    }
    
	public Dijkstra getDijkstra() {
		return dijkstra;
	}
	
	public Graph getGraph() {
		return graph;
	}
}
