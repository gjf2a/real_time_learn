package edu.hendrix.ev3.remote;

import java.io.IOException;
import java.util.ArrayList;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.remote.net.RobotConstants;
import edu.hendrix.ev3.remote.net.actionselector.BSOCController;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.video.Video;

public class HardCodedPathFollower{
	ArrayList<MoveRotations> moveList;
	GraphicsLCD gLCD;
	byte[] frame;
	NXTRegulatedMotor LEFT_MOTOR, RIGHT_MOTOR;
	BSOCController ai;
	boolean quit = false;

	public static void main(String[] args){
		new HardCodedPathFollower().start();
	}
	public void start(){
		try{
			print("starting up",0);
			final int clusters = 16;
			final int shrink = 2;
			Video vid = setupVideo();
			LEFT_MOTOR = Mover.LEFT_MOTOR;
			RIGHT_MOTOR = Mover.RIGHT_MOTOR;
			moveList = generateMoves();
			ai = new BSOCController(clusters,shrink);
			print("C: " + clusters + " S: " + shrink,4);
			print("Learning!",0);
			int totalMovesCounted = 0;
			for(int x = 0; x < moveList.size(); x ++){
				LEFT_MOTOR.resetTachoCount();
				RIGHT_MOTOR.resetTachoCount();
				MoveRotations currentMR = moveList.get(x);
				while(currentMR.getLeftT() > tacCount(LEFT_MOTOR) ||
						currentMR.getRightT() > tacCount(RIGHT_MOTOR)){
					if(Button.ESCAPE.isDown()){
						quit = true;
						break;
					}
					Mover.move(currentMR.getMove());
					vid.grabFrame(frame);
					// Put learner here
					int node = ai.train(new AdaptedYUYVImage(frame,RobotConstants.WIDTH,RobotConstants.HEIGHT));
					ai.assignMoveFor(node, currentMR.getMove());
					totalMovesCounted++;
				}
			}
			Mover.move(Move.STOP);
			if(!quit){
				gLCD.clear();
				print("Done Training:",0);
				print("Frames Learned " + totalMovesCounted,1);
				print("Reset me and press UP!",2);
				while(!Button.UP.isDown()){}
				print("Press Escape",2);
				print("to stop me!",3);
				while(!Button.ESCAPE.isDown()){
					vid.grabFrame(frame);
					Move selectedMove = ai.pickMoveFor(
							new AdaptedYUYVImage(frame,RobotConstants.WIDTH,RobotConstants.HEIGHT));
					print(selectedMove.uiName(),4);
					Mover.move(selectedMove);
				}
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		} 
	}
	public void printDebugInfo(){
		print("Left: " + tacCount(LEFT_MOTOR),2);
		print("Right: " + tacCount(RIGHT_MOTOR),3);
	}
	private int tacCount(NXTRegulatedMotor m){
		int result = m.getTachoCount();
		return result < 0 ? -result :result;
	}
	
	private ArrayList<MoveRotations> generateMoves(){
		int ninetyDeg = 315;
		int squareLeg = 1300;
		// Modify This function to create your own path!
		ArrayList<MoveRotations> result = new ArrayList<>(10);
		result.add(new MoveRotations(Move.FORWARD,squareLeg));
		result.add(new MoveRotations(Move.LEFT,ninetyDeg));
		result.add(new MoveRotations(Move.FORWARD,squareLeg));
		result.add(new MoveRotations(Move.LEFT,ninetyDeg));
		result.add(new MoveRotations(Move.FORWARD,squareLeg));
		result.add(new MoveRotations(Move.LEFT,ninetyDeg));
		result.add(new MoveRotations(Move.FORWARD,squareLeg));
		result.add(new MoveRotations(Move.LEFT,ninetyDeg));
		result.add(new MoveRotations(Move.FORWARD,squareLeg));
		result.add(new MoveRotations(Move.LEFT,ninetyDeg));
		result.add(new MoveRotations(Move.FORWARD,squareLeg));
		result.add(new MoveRotations(Move.LEFT,ninetyDeg));
		result.add(new MoveRotations(Move.FORWARD,squareLeg));
		result.add(new MoveRotations(Move.LEFT,ninetyDeg));
		result.add(new MoveRotations(Move.FORWARD,squareLeg));
		result.add(new MoveRotations(Move.LEFT,ninetyDeg));

		return result;
	}
	public Video setupVideo() throws IOException {
		gLCD = BrickFinder.getDefault().getGraphicsLCD();
		Video wc = RobotConstants.setupEV3Video();
		frame = wc.createFrame();
		return wc;
	}
	public void print(String msg, int line) {
		LCD.drawString(msg + "          ", 0, line);
	}
	private class MoveRotations{
		private final Move move;
		private int leftT, rightT;
		public MoveRotations(Move move, int rot){
			if(move == Move.RIGHT){
				rightT = rot;
				leftT = 0;
			} else {
				leftT = rot;
				rightT = 0;
			}	
			this.move = move;
		}
		public Move getMove() {
			return move;
		}
		public int getRightT() {
			return rightT;
		}
		public int getLeftT() {
			return leftT;
		}
	}
}
