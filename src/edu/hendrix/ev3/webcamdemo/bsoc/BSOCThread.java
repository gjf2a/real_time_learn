package edu.hendrix.ev3.webcamdemo.bsoc;

import java.awt.image.BufferedImage;

import com.github.sarxos.webcam.Webcam;
//import com.github.sarxos.webcam.WebcamResolution;

import edu.hendrix.ev3.ai.bsoc.BoundedSelfOrgCluster;
import edu.hendrix.ev3.webcamdemo.ClusterableImage;
import javafx.application.Platform;

public class BSOCThread extends Thread {
	private BoundedSelfOrgCluster<ClusterableImage> bsoc;
	private int shrink;
	private boolean quit;
	private BSOCRenderFunction func;
	private BufferedImage img;
	
	public BSOCThread(int nodes, int shrinkFactor, BSOCRenderFunction func) {
		bsoc = new BoundedSelfOrgCluster<>(nodes, ClusterableImage::distance);
		this.shrink = shrinkFactor;
		this.func = func;
		quit = true;
	}
	
	@Override
	public void run() {
		quit = false;
		Webcam webcam = Webcam.getDefault();
		//webcam.setViewSize(WebcamResolution.VGA.getSize());
		webcam.open();
		int frames = 0;
		long start = System.currentTimeMillis();
		while (!quit) {
			if (webcam.isImageNew()) {
				img = webcam.getImage();
				if (shrink > 1) {
					BufferedImage shrunk = new BufferedImage(img.getWidth() / shrink, img.getHeight() / shrink, BufferedImage.TYPE_INT_ARGB);
					for (int x = 0; x < img.getWidth() / shrink; x++) {
						for (int y = 0; y < img.getHeight() / shrink; y++) {
							shrunk.setRGB(x, y, img.getRGB(x * shrink, y * shrink));
						}
					}
					img = shrunk;
				}
				frames += 1;
				long trainStart = System.currentTimeMillis();
				int node = bsoc.train(new ClusterableImage(img));
				long trainCall = System.currentTimeMillis() - trainStart;
				double rate = 1000.0 * frames / (System.currentTimeMillis() - start);
				
				Platform.runLater(() -> func.render(img, bsoc.getIdealInputFor(node), rate, trainCall / 1000.0, node));
			}
		}
		webcam.close();
	}
	
	public void quit() {
		quit = true;
	}
	
	public boolean isRunning() {return !quit;}
	
	public ClusterableImage getIdealInputFor(int node) {
		return bsoc.getIdealInputFor(node);
	}
	
	public int numNodes() {return bsoc.size();}
	
	public int getWidth() {
		return img.getWidth();
	}
	
	public int getHeight() {
		return img.getHeight();
	}
}
