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

public class CTAgent extends Agent {
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	private int health;
	private boolean isIGL;
	protected LinkedList<Node> onCourse;
	
	public CTAgent(ContinuousSpace<Object> space, Grid<Object> grid, boolean isIGL) {
		this.space = space; this.grid = grid; this.isIGL = isIGL;
		this.health = 100;
		this.onCourse = new LinkedList<Node>();
	}
	
	@Override
	public void setup() {
		System.out.println(this.getAID().getName() + " reporting in.");
		
		// TODO: Add behaviours here.
		this.addBehaviour(new ListeningBehaviour());
		this.addBehaviour(new WalkingBehaviour());
	}
	
	@Override
	public void takeDown() {
		System.out.println("I've been killed.");
	}

	private void createNewRoute(Node dstNode) {
		GridPoint srcPoint = grid.getLocation(this);
		Node srcNode = GameServer.getInstance().map.getGraph().getNode(srcPoint);

		GameServer.getInstance().map.getDijkstra().execute(srcNode);
		this.onCourse = GameServer.getInstance().map.getDijkstra().getPath(dstNode);
	}
	
	public void informTeammates(GridCell<TAgent> enemy) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("HELP");
		
		for (int i = 0; i < 5; i++) {
			String receiverAID = String.format("CT%d@AIAD Source", (i+1));
			
			if (this.getAID().getName().equals(receiverAID))
				continue;
			
			msg.addReceiver(new AID(receiverAID, true));
		}
		send(msg);
	}
	
	public void checkSurroundings() {
		GridPoint pt = grid.getLocation(this);
		GridCellNgh<TAgent> nghCreator = new GridCellNgh<TAgent>(this.grid, pt, TAgent.class, 1, 1);
		List<GridCell<TAgent>> gridCells = nghCreator.getNeighborhood(true);
		
		for (GridCell<TAgent> enemy : gridCells) {
			informTeammates(enemy);
		}
	}
	
	public void moveTowards(Node node) {
		// TODO: This doesn't look right, find the difference between space and grid.
		space.moveTo(this, node.getX(), node.getY());
		grid.moveTo(this, node.getX(), node.getY());
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
				moveTowards(onCourse.removeFirst());
		}
	}
	
	private class ListeningBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;
		
		@Override
		public void action() {
			ACLMessage msg = receive();
			
			if (msg != null) {
				if (msg.getContent().equals("SERVER_OPERATIONAL")) {

					// TODO: Delete this test code.
					Node test = GameServer.getInstance().map.getGraph().getNode(new GridPoint(25, 25));
					createNewRoute(test);
				}
			} else {
				block();
			}
		}
	}

}
