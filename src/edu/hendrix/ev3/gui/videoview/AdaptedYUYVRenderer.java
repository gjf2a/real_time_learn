package edu.hendrix.ev3.gui.videoview;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.imgproc.ImageOutline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class AdaptedYUYVRenderer implements ConcreteRenderer {
	private AdaptedYUYVImage img;
	
	public AdaptedYUYVRenderer(AdaptedYUYVImage img) {
		this.img = img;
	}

	public static void placeOnCanvas(AdaptedYUYVImage img, Canvas canv) {
		new AdaptedYUYVRenderer(img).draw(canv);
	}

	@Override
	public ImageOutline getOutline() {
		return img;
	}

	@Override
	public void renderLoop(GraphicsContext g, double cellWidth, double cellHeight) {
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				g.setFill(img.getRGBColor(x, y));
				g.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
			}
		}
	}
}
