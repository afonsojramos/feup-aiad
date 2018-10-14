package colorPicking;

import java.awt.Color;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;

public class ColorPickingAgentStyleOGL2D extends DefaultStyleOGL2D {
	
	public Color getColor(Object agent) {
		return ((ColorPickingAgent) agent).getColor();
	}
	
}
