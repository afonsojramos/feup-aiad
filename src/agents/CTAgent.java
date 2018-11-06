package agents;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import jade.lang.acl.ACLMessage;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import sajas.core.AID;

public class CTAgent extends PlayerAgent {	
	public CTAgent(ContinuousSpace<Object> space, Grid<Object> grid, boolean isIGL) {
		super(space, grid, new GridPoint(35, 12), isIGL);
	}
	
	public void informTeammates(GridCell<TAgent> enemy) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("HELP");
		
		for (int i = 0; i < 5; i++) {
			String receiverAID = String.format("CT%d@AIAD Source", (i+1));
			
			if (super.getAID().getName().equals(receiverAID))
					continue;
			
			msg.addReceiver(new AID(receiverAID, true));
		}
		send(msg);
	}
	
	@Override
	public void checkSurroundings() {
		GridPoint pt = grid.getLocation(this);
		GridCellNgh<TAgent> nghCreator = new GridCellNgh<TAgent>(this.grid, pt, TAgent.class, 1, 1);
		List<GridCell<TAgent>> gridCells = nghCreator.getNeighborhood(true);
		
		for (GridCell<TAgent> enemy : gridCells)
			informTeammates(enemy);
	}
}
