package ui;

import java.io.IOException;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.ShapeFactory2D;
import saf.v3d.scene.VSpatial;

public class RadarStyle extends DefaultStyleOGL2D {
	
	@Override
	public void init(ShapeFactory2D factory) {
		super.init(factory);
	}
	
	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
		if (spatial == null) {
			try {
				spatial = shapeFactory.createImage("src/assets/dust2-radar.png");
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		return spatial;
	}
	
	@Override
	public float getScale(Object object) {
		return 0.75f;
	}
	
}
