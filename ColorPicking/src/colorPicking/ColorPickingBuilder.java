package colorPicking;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Hashtable;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.util.collections.IndexedIterable;

public class ColorPickingBuilder implements ContextBuilder<Object> {

	private Context<Object> theContext;
	private Hashtable<Color, Integer> agentColors;

	@Override
	public Context build(Context<Object> context) {
		context.setId("ColorPicking");
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context,
				new RandomCartesianAdder<Object>(), new repast.simphony.space.continuous.WrapAroundBorders(), 100, 100);
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(), new SimpleGridAdder<Object>(), true, 100, 100));

		Parameters params = RunEnvironment.getInstance().getParameters();
		int agentCount = (Integer) params.getValue("NumberOfAgents");
		String movingMode = (String) params.getValue("MovingMode");
		for(int i = 0; i < agentCount; i++) {
			Color color =  new Color(RandomHelper.getUniform().nextIntFromTo(0,255), RandomHelper.getUniform().nextIntFromTo(0,255), RandomHelper.getUniform().nextIntFromTo(0,255));
			context.add(new ColorPickingAgent(space, grid, color, movingMode));
		}

		for(Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
		}
		
		// save a reference to the context
		theContext = context;
		
		// add this object to the context (needed to use methods in data sources)
		context.add(this);
		
		return context;
	}
	
	public int numberOfColors() {
		// prepare agent colors hashtable
		agentColors = new Hashtable<Color,Integer>();

		IndexedIterable<Object> ags = theContext.getObjects(ColorPickingAgent.class);
		
		for(Object a : ags) {
			Color c = ((ColorPickingAgent) a).getColor();
			
			int nAgentsWithColor = (agentColors.get(c) == null ? 1 : agentColors.get(c)+1); 
			agentColors.put(c, nAgentsWithColor);
		}
		
		return agentColors.size();
	}

	public int topColor() {
		int n = 0;
		Enumeration<Integer> agentsPerColor = agentColors.elements();
		while(agentsPerColor.hasMoreElements()) {
			int c = agentsPerColor.nextElement();
			if(c>n) n=c;
		}
		return n;
	}

}
