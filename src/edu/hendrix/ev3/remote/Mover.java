package edu.hendrix.ev3.remote;

import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;

public class Mover {	
	public final static NXTRegulatedMotor LEFT_MOTOR = Motor.A, RIGHT_MOTOR = Motor.D;
	
	public static void setSpeed(int degPerSec) {
		LEFT_MOTOR.setSpeed(degPerSec);
		RIGHT_MOTOR.setSpeed(degPerSec);
	}

	public static void move(Move m) {
		switch (m) {
		case FORWARD:
			setSpeed(360);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.forward();
			break;
		case LEFT:
			setSpeed(90);
			LEFT_MOTOR.backward();
			RIGHT_MOTOR.stop();
			break;
		case RIGHT:
			setSpeed(90);
			LEFT_MOTOR.stop(true);
			RIGHT_MOTOR.backward();
			break;
		case BACKWARD:
			setSpeed(90);
			LEFT_MOTOR.backward();
			RIGHT_MOTOR.backward();
			break;
		case STOP:
			LEFT_MOTOR.stop(true);
			RIGHT_MOTOR.stop();
			break;
		case NONE:
			break;
		case SPIN_LEFT:
			setSpeed(90);
			LEFT_MOTOR.backward();
			RIGHT_MOTOR.forward();
			break;
		case SPIN_RIGHT:
			setSpeed(90);
			LEFT_MOTOR.forward();
			RIGHT_MOTOR.backward();
			break;
		default:
			break;
		}
	}
}
