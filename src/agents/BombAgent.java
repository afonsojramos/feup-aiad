package agents;

import jade.lang.acl.ACLMessage;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
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
		this.secondsRemaining = 5;
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
					
					addBehaviour(new ExplosionCountdown(context, 1000));
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
