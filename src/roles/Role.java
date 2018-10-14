import repast.simphony.space.grid.GridPoint;

public class Role {
    private boolean isIGL;
    private GridPoint position;

    public Role(boolean isIGL, String position) {
        if (position.equals("BOMBSITE_A")) 
            this.position = new GridPoint(5, 5);
    }
}