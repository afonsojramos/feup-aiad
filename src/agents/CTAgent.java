package agents;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class CTAgent {
    private int hp;

    private ContinuousSpace<Object> space;
    private Grid<Object> grid;

    public CTAgent(ContinuousSpace<Object> space, Grid<Object> grid) {
        this.space = space; 
        this.grid = grid;
    }

    public void walkTowards(GridPoint pt) {
        if (pt.equals(grid.getLocation(this))) return;
    }

    public void defuse() {
        GridPoint pt = grid.getLocation(this);  // Gets grid location of counter-terrorist.

        // TODO: Check if bomb 'agent' (?) is in this position.
    }

}