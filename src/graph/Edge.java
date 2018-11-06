package graph;

public class Edge  {
    private final String id;
    private final Node source;
    private final Node destination;
    private final int weight;

    public Edge(String id, Node source, Node destination, int weight) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }
    public Node getDestination() {
        return destination;
    }

    public Node getSource() {
        return source;
    }
    
    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return source + " " + destination;
    }

}