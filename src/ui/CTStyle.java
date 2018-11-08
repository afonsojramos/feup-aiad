package ui;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import agents.CTAgent;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.Position;
import saf.v3d.scene.VSpatial;

public class CTStyle extends DefaultStyleOGL2D {
	
	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
		try {
			spatial = shapeFactory.createImage("src/assets/ct-icon.png");
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
		return ((CTAgent) object).getAID().getLocalName();
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
