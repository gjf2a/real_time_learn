package edu.hendrix.ev3.gui.videoview;

import java.util.ArrayList;

import javafx.scene.canvas.Canvas;

public class RenderList implements Renderer {
	private ArrayList<Renderer> renderers;
	
	public RenderList() {
		this.renderers = new ArrayList<>();
	}
	
	public void add(Renderer r) {
		renderers.add(r);
	}

	@Override
	public void draw(Canvas canv) {
		for (Renderer r: renderers) {
			r.draw(canv);
		}
	}
}
