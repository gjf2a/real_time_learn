package edu.hendrix.ev3.remote.net.simplest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import edu.hendrix.ev3.ai.TrainingList;
import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.remote.Mover;
import edu.hendrix.ev3.remote.net.ToRobot;
import edu.hendrix.ev3.remote.net.RobotConstants;
import edu.hendrix.ev3.storage.VideoStorage;
import edu.hendrix.ev3.util.Logger;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.lcd.LCD;
import lejos.hardware.video.Video;

// Stores frames in memory and saves all at the end.
// Tends to run out of memory.
public class VideographyServer {
	
	public static void main(String[] args) {
		new VideographyServer().start();
	}
	
	public final static int MAX_FRAMES = 700;
	
	private boolean quit = false;
	private byte[] inputs = new byte[1];
	private byte[] frame;
	private GraphicsLCD g;
	private Move lastCommand = Move.NONE;
	private DatagramSocket msgSock;
	private int framesGrabbed = 0;
	private long startTime;
	private TrainingList examples = new TrainingList();
	private boolean controlStarted = false;
	
	public void start() {
		try {
			Video wc = setupVideo();
			setupSockets();
			System.out.println("Video ready!");
			LCD.clear();
			startTime = System.currentTimeMillis();
			while (!quit && !Button.ESCAPE.isDown() && framesGrabbed < MAX_FRAMES) {
				if (controlStarted) {
					wc.grabFrame(frame);
					framesGrabbed++;
					Logger.EV3Log.log(String.format("frame %d", framesGrabbed));
					AdaptedYUYVImage image = RobotConstants.setupFrom(frame);
					image.display(g, 0, 0, image.getMeanY());
					examples.add(lastCommand, new AdaptedYUYVImage(image));
				}
			}
			wc.close();
			Logger.EV3Log.log(String.format("%d images grabbed", framesGrabbed));
			LCD.clear();
			Mover.move(Move.STOP);
			System.out.printf("Saving %d images\n", framesGrabbed);
			VideoStorage.getEV3Storage().save(examples);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			quit = true;
			msgSock.close();
			long duration = System.currentTimeMillis() - startTime;
			double fps = 1000.0 * framesGrabbed / duration;
			Logger.EV3Log.log(String.format("Frames: %d\nDuration: %d\nFPS: %5.3f\n", framesGrabbed, duration, fps));
		}
	}
	
	private Video setupVideo() throws IOException {
		g = BrickFinder.getDefault().getGraphicsLCD();
		Video wc = RobotConstants.setupEV3Video();
		frame = wc.createFrame();
		return wc;
	}
	
	private void setupSockets() throws SocketException {
		msgSock = new DatagramSocket(RobotConstants.MSG_PORT);
		new CommThread().start();
	}
	
	private class CommThread extends Thread {
		
		@Override
		public void run() {
			try {
				DatagramPacket input = new DatagramPacket(inputs, 1);
				System.out.println("Socket ready!");
				while (!quit) {
					Logger.EV3Log.log("Waiting to receive");
					msgSock.receive(input);
					Logger.EV3Log.log("Received: " + ToRobot.from(inputs[0]));
					chooseAction(ToRobot.from(inputs[0]));
					Logger.EV3Log.log("Action chosen");
				}
				Logger.EV3Log.log("Exiting loop");
			} catch (SocketException e) {
				Logger.EV3Log.log("SocketException: " + e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				quit = true;
			}
		}

		private void chooseAction(ToRobot choice) throws IOException {
			controlStarted = true;
			if (choice == ToRobot.QUIT) {
				quit = true;
			} else if (choice.isMoveCmd()) {
				lastCommand = choice.getMove();
				Mover.move(lastCommand);
			} 
		}
	}
}
