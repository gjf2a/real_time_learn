package edu.hendrix.ev3.webcamdemo.bsoc;

import java.awt.image.BufferedImage;

import edu.hendrix.ev3.webcamdemo.ClusterableImage;

public interface BSOCRenderFunction {
	public void render(BufferedImage img, ClusterableImage ref, double frameRate, double trainingTime, int cluster);
}
