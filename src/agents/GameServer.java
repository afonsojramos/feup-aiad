package agents;

import maps.Map;
import sajas.core.Agent;

public class GameServer extends Agent {
	
	private static GameServer instance = null;
	protected Map map;
	
	public GameServer() {
		return;
	}
	
	@Override
	public void setup() {
		this.map = new Map();
		System.out.println("Generated graph!");
	}
	
	@Override
	public void takeDown() {
		System.out.println("Server shutdown.");
	}
	
	public static GameServer getInstance() {
		if (instance == null) instance = new GameServer();
		return instance;
	}
}
