package edu.hendrix.ev3.remote.sonar;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.SensorMode;

public class ThreeSonarTouchBot extends ThreeSonarBot {
	private EV3TouchSensor front;
	private SensorMode mode;
	private float[] samples = new float[1];
	private int numTouches, numCycles;
	
	public ThreeSonarTouchBot() {
		super();
		front = new EV3TouchSensor(SensorPort.S4);
		mode = front.getTouchMode();
		numTouches = numCycles = 0;
	}
	
	@Override
	public void pollSonars() {
		super.pollSonars();
		mode.fetchSample(samples, 0);
		numCycles += 1;
		if (wasPressed()) numTouches += 1;
	}
	
	public boolean wasPressed() {return samples[0] != 0;}
	
	@Override
	public float getLastDistance(SonarPosition sonar) {
		if (sonar == SonarPosition.CENTER && wasPressed()) {
			return 0.0f;
		} else {
			return super.getLastDistance(sonar);
		}
	}
	
	public int getNumTouches() {return numTouches;}
	public int getNumCycles() {return numCycles;}
	
	public static void main(String[] args) {
		new DisplaySonar(new ThreeSonarTouchBot()).loop();
	}
}
