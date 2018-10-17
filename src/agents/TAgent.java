package agents;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class TAgent {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	public TAgent(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space; this.grid = grid;
	}
}
