package agents;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
import sajas.core.behaviours.SimpleBehaviour;
import utils.Calls;
import utils.Calls.Callouts;
import utils.Calls.Positions;
import sajas.core.behaviours.TickerBehaviour;
import sajas.core.behaviours.WakerBehaviour;

public class CTAgent extends Agent {
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	private int health;
	private GameServer gameServer;
	private boolean isIGL, canAdvance, isBombPlanted;
	protected LinkedList<Node> onCourse;
	protected CTAgent instance;
	protected Node bombNode;
	
	public CTAgent(ContinuousSpace<Object> space, Grid<Object> grid, boolean isIGL) throws FileNotFoundException {
		this.gameServer = GameServer.getInstance();
		this.space = space; this.grid = grid; this.isIGL = isIGL;
		this.health = gameServer.CT_HEALTH;;
		this.onCourse = new LinkedList<Node>();
		this.bombNode = null;
		this.instance = this;
		this.canAdvance = true;
		this.isBombPlanted = false;
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
		GridPoint srcPoint = grid.getLocation(this);
		Node srcNode = gameServer.map.getGraph().getNode(srcPoint);

		gameServer.map.getDijkstra().execute(srcNode);
		this.onCourse = gameServer.map.getDijkstra().getPath(dstNode);
		
		if(this.onCourse == null) {  // If Dijkstra returns null (in the case we already are in the destiny position) add the destiny node to the LinkedList
			this.onCourse = new LinkedList<Node>();
			this.onCourse.add(dstNode);
		}
	}
	
	private void createNewRoute(ArrayList<Node> nodes) {
		GridPoint srcPoint = grid.getLocation(this);
		Node srcNode = gameServer.map.getGraph().getNode(srcPoint);
		gameServer.map.getDijkstra().execute(srcNode);
			
		this.onCourse = gameServer.map.getDijkstra().getPath(nodes.get(0));
		for(int i = 1; i < nodes.size(); i++) {
			srcNode = nodes.get(i-1);
			gameServer.map.getDijkstra().execute(srcNode);
			this.onCourse.addAll(gameServer.map.getDijkstra().getPath(nodes.get(i)));
		}

	}
	
	public void shootEnemy(TAgent enemy) {
		int damage = gameServer.rollDamageOutput();
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent(String.format("SHOT %s %d", enemy.getAID().getName(), damage));
		System.out.println("INFO: " + getAID().getName() + " shot " + enemy.getAID().getName());
		
		msg.addReceiver(new AID("server@aiadsource", true));
		send(msg);
	}
	
	public void checkSurroundings() {
		this.canAdvance = true;
		GridPoint pt = grid.getLocation(this);
		GridCellNgh<TAgent> nghCreator = new GridCellNgh<TAgent>(this.grid, pt, TAgent.class, 1, 1);
		List<GridCell<TAgent>> gridCells = nghCreator.getNeighborhood(true);
		
		boolean alreadyShotOnThisTick = false;
		
		for (GridCell<TAgent> enemy : gridCells) {
			Iterator<TAgent> it = enemy.items().iterator();
			
			while (it.hasNext()) {
				TAgent t = it.next();
				
				if (!alreadyShotOnThisTick) {
					shootEnemy(t); alreadyShotOnThisTick = !alreadyShotOnThisTick;
				}
				this.canAdvance = false;
			}		
		}
	}
	
	public void moveTowards(Node node) {
		// TODO: This doesn't look right, find the difference between space and grid.
		space.moveTo(this, node.getX(), node.getY());
		grid.moveTo(this, node.getX(), node.getY());
	}
	
	private void playCallout(Callouts callout, int id) {
		Calls<CTAgent> calls= new Calls<CTAgent>();
		Positions[] pos = calls.getCallouts(callout);
		ArrayList<GridPoint> callPos = calls.getPosition(pos[id-1], instance);
		
		ArrayList<Node> nodes = new ArrayList<Node>();
		for(GridPoint temp : callPos) {
			nodes.add(gameServer.map.getGraph().getNode(temp));
		}
		createNewRoute(nodes);
	}
	
	public void warnServerOfDeath() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("DEAD");
		msg.addReceiver(new AID("server@aiadsource", true));
		send(msg);
		
		System.out.println(String.format("DEAD: %s", this.getAID().getName()));
	}
	
	public void nominateNewIGL() {
		ArrayList<String> aliveAgents = gameServer.getAliveAgents();
		String newIGL = null;
		
		for (String agent : aliveAgents) {
			if (agent.matches("\\w{2}\\d")) {
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
	
	private class AliveBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			if (health <= 0) {
				//System.out.println("I've been killed " + getAID().getName());
				warnServerOfDeath();
				if (isIGL)
					nominateNewIGL();
				
				moveTowards(new Node("cemetery", 0, 0));
				doDelete();
			}
		}
	}
	
	private class WalkingBehaviour extends TickerBehaviour {
		private static final long serialVersionUID = 1L;
		
		private boolean bombIsAlreadyBeingDefused = false;
		
		public WalkingBehaviour(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			checkSurroundings();
			
			if (health > 0 && onCourse != null && !onCourse.isEmpty() && canAdvance)
				moveTowards(onCourse.removeFirst());
			
			if (this.bombIsAlreadyBeingDefused || bombNode == null)
				return;
			
			GridPoint myLocale = grid.getLocation(instance);
			if (myLocale.getX() == bombNode.getX() && myLocale.getY() == bombNode.getY() && isBombPlanted) {
				addBehaviour(new DefuseBehaviour(instance, 5000));
				this.bombIsAlreadyBeingDefused = true;
			}
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
					System.out.println("INFO: " + getAID().getName() + " got tagged by " + info[1]);
					health -= Integer.parseInt(info[1]);
				}
				
				/*if (info[0].equals("DEAD"))
					System.out.println(String.format("DEAD: %s", info[1]));*/
				
				if (info[0].equals("IGL"))
					isIGL = true;
				
				if (info[0].equals("STRAT")) 
					playCallout(Callouts.valueOf(info[1]), Integer.parseInt(info[2]));
					
				if (info[0].equals("DROPPED")) {
					
					if (health > 0) {
						GridPoint dest = new GridPoint(Integer.parseInt(info[1]), Integer.parseInt(info[2]));
						bombNode = gameServer.map.getGraph().getNode(dest);
						System.out.println(getAID().getName() + " going to protect Bomb!");
						createNewRoute(bombNode);		
					}
				}
				
				if (info[0].equals("PLANTED")) {
					isBombPlanted = true;
					// TODO: Add bomb state to GameServer.
					GridPoint pt = new GridPoint(Integer.parseInt(info[1]), Integer.parseInt(info[2]));
					bombNode = gameServer.map.getGraph().getNode(pt);
					
					//onCourse.clear();
					createNewRoute(bombNode);
				}
				
				if (info[0].equals("SERVER_OPERATIONAL")) {
					if(isIGL) 
						addBehaviour(new DelegateBehaviour());
					
					bombNode = null;						
				}
			} else {
				block();
			}
		}
	}
	
	private class DelegateBehaviour extends SimpleBehaviour {

		private static final long serialVersionUID = 1L;

		@Override
		public void action() {						
			
			for (int i = 1; i <= 5; i++) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setContent("STRAT DEFAULT " + i);
				String receiverAID = String.format("CT%d@aiadsource", i);
				
				msg.addReceiver(new AID(receiverAID, true));

				send(msg);
			}
			
			
		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return true;
		}
	}
	
	private class DefuseBehaviour extends WakerBehaviour {
		private static final long serialVersionUID = 1L;
		
		public DefuseBehaviour(Agent a, long timeout) {
			super(a, timeout);
		}
		
		@Override
		public void onWake() {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setContent("DEFUSED");
			msg.addReceiver(new AID("bomb@aiadsource", true));
			send(msg);
			System.out.println("Bomb has been defused!");
			stop();
		}
		
	}
		
	public boolean getIsIGL() {
		return this.isIGL;
	}

}
