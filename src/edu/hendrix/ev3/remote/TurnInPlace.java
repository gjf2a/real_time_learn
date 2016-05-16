package edu.hendrix.ev3.remote;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;

public class TurnInPlace {
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		Mover.move(Move.LEFT);
		while (!Button.ENTER.isDown());
		Mover.move(Move.STOP);
		LCD.drawString(String.format("%d ms", System.currentTimeMillis() - start), 0, 4);
		while (!Button.ESCAPE.isDown());
	}
}
