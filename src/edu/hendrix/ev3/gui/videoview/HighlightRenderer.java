package edu.hendrix.ev3.gui.videoview;

import edu.hendrix.ev3.imgproc.BitImage;
import edu.hendrix.ev3.imgproc.Feature;
import edu.hendrix.ev3.imgproc.ImageOutline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class HighlightRenderer implements ConcreteRenderer {
	private BitImage highlights;
	
	public HighlightRenderer(BitImage highlights) {
		this.highlights = highlights;
	}

	@Override
	public ImageOutline getOutline() {
		return highlights;
	}

	@Override
	public void renderLoop(GraphicsContext g, double cellWidth, double cellHeight) {
		g.setFill(Color.RED);
		for (Feature f: highlights.allSet()) {
			g.fillRect(cellWidth * f.X(), cellHeight * f.Y(), cellWidth, cellHeight);
		}
	}
}
