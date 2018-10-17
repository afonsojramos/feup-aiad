package agents;

import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class CTAgent {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private boolean hasMoved;
	
	public CTAgent(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space; this.grid = grid;
		this.hasMoved = false;
	}
	
	@ScheduledMethod(start=1, interval=500000)
	public void step() {
		GridPoint pt = this.grid.getLocation(this);
		/*
		GridCellNgh<TAgent> nghCreator = new GridCellNgh<TAgent>(this.grid, pt, TAgent.class, 1, 1);
		List<GridCell<TAgent>> enemies = nghCreator.getNeighborhood(true);
		
		for (GridCell<TAgent> enemy : enemies) {
			System.out.println("Enemy spotted!");
			moveTowards(enemy.getPoint());
		}*/
		
		moveTowards(new GridPoint(3, 3));
	}
	
	public void moveTowards(GridPoint pt) {
		if (pt.equals(grid.getLocation(this))) return;
		
		NdPoint posStart = space.getLocation(this);
		NdPoint posEnd = new NdPoint(pt.getX(), pt.getY());
		space.moveByVector(this, 1, SpatialMath.calcAngleFor2DMovement(space, posStart, posEnd), 0);
		
		NdPoint posRes = space.getLocation(this);
		grid.moveTo(this, (int) posRes.getX(), (int) posRes.getY());
		
		this.hasMoved = true;
	}
	
	
	
}
