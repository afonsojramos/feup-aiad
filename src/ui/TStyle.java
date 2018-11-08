package ui;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import agents.CTAgent;
import agents.TAgent;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.Position;
import saf.v3d.scene.VSpatial;

public class TStyle extends DefaultStyleOGL2D {
	
	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
		TAgent tAgent = ((TAgent) agent);
		String logoPath = "src/assets/t-icon.png";
		
		if (tAgent.getIsIGL())
			logoPath = "src/assets/t-icon-igl.png";
		
		if (tAgent.getHasBomb())
			logoPath = "src/assets/t-icon-bomb.png";

		try {
			spatial = shapeFactory.createImage(logoPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return spatial;
	}
	
	@Override
	public float getScale(Object object) {
		return 0.1f;
	}
	
	@Override
	public String getLabel(Object object) {
		return ((TAgent) object).getAID().getLocalName();
	}
	
	@Override
	public Font getLabelFont(Object object) {
		return new Font(Font.DIALOG, Font.PLAIN, 12);
	}
	
	@Override
	public Position getLabelPosition(Object object) {
		return Position.SOUTH;
	}
	
	@Override
	public float getLabelYOffset(Object object) {
		return 2.0f;
	}
	
	@Override
	public Color getLabelColor(Object object) {
		return Color.WHITE;
	}
}
