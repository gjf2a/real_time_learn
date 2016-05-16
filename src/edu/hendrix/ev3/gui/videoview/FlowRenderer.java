package edu.hendrix.ev3.gui.videoview;

import java.util.Map.Entry;

import edu.hendrix.ev3.imgproc.Feature;
import edu.hendrix.ev3.imgproc.FeatureFlow;
import edu.hendrix.ev3.imgproc.ImageOutline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class FlowRenderer implements ConcreteRenderer {
	private FeatureFlow ff;
	private ImageOutline outline;
	
	public FlowRenderer(ImageOutline outline, FeatureFlow ff) {
		this.ff = ff;
		this.outline = outline;
	}

	@Override
	public ImageOutline getOutline() {
		return outline;
	}

	@Override
	public void renderLoop(GraphicsContext g, double cellWidth, double cellHeight) {
		g.setStroke(Color.RED);
		g.setLineWidth(1.0);
		for (Entry<Feature, Feature> pair: ff) {
			g.strokeLine(pair.getKey().X() * cellWidth, pair.getKey().Y() * cellHeight, pair.getValue().X() * cellWidth, pair.getValue().Y() * cellHeight);
		}
	}
}
