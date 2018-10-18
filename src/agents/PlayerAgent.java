package agents;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class PlayerAgent {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	public PlayerAgent(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space; this.grid = grid;
	}
	
	@ScheduledMethod(start=1, interval=500000)
	public void step() {
		moveTowards(new GridPoint(3, 3));
	}
	
	public void moveTowards(GridPoint pt) {
		if (pt.equals(grid.getLocation(this))) return;
		
		NdPoint posStart = space.getLocation(this);
		NdPoint posEnd = new NdPoint(pt.getX(), pt.getY());
		space.moveByVector(this, 1, SpatialMath.calcAngleFor2DMovement(space, posStart, posEnd), 0);
		
		NdPoint posRes = space.getLocation(this);
		grid.moveTo(this, (int) posRes.getX(), (int) posRes.getY());
	}
}
