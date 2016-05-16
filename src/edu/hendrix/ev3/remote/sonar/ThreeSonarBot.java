package edu.hendrix.ev3.remote.sonar;

import java.util.EnumMap;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

public class ThreeSonarBot {
	private EnumMap<SonarPosition,EV3UltrasonicSensor> sonars;
	private EnumMap<SonarPosition,SampleProvider> samplers;
	private float[] lastDistances;

	public ThreeSonarBot() {
		sonars = new EnumMap<>(SonarPosition.class);
		samplers = new EnumMap<>(SonarPosition.class);
		lastDistances = new float[SonarPosition.values().length];
		sonars.put(SonarPosition.LEFT, new EV3UltrasonicSensor(SensorPort.S1));
		sonars.put(SonarPosition.CENTER, new EV3UltrasonicSensor(SensorPort.S2));
		sonars.put(SonarPosition.RIGHT, new EV3UltrasonicSensor(SensorPort.S3));
		for (SonarPosition sonar: SonarPosition.values()) {
			sonars.get(sonar).enable();
			samplers.put(sonar, sonars.get(sonar).getDistanceMode());
		}
	}
	
	public void pollSonars() {
		for (SonarPosition sonar: SonarPosition.values()) {
			samplers.get(sonar).fetchSample(lastDistances, sonar.ordinal());
		}
	}

	public float getLastDistance(SonarPosition sonar) {
		return lastDistances[sonar.ordinal()];
	}
}