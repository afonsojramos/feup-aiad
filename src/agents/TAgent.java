package agents;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import graph.Node;
import jade.lang.acl.ACLMessage;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import sajas.core.AID;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;

public class TAgent extends Agent {
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	private int health;
	private boolean isIGL;
	protected LinkedList<Node> onCourse;
	
	public TAgent(ContinuousSpace<Object> space, Grid<Object> grid, boolean isIGL) {
		this.space = space; this.grid = grid; this.isIGL = isIGL;
		this.health = 100;
		this.onCourse = new LinkedList<Node>();
	}
	
	@Override
	public void setup() {
		System.out.println("Reporting in.");
		// TODO: Add behaviours here.
	}
	
	@Override
	public void takeDown() {
		System.out.println("I've been killed.");
	}

	private void createNewRoute(Node dstNode) {
		GridPoint srcPoint = this.grid.getLocation(this);
		Node srcNode = GameServer.getInstance().map.getGraph().getNode(srcPoint);

		GameServer.getInstance().map.getDijkstra().execute(srcNode);
		this.onCourse = GameServer.getInstance().map.getDijkstra().getPath(dstNode);
	}
	
	public void informTeammates(GridCell<CTAgent> enemy) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("HELP");
		
		for (int i = 0; i < 5; i++) {
			String receiverAID = String.format("T%d@AIAD Source", (i+1));
			
			if (this.getAID().getName().equals(receiverAID))
				continue;
			
			msg.addReceiver(new AID(receiverAID, true));
		}
		send(msg);
	}
	
	public void checkSurroundings() {
		GridPoint pt = grid.getLocation(this);
		GridCellNgh<CTAgent> nghCreator = new GridCellNgh<CTAgent>(this.grid, pt, CTAgent.class, 1, 1);
		List<GridCell<CTAgent>> gridCells = nghCreator.getNeighborhood(true);
		
		for (GridCell<CTAgent> enemy : gridCells) {
			informTeammates(enemy);
		}
	}
	
	private class AliveBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			if (health <= 0) takeDown();
		}
	}
	
	private class WalkingBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			// TODO: Check surroundings here.
			int damage = GameServer.getInstance().rollDamageOutput();
			
			if (!onCourse.isEmpty())
				System.out.println(onCourse.removeFirst());
		}
	}
	
	private class ListeningBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;
		
		@Override
		public void action() {
			ACLMessage message = receive();
			
			if (message != null) {
				
			} else {
				block();
			}
		}
	}

}
