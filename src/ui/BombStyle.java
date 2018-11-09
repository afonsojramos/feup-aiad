package ui;

import java.io.IOException;

import agents.BombAgent;
import agents.BombAgent.State;
import agents.CTAgent;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VSpatial;

public class BombStyle extends DefaultStyleOGL2D {
	
	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
		BombAgent bombAgent = ((BombAgent) agent);
		String logoPath = "src/assets/bomb.png";
		
		if (bombAgent.getState().equals(State.CARRIED))
			logoPath = "src/assets/transparent.png";
		
		try {
			spatial = shapeFactory.createImage(logoPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return spatial;
	}
	
	@Override
	public float getScale(Object object) {
		return 0.2f;
	}
}
