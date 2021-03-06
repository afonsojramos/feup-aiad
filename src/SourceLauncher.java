import sajas.sim.repasts.RepastSLauncher;
import sajas.wrapper.ContainerController;

import java.io.FileNotFoundException;
import java.util.concurrent.ThreadLocalRandom;

import agents.BombAgent;
import agents.CTAgent;
import agents.GameServer;
import agents.RadarBackground;
import agents.TAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
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
		} catch (StaleProxyException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void launchAgents(ContainerController container) throws StaleProxyException, FileNotFoundException {
		GameServer server = GameServer.newInstance();
		
		//Set the parameters of execution
		Parameters params = RunEnvironment.getInstance().getParameters();
		server.setCTHealth((int) params.getValue("CT_Health"));
		server.setTHealth((int) params.getValue("T_Health"));
		server.setMinDmg((int) params.getValue("Min_Dmg"));
		server.setMaxDmg((int) params.getValue("Max_Dmg"));
		server.setCritDmg((int) params.getValue("Crit_Dmg"));
		server.setCritChance((int) params.getValue("Crit_Chance"));
		server.setFirstStrat((String) params.getValue("First_Strat"));
		
		RunEnvironment.getInstance().endAt(50000000);
		
		container.acceptNewAgent("server", server).start();
		context.add(server);

		int iglIndex = ThreadLocalRandom.current().nextInt(0, 5);
		
		for (int i = 0; i < 5; i++) {
			boolean isIGL = false, hasBomb = false;
			
			if (iglIndex == i) isIGL = true;
			if (i == 0) hasBomb = true; 
			
			CTAgent ct = new CTAgent(this.space, this.grid, isIGL);
			TAgent t = new TAgent(this.space, this.grid, isIGL, hasBomb);
			
			container.acceptNewAgent("CT" + (i+1), ct).start();
			container.acceptNewAgent("T" + (i+1), t).start();
			context.add(ct); context.add(t);
			
			GridPoint ctSpawnPoint = server.generateSpawnPoint(true), tSpawnPoint = server.generateSpawnPoint(false);
			
			space.moveTo(ct, ctSpawnPoint.getX(), ctSpawnPoint.getY());
			space.moveTo(t, tSpawnPoint.getX(), tSpawnPoint.getY());
		}
		
		BombAgent bomb = new BombAgent(this.grid, this.space);
		container.acceptNewAgent("bomb", bomb).start();
		context.add(bomb);
		
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
