package agents;

import java.util.Iterator;
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
import sajas.core.behaviours.TickerBehaviour;

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
		
		addBehaviour(new WalkingBehaviour(this, 1000));
		addBehaviour(new AliveBehaviour());
		addBehaviour(new ListeningBehaviour());
	}
	
	@Override
	public void takeDown() {
		
	}

	private void createNewRoute(Node dstNode) {
		GridPoint srcPoint = grid.getLocation(this);
		Node srcNode = GameServer.getInstance().map.getGraph().getNode(srcPoint);

		GameServer.getInstance().map.getDijkstra().execute(srcNode);
		this.onCourse = GameServer.getInstance().map.getDijkstra().getPath(dstNode);
	}
	
	public void informTeammates(TAgent enemy, GridPoint pt) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent(String.format("HELP ENEMY %s AT X=%d AND Y=%d", enemy.getAID(), pt.getX(), pt.getY()));
		
		for (int i = 0; i < 5; i++) {
			String receiverAID = String.format("CT%d@aiadsource", (i+1));
			
			if (this.getAID().getName().equals(receiverAID))
				continue;
			
			msg.addReceiver(new AID(receiverAID, true));
		}
		send(msg);
	}
	
	public void shootEnemy(TAgent enemy) {
		int damage = GameServer.getInstance().rollDamageOutput();
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent(String.format("SHOT %s %d", enemy.getAID().getName(), damage));
		System.out.println("i, " + getAID().getName() + " shot " + enemy.getAID().getName());
		
		msg.addReceiver(new AID("server@aiadsource", true));
		send(msg);
	}
	
	public void checkSurroundings() {
		GridPoint pt = grid.getLocation(this);
		GridCellNgh<TAgent> nghCreator = new GridCellNgh<TAgent>(this.grid, pt, TAgent.class, 1, 1);
		List<GridCell<TAgent>> gridCells = nghCreator.getNeighborhood(true);
		
		boolean alreadyShotOnThisTick = false;
		
		for (GridCell<TAgent> enemy : gridCells) {
			Iterator<TAgent> it = enemy.items().iterator();
			
			while (it.hasNext()) {
				TAgent t = it.next();
				this.informTeammates(t, enemy.getPoint());
				
				if (!alreadyShotOnThisTick) {
					shootEnemy(t); alreadyShotOnThisTick = !alreadyShotOnThisTick;
				}
			}
		}
	}
	
	public void moveTowards(Node node) {
		// TODO: This doesn't look right, find the difference between space and grid.
		space.moveTo(this, node.getX(), node.getY());
		grid.moveTo(this, node.getX(), node.getY());
	}
	
	public void warnServerOfDeath() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("DEAD");
		msg.addReceiver(new AID("server@aiadsource", true));
		send(msg);
	}
	
	private class AliveBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			if (health <= 0) {
				//System.out.println("I've been killed " + getAID().getName());
				warnServerOfDeath();
				moveTowards(new Node("cemetery", 0, 0));
				doDelete();
			}
		}
	}
	
	private class WalkingBehaviour extends TickerBehaviour {
		private static final long serialVersionUID = 1L;
		
		public WalkingBehaviour(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			checkSurroundings();
			
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
				String[] info = msg.getContent().split(" ");
				
				if (info[0].equals("SHOT")) {
					System.out.println("i, " + getAID().getName() + " got tagged by " + info[1]);
					health -= Integer.parseInt(info[1]);
				}
				
				if (info[0].equals("DEAD"))
					System.out.println(String.format("DEAD: %s", info[1]));
				
				if (info[0].equals("SERVER_OPERATIONAL")) {
					// TODO: Delete this test code.
					Node test = GameServer.getInstance().map.getGraph().getNode(new GridPoint(10, 42));
					createNewRoute(test);
				}
			} else {
				block();
			}
		}
	}
	
	public boolean getIsIGL() {
		return this.isIGL;
	}

}
