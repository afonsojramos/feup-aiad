package agents;

import jade.lang.acl.ACLMessage;
import jade.wrapper.StaleProxyException;
import maps.Map;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import sajas.core.AID;
import sajas.core.Agent;
import sajas.wrapper.ContainerController;
import java.util.concurrent.ThreadLocalRandom;

public class GameServer extends Agent {
	
	private static GameServer instance = null;
	private ContinuousSpace<Object> space;
	public Grid<Object> grid;
	protected Map map;
	
	private int MIN_DMG = 20, MAX_DMG = 33, CRIT_DMG = 80, CRIT_CHANCE = 33;
	
	private GameServer() {
	}
	
	@Override
	public void setup() {
		this.map = new Map();
		System.out.println("Generated graph!");
		this.informIsOperational();
	}
	
	@Override
	public void takeDown() {
		System.out.println("Server shutdown.");
	}
	
	public void informIsOperational() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("SERVER_OPERATIONAL");
		
		for (int i = 1; i <= 5; i++) {
			msg.addReceiver(new AID(String.format("CT%d@AIAD Source", i), true));
			msg.addReceiver(new AID(String.format("T%d@AIAD Source", i), true));
		}
		
		send(msg);
	}
	
	public static GameServer getInstance() {
		if (instance == null) instance = new GameServer();
		return instance;
	}

	public int rollDamageOutput() {
		if (ThreadLocalRandom.current().nextInt(100) < CRIT_CHANCE)
			return CRIT_DMG;
		
		return ThreadLocalRandom.current().nextInt(MIN_DMG, MAX_DMG + 1);
	}
	
	public GridPoint generateSpawnPoint(boolean isCTSide) {
		if (isCTSide)
			return new GridPoint(ThreadLocalRandom.current().nextInt(25, 31), 38);
		
		return new GridPoint(ThreadLocalRandom.current().nextInt(14, 21), 5);
	}
	
}
