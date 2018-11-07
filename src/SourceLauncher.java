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
import repast.simphony.space.continuous.NdPoint;
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
	private Context<Object> context;
	
	@Override
	public String getName() {
		return "aiadsource";
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
	
	private void launchAgents(ContainerController container) throws StaleProxyException {
		GameServer server = GameServer.getInstance();
		container.acceptNewAgent("server", server).start();
		context.add(server);
		
		int iglIndex = ThreadLocalRandom.current().nextInt(0, 5);
		
		for (int i = 0; i < 5; i++) {
			boolean isIGL = false;
			if (iglIndex == i) isIGL = true;
			
			CTAgent ct = new CTAgent(this.space, this.grid, isIGL);
			TAgent t = new TAgent(this.space, this.grid, isIGL);
			
			container.acceptNewAgent("CT" + (i+1), ct).start();
			container.acceptNewAgent("T" + (i+1), t).start();
			context.add(ct); context.add(t);
			
			GridPoint ctSpawnPoint = server.generateSpawnPoint(true), tSpawnPoint = server.generateSpawnPoint(false);
			
			space.moveTo(ct, ctSpawnPoint.getX(), ctSpawnPoint.getY());
			space.moveTo(t, tSpawnPoint.getX(), tSpawnPoint.getY());
		}
		
		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
		}
	}

	@Override
	public Context<?> build(Context<Object> context) {	
		this.context = context;
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
