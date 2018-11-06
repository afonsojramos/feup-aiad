import sajas.sim.repasts.RepastSLauncher;
import sajas.wrapper.ContainerController;

import java.util.concurrent.ThreadLocalRandom;

import agents.CTAgent;
import agents.GameServer;
import agents.RadarBackground;
import agents.TAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import maps.Map;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.StrictBorders;
import sajas.core.Runtime;

public class SourceLauncher extends RepastSLauncher {
	
	private Grid<Object> grid;
	private ContinuousSpace<Object> space;
	
	@Override
	public String getName() {
		return "AIAD Source";
	}

	@Override
	protected void launchJADE() {
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		ContainerController container = rt.createAgentContainer(p1);
		
		try {
			launchAgents(container);
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
	
	private GridPoint generateSpawnPoint(boolean isTSide) {
		if (isTSide) {
			int x = ThreadLocalRandom.current().nextInt(15, 24), y = ThreadLocalRandom.current().nextInt(3, 7);
			return new GridPoint(x, y);
		} else {
			int x = ThreadLocalRandom.current().nextInt(29, 33), y = ThreadLocalRandom.current().nextInt(38, 42);
			return new GridPoint(x, y);
		}
	}
	
	private void launchAgents(ContainerController container) throws StaleProxyException {
		
		GameServer server = new GameServer();
		container.acceptNewAgent("server", server);
		
		// Generate the two indexes of the team's in-game leaders.
		int tIGLIndex = ThreadLocalRandom.current().nextInt(0, 5);
		int ctIGLIndex = ThreadLocalRandom.current().nextInt(0, 5);
		
		for (int i = 0; i < 5; i++) {
			boolean tIsIGL = false, ctIsIGL = false;
			
			if (i == tIGLIndex) tIsIGL = true;
			if (i == ctIGLIndex) ctIsIGL = true;
			
			CTAgent ct = new CTAgent(this.space, this.grid, ctIsIGL);
			container.acceptNewAgent("CT" + i, ct).start();
			
			GridPoint ctSpawn = generateSpawnPoint(false);
			this.space.moveTo(ct, ctSpawn.getX(), ctSpawn.getY());	// Move to spawn point.
		
			TAgent t = new TAgent(this.space, this.grid, tIsIGL);
			container.acceptNewAgent("T" + i, t).start();
			
			GridPoint tSpawn = generateSpawnPoint(true);
			this.space.moveTo(t, tSpawn.getX(), tSpawn.getY());		// Move to spawn point.
		}
	}
	
	@Override
	public Context build(Context<Object> context) {	
		context.setId("aiad-source");
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		this.space = spaceFactory.createContinuousSpace("space", context, 
				new RandomCartesianAdder<Object>(), new repast.simphony.space.continuous.StrictBorders(), 50, 50);
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		this.grid = gridFactory.createGrid("grid", context, 
				new GridBuilderParameters<Object>(new StrictBorders(),
				new SimpleGridAdder<Object>(), true, 50, 50));
		
		RadarBackground rb = new RadarBackground();
		context.add(rb);
		space.moveTo(rb, 25, 25);

		return super.build(context);
	}
	
}
