package edu.hendrix.ev3.gui.videoview;

import edu.hendrix.ev3.imgproc.ImageOutline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public interface ConcreteRenderer extends Renderer {
	default public void draw(Canvas canv) {
		renderLoop(canv.getGraphicsContext2D(), canv.getWidth() / getOutline().getWidth(), canv.getHeight() / getOutline().getHeight());
	}
	
	public ImageOutline getOutline();
	
	public void renderLoop(GraphicsContext g, double cellWidth, double cellHeight);
}
