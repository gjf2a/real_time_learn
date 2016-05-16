package edu.hendrix.ev3.remote.sonar;

import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Set;

import edu.hendrix.ev3.remote.net.RobotConstants;

import java.util.Map.Entry;

public class SonarState implements Iterable<Entry<SonarPosition, Float>> {
	private EnumMap<SonarPosition,Float> sonarReadings = new EnumMap<>(SonarPosition.class);
	
	public final static int NUM_BYTES = SonarPosition.values().length * (1 + Float.BYTES);
	
	public SonarState() {}
	
	public SonarState(ThreeSonarBot bot) {
		for (SonarPosition sonar: SonarPosition.values()) {
			setReading(sonar, bot.getLastDistance(sonar));
		}
	}
	
	public SonarState(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		while (buffer.hasRemaining()) {
			SonarPosition pos = SonarPosition.values()[buffer.get()];
			float value = buffer.getFloat();
			setReading(pos, value);
		}
	}
	
	public byte[] toBytes() {
		ByteBuffer bytes = ByteBuffer.allocate(NUM_BYTES);
		for (SonarPosition pos: SonarPosition.values()) {
			bytes.put((byte)pos.ordinal());
			bytes.putFloat(sonarReadings.get(pos));
		}
		return bytes.array();
	}
	
	public SonarState(SonarState other) {
		sonarReadings.putAll(other.sonarReadings);
	}

	public SonarState(String src) {
		for (String positionStr: src.split(";")) {
			String[] parts = positionStr.split(":");
			setReading(SonarPosition.valueOf(parts[0]), Float.parseFloat(parts[1]));
		}
	}

	public void setReading(SonarPosition pos, float reading) {
		reading = Float.isFinite(reading) ? reading : RobotConstants.MAX_SONAR_DISTANCE;
		sonarReadings.put(pos, reading);
	}
	
	public float getReading(SonarPosition pos) {
		return sonarReadings.get(pos);
	}
	
	public boolean positionsMatch(Set<SonarPosition> positions) {
		return sonarReadings.keySet().equals(positions);
	}
	
	public boolean positionsMatch(SonarState other) {
		return positionsMatch(other.sonarReadings.keySet());
	}

	@Override
	public Iterator<Entry<SonarPosition, Float>> iterator() {
		return sonarReadings.entrySet().iterator();
	}
	
	@Override
	public String toString() {
		if (sonarReadings.size() == 0) {return "";}
		
		StringBuilder result = new StringBuilder();
		for (Entry<SonarPosition, Float> pos: sonarReadings.entrySet()) {
			result.append(pos.getKey());
			result.append(':');
			result.append(pos.getValue());
			result.append(';');
		}
		result.deleteCharAt(result.length() - 1);
		return result.toString();
	}
	
	@Override
	public int hashCode() {return toString().hashCode();}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof SonarState) {
			SonarState that = (SonarState)other;
			return this.sonarReadings.equals(that.sonarReadings);
		} else {
			return false;
		}
	}
}
