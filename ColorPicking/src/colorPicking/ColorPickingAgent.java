package colorPicking;

import java.awt.Color;
import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;

public class ColorPickingAgent {

	private ContinuousSpace<Object> space ;
	private Grid<Object> grid ;
	
	private Color color;
	private String movingMode;

	public ColorPickingAgent(ContinuousSpace<Object> space, Grid<Object> grid, Color color, String movingMode) {
		this.space = space;
		this.grid = grid;
		this.color = color;
		this.movingMode = movingMode;
	}
	
	public Color getColor() {
		return color;
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		switch(movingMode) {
			case "walk":
				walk();
				break;
			case "jump":
				jump();
				break;
		}
		recolor();
	}
	
	public void jump() {
		// get the grid location of this agent
		GridPoint pt = grid.getLocation(this);
		
		// generate new random empty location
		GridPoint pt2;
		do {
			pt2 = new GridPoint(
					RandomHelper.getUniform().nextIntFromTo(0, grid.getDimensions().getWidth() - 1),
					RandomHelper.getUniform().nextIntFromTo(0, grid.getDimensions().getHeight() - 1));
		} while (grid.getObjectAt(pt2.getX(), pt2.getY()) != null);
		
		// jump to new empty position
		NdPoint newPoint = new NdPoint(pt2.getX(), pt2.getY());
		space.moveTo(this, newPoint.getX(), newPoint.getY());
		grid.moveTo(this, (int) newPoint.getX(), (int) newPoint.getY());
	}

	public void walk() {
		// get the grid location of this agent
		GridPoint pt = grid.getLocation(this);
		
		// generate new grid location
		int xMove = RandomHelper.getUniform().nextIntFromTo(0, 2)-1;
		int yMove = RandomHelper.getUniform().nextIntFromTo(0, 2)-1;
		GridPoint pt2 = new GridPoint(pt.getX()+xMove, pt.getY()+yMove);
		
		// only move if position is not taken
		if(grid.getObjectAt(pt2.getX(), pt2.getY()) == null) {
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt2.getX(), pt2.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
			space.moveByVector(this, 1, angle, 0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
		}
	}
	
	public void recolor() {
		// get the grid location of this agent
		GridPoint pt = grid.getLocation(this);

		// use the GridCellNgh class to create GridCells for the surrounding neighborhood
		GridCellNgh<ColorPickingAgent> nghCreator = new GridCellNgh<ColorPickingAgent>(grid, pt, ColorPickingAgent.class , 1, 1);
		List<GridCell<ColorPickingAgent>> gridCells = nghCreator.getNeighborhood(false);
		
		// shuffle grid cells
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());

		// take the color of an agent in a random cell (the first found after shuffling)
		for(GridCell<ColorPickingAgent> cell : gridCells) {
			if (cell.size() > 0) {
				this.color = cell.items().iterator().next().getColor();
				return;
			}
		}
		
	}

}
