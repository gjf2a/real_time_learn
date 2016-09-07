package edu.hendrix.ev3.remote;

import lejos.hardware.Button;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.NXTRegulatedMotor;

public class HardCodedPathDebugger {
	GraphicsLCD gLCD;
	NXTRegulatedMotor LEFT_MOTOR, RIGHT_MOTOR;
	Move currentMove;
	boolean quit = false;
	public static void main(String[] args){
		new HardCodedPathDebugger().start();
	}
	public void start(){
		LEFT_MOTOR = Mover.LEFT_MOTOR;
		RIGHT_MOTOR = Mover.RIGHT_MOTOR;
		currentMove = Move.STOP;
		printDebugInfo();
		print("Press any",5);
		print("Direction to start!",6);
		while(currentMove.equals(Move.STOP)){
			currentMove = updatedMove(currentMove);
		}
		while(!quit && !Button.ESCAPE.isDown()){
			Mover.move(currentMove);
			if(currentMove != Move.STOP){
				printDebugInfo();
			} else if (tacCount(LEFT_MOTOR)
					+ tacCount(RIGHT_MOTOR) != 0){
				LEFT_MOTOR.resetTachoCount();
				RIGHT_MOTOR.resetTachoCount();
			}	
			currentMove = updatedMove(currentMove);
		}
	}
	public void printDebugInfo(){
		print("Left: " + tacCount(LEFT_MOTOR),2);
		print("Right: " + tacCount(RIGHT_MOTOR),3);
		print("Move: " + currentMove.uiName(),4);
	}
	
	public void print(String msg, int line) {
		LCD.drawString(msg + "          ", 0, line);
	}
	
	public Move updatedMove(Move current) {
		if (Button.UP.isDown()) {
			return Move.FORWARD;
		} else if (Button.LEFT.isDown()) {
			return Move.LEFT;
		} else if (Button.RIGHT.isDown()) {
			return Move.RIGHT;
		} else if (Button.DOWN.isDown()) {
			return Move.BACKWARD;
		} else if (Button.ENTER.isDown()) {
			return Move.STOP;
		} else {
			return current;
		}
	}
	private int tacCount(NXTRegulatedMotor m){
		int result = m.getTachoCount();
		return result < 0 ? -result :result;
	}
	
}
