package edu.hendrix.ev3.remote;
import java.io.IOException;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.ai.supervised.evaluable.BSOC32;
import edu.hendrix.ev3.ai.supervised.evaluable.BSOC64;
import edu.hendrix.ev3.remote.net.RobotConstants;
import edu.hendrix.ev3.util.Logger;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.lcd.LCD;
import lejos.hardware.video.Video;

public class SelfTrainBSOC64 {
	private GraphicsLCD g;
	private byte[] frame;
	private boolean quit = false;
	private boolean training = true;
	private int framesSinceTraining = 0;
	private BSOC32 bsoc;
	public static void main(String[] args){
		new SelfTrainBSOC64().setup();
	}
	
	public void setup(){
		bsoc = new BSOC32();
		int shrinkValue = 4;
		try {
			Video wc = setupVideo();
			System.out.println("Video ready!");
			LCD.clear();
			Move move = Move.STOP;
			Logger.EV3Log.format("BSOC 32 Shrink Value: %d", shrinkValue);
			while (!quit){
				move = updateMove(move);
				while(move == Move.STOP){
					move = updateMove(move);
					Mover.move(move);
				}
				
				wc.grabFrame(frame);
				AdaptedYUYVImage image = RobotConstants.setupFrom(frame);
				// image.display(g, 0, 0, image.getMeanY());
				if (training){
					long start = System.currentTimeMillis();
					train(image.shrunken(shrinkValue), move);
					long duration = System.currentTimeMillis() - start;
					Logger.EV3Log.format("%d ms %s", duration, move.name());
					Mover.move(move);
				}
				else{
					Logger.EV3Log.log("Free roam mode");
					Mover.move(freeRoam(image.shrunken(shrinkValue)));
				}
			}
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	public void train(AdaptedYUYVImage image, Move move){
		bsoc.train(image,move);
	}
	
	public Move freeRoam(AdaptedYUYVImage image){
		return bsoc.bestMatchFor(image);
	}
	
	private Video setupVideo() throws IOException {
		g = BrickFinder.getDefault().getGraphicsLCD();
		Video wc = RobotConstants.setupEV3Video();
		frame = wc.createFrame();
		return wc;
	}
	private Move updateMove(Move current) {
		if (Button.UP.isDown()) {
			return Move.FORWARD;
		} else if (Button.LEFT.isDown()) {
			return Move.LEFT;
		} else if (Button.RIGHT.isDown()) {
			return Move.RIGHT;
		} else if (Button.DOWN.isDown()) {
			return Move.BACKWARD;
		} else if (Button.ENTER.isDown()) {
			training = false;
			return Move.STOP;
		} else if (Button.ESCAPE.isDown()){
			quit = true;
			return Move.STOP;
		} else {
			return current;
		}
	}
	
}
