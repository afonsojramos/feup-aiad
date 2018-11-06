package agents;

import jade.wrapper.StaleProxyException;
import maps.Map;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import sajas.core.Agent;
import sajas.wrapper.ContainerController;
import java.util.concurrent.ThreadLocalRandom;

public class GameServer extends Agent {
	
	private static GameServer instance = null;
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	protected Map map;
	
	public GameServer(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		instance = this;
	}
	
	public static GameServer getInstance() {
		if (instance == null) instance = new GameServer(null, null);
		return instance;
	}
	
	public GridPoint generateSpawnPoint(boolean isCTSide) {
		if (isCTSide)
			return new GridPoint(ThreadLocalRandom.current().nextInt(1, 1), 1);
		
		return new GridPoint(ThreadLocalRandom.current().nextInt(1, 1), 1);
	}

	public void launchAgents(ContainerController container) throws StaleProxyException {
		int iglIndex = ThreadLocalRandom.current().nextInt(0, 5);
		
		for (int i = 0; i < 5; i++) {
			boolean isIGL = false;
			if (iglIndex == i) isIGL = true;
			
			container.acceptNewAgent("CT" + (i+1), new CTAgent(space, grid, isIGL)).start();
			container.acceptNewAgent("T" + (i+1), new TAgent(space, grid, isIGL)).start();
		}
	}
	
	@Override
	public void setup() {
		this.map = new Map();
		System.out.println("Generated graph!");
		
		try {
			this.launchAgents(this.getContainerController());
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void takeDown() {
		System.out.println("Server shutdown.");
	}
}
