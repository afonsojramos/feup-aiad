package agents;

import java.util.ArrayList;
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
import sajas.core.behaviours.WakerBehaviour;
import utils.Calls;
import utils.Calls.Positions;

public class TAgent extends Agent {
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	private int health;
	private boolean isIGL, hasBomb;
	protected LinkedList<Node> onCourse;
	
	public TAgent(ContinuousSpace<Object> space, Grid<Object> grid, boolean isIGL, boolean hasBomb) {
		this.space = space; this.grid = grid; this.isIGL = isIGL; this.hasBomb = hasBomb;
		this.health = 100;
		this.onCourse = new LinkedList<Node>();
	}
	
	@Override
	public void setup() {
		System.out.println(this.getAID().getName() + " reporting in.");
		
		addBehaviour(new WalkingBehaviour(this, 200));
		addBehaviour(new AliveBehaviour());
		addBehaviour(new ListeningBehaviour());
	}
	
	@Override
	public void takeDown() {
		
	}

	private void createNewRoute(Node dstNode) {
		GridPoint srcPoint = this.grid.getLocation(this);
		Node srcNode = GameServer.getInstance().map.getGraph().getNode(srcPoint);

		GameServer.getInstance().map.getDijkstra().execute(srcNode);
		this.onCourse = GameServer.getInstance().map.getDijkstra().getPath(dstNode);
	}
	
	public void informTeammates(CTAgent enemy, GridPoint pt) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent(String.format("HELP ENEMY %s AT X=%d AND Y=%d", enemy.getAID(), pt.getX(), pt.getY()));
		
		for (int i = 0; i < 5; i++) {
			String receiverAID = String.format("T%d@aiadsource", (i+1));
			
			if (this.getAID().getName().equals(receiverAID))
				continue;
			
			msg.addReceiver(new AID(receiverAID, true));
		}
		send(msg);
	}
	
	public void shootEnemy(CTAgent enemy) {
		int damage = GameServer.getInstance().rollDamageOutput();
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent(String.format("SHOT %s %d", enemy.getAID().getName(), damage));
		
		System.out.println("i, " + getAID().getName() + " shot " + enemy.getAID().getName());
		
		msg.addReceiver(new AID("server@aiadsource", true));
		send(msg);
	}
	
	public void checkSurroundings() {
		GridPoint pt = grid.getLocation(this);
		GridCellNgh<CTAgent> nghCreator = new GridCellNgh<CTAgent>(this.grid, pt, CTAgent.class, 1, 1);
		List<GridCell<CTAgent>> gridCells = nghCreator.getNeighborhood(true);
		
		boolean alreadyShotOnThisTick = false;
		
		for (GridCell<CTAgent> enemy : gridCells) {
			Iterator<CTAgent> it = enemy.items().iterator();
			
			while (it.hasNext()) {
				CTAgent ct = it.next();
				this.informTeammates(ct, enemy.getPoint());
				
				if (!alreadyShotOnThisTick) {
					shootEnemy(ct); alreadyShotOnThisTick = !alreadyShotOnThisTick;
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
	
	public void informDroppedBomb() {
		GridPoint gp = this.grid.getLocation(this);
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent(String.format("DROP %d %d", gp.getX(), gp.getY()));
		msg.addReceiver(new AID("server@aiadsource", true));
		send(msg);
	}
	
	public void nominateNewIGL() {
		ArrayList<String> aliveAgents = GameServer.getInstance().getAliveAgents();
		String newIGL = null;
		
		for (String agent : aliveAgents) {
			if (agent.matches("\\w{1}\\d")) {
				newIGL = agent;
				break;
			}
		}
		
		if (newIGL == null)
			return;
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("IGL");
		msg.addReceiver(new AID(String.format("%s@aiadsource", newIGL), true));
		send(msg);
	}
	
	public void attemptPlantBomb() {
		GridPoint myLocal = this.grid.getLocation(this); 
		GridPoint bsAPoint = Calls.getBombsiteLocation(Positions.A_SITE), bsBPoint = Calls.getBombsiteLocation(Positions.B_SITE);
		
		if ((myLocal.getX() == bsAPoint.getX() && myLocal.getY() == bsAPoint.getY()) || (myLocal.getX() == bsBPoint.getX() && myLocal.getY() == bsBPoint.getY())) {
			System.out.println("Planting bomb... 3 seconds remaining.");
			addBehaviour(new BombPlantBehaviour(this, 3000, myLocal.getX(), myLocal.getY()));
			
			flipBombState();
		}
	}
	
	private class AliveBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			if (health <= 0) {
				//System.out.println("I've been killed " + getAID().getName());
				warnServerOfDeath();
				
				if (hasBomb)
					
					
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
			
			if (getHasBomb())
				attemptPlantBomb();
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
				
				if (info[0].equals("IGL"))
					isIGL = true;
				
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
	
	private class BombPlantBehaviour extends WakerBehaviour {
		private static final long serialVersionUID = 1L;
		
		private int x, y;

		public BombPlantBehaviour(Agent a, long timeout, int x, int y) {
			super(a, timeout);
			this.x = x; this.y = y;
		}
		
		@Override
		public void onWake() {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setContent(String.format("ARM %d %d", this.x, this.y));
			msg.addReceiver(new AID("bomb@aiadsource", true));
			send(msg);
			
			System.out.println("Bomb has been planted!");
			stop();
		}
		
	}
	
	public boolean getHasBomb() {
		return this.hasBomb;
	}
	
	public boolean getIsIGL() {
		return this.isIGL;
	}
	
	public void flipBombState() {
		this.hasBomb ^= this.hasBomb;
	}
	
}
