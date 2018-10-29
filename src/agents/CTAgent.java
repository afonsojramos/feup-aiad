package agents;

import java.util.concurrent.ThreadLocalRandom;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class CTAgent extends PlayerAgent {	
	public CTAgent(ContinuousSpace<Object> space, Grid<Object> grid, boolean isIGL) {
		super(space, grid, new GridPoint(35, 12), isIGL);
	}
	
	@Override
	public void checkSurroundings() {
	}
}
