package edu.hendrix.ev3.remote;

public class StraightLine {
	public static void main(String[] args) throws InterruptedException {
		Mover.move(Move.FORWARD);
		Thread.sleep(5000);
		Mover.move(Move.STOP);
	}
}
