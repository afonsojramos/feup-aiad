import sajas.sim.repasts.RepastSLauncher;
import sajas.wrapper.ContainerController;
import agents.CTAgent;
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
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.StrictBorders;
import sajas.core.Runtime;

public class SourceLauncher extends RepastSLauncher {

	@Override
	public String getName() {
		return "AIAD Source";
	}

	@Override
	protected void launchJADE() {
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		ContainerController mainContainer = rt.createAgentContainer(p1);
		
		try {
			launchAgents(mainContainer);
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
	
	private void launchAgents(ContainerController container) throws StaleProxyException {
		CTAgent agent = new CTAgent();
		container.acceptNewAgent("Bot Wolf", agent).start();
	}
	
	@Override
	public Context build(Context<Object> context) {	
		context.setId("aiad-source");
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context, 
				new SimpleCartesianAdder<Object>(), new repast.simphony.space.continuous.StrictBorders(), 50, 50);
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context, 
				new GridBuilderParameters<Object>(new StrictBorders(),
				new SimpleGridAdder<Object>(), true, 50, 50));
		
		RadarBackground rb = new RadarBackground();
		context.add(rb);
		space.moveTo(rb, 25, 25);
		
		for (int i = 0; i < 5; i++) {
			context.add(new CTAgent(space, grid));
			context.add(new TAgent(space, grid));
		}

		return super.build(context);
	}
	
}
