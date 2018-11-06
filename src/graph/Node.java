package graph;

public class Node {
    final private String id;
    final private int x, y;
    
    public Node(String id, int x, int y) {
        this.id = id;
        this.x = x; this.y = y;
    }
    
    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
    	final Node that = (Node) obj;
    	
    	if ((this.x == that.x && this.y == that.y) || this.id == that.id)
    		return true;
    	
    	return false;
    		
    	/*
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node other = (Node) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
        */
    }

    @Override
    public String toString() {
        return String.format("%s at x=%d and y=%d", this.id, this.x, this.y);
    }

}