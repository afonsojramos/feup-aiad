package agents;

import jade.lang.acl.ACLMessage;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import sajas.core.AID;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.core.behaviours.TickerBehaviour;

public class BombAgent extends Agent {
	
	public enum State {
		CARRIED, PLANTED, DROPPED
	}
	
	private Grid<Object> grid;
	private ContinuousSpace<Object> space;
	private int secondsRemaining;
	private BombAgent context;
	private State state;
	
	public BombAgent(Grid<Object> grid, ContinuousSpace<Object> space) {
		this.grid = grid; this.space = space;
		this.secondsRemaining = 20;
		this.state = State.CARRIED;
	}
	
	@Override
	public void setup() {
		this.context = this;
		this.addBehaviour(new ListeningBehaviour());
	}
	
	@Override
	public void takeDown() {
		System.out.println("Bomb has exploded/been defused!");
	}
	
	public void broadcastCTs(String message) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent(message);
		
		for (int i = 0; i < 5; i++)
			msg.addReceiver(new AID(String.format("CT%d@aiadsource", (i+1)), true));
		
		send(msg);
	}
	
	public void broadcastTs(String message) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent(message);
		
		for (int i = 0; i < 5; i++)
			msg.addReceiver(new AID(String.format("T%d@aiadsource", (i+1)), true));
		
		send(msg);
	}
	
	private class ListeningBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			ACLMessage msg = receive();
			
			if (msg != null) {
				String[] info = msg.getContent().split(" ");
				
				if (info[0].equals("ARM")) {
					context.grid.moveTo(context, Integer.parseInt(info[1]), Integer.parseInt(info[2]));
					context.space.moveTo(context, Integer.parseInt(info[1]), Integer.parseInt(info[2]));
					
					broadcastCTs(String.format("PLANTED %s %s", info[1], info[2]));
					addBehaviour(new ExplosionCountdown(context, 1000));
					state = State.PLANTED;
				}
				
				if (info[0].equals("DEFUSED")) {
					//TODO finished everything with the Defuse
					
					doDelete();
				}
				
				if (info[0].equals("SECUREDBOMB")) {		
					broadcastCTs(String.format("SERVER_OPERATIONAL"));
					broadcastTs(String.format("SERVER_OPERATIONAL"));
					state = State.CARRIED;
				}
				
				if (info[0].equals("DROP")) {
					context.grid.moveTo(context, Integer.parseInt(info[1]), Integer.parseInt(info[2]));
					context.space.moveTo(context, Integer.parseInt(info[1]), Integer.parseInt(info[2]));
					
					state = State.DROPPED;
										
					ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
					msg2.setContent("DROPPED " + info[1] + " " + info[2]);

					for (int i = 1; i <= 5; i++) {
						String receiverAIDCT = String.format("CT%d@aiadsource", i);
						String receiverAIDT = String.format("T%d@aiadsource", i);
						
						msg2.addReceiver(new AID(receiverAIDCT, true));
						msg2.addReceiver(new AID(receiverAIDT, true));
					}
					
					send(msg2);
				}
				
			}
		}
	}
	
	private class ExplosionCountdown extends TickerBehaviour {
		private static final long serialVersionUID = 1L;
		
		public ExplosionCountdown(Agent a, long period) {
			super(a, period);
			state = State.PLANTED;
		}

		@Override
		protected void onTick() {
			secondsRemaining--;
			
			if (secondsRemaining <= 0)
				doDelete();	
			
			System.out.println(String.format("%d until explosion!", secondsRemaining));
		}
		
	}
	
	public State getState() {
		return this.state;
	}
}
