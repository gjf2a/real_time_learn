package edu.hendrix.ev3.remote.net.simplest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.remote.Mover;
import edu.hendrix.ev3.remote.net.RobotConstants;
import edu.hendrix.ev3.remote.net.ToRobot;
import edu.hendrix.ev3.util.Logger;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;

public class SimpleMoveServer {
	
	public static void main(String[] args) {
		new SimpleMoveServer().start();
	}
	
	private boolean quit = false;
	private byte[] inputs = new byte[1];
	private Move lastCommand = Move.NONE;
	private DatagramSocket msgSock;
	private int framesGrabbed = 0;
	private long startTime;
	
	public void start() {
		try {
			setupSockets();
			LCD.clear();
			startTime = System.currentTimeMillis();
			while (!quit && !Button.ESCAPE.isDown()) {
			}
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
			if (choice == ToRobot.QUIT) {
				quit = true;
			} else if (choice.isMoveCmd()) {
				lastCommand = choice.getMove();
				Mover.move(lastCommand);
			} 
		}
	}
}
