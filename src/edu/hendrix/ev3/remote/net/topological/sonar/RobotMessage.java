package edu.hendrix.ev3.remote.net.topological.sonar;

import java.nio.ByteBuffer;

import edu.hendrix.ev3.ai.topological.sonar.ClusterableSonarState;
import edu.hendrix.ev3.remote.net.TaggedMessage;
import edu.hendrix.ev3.remote.sonar.SonarState;
import edu.hendrix.ev3.util.Util;

public class RobotMessage implements TaggedMessage {
	private byte tag;
	private boolean initialized;
	private ClusterableSonarState currentState;
	
	public final static int NUM_BYTES = 2 + SonarState.NUM_BYTES;
	
	public RobotMessage(byte tag, boolean initialized, ClusterableSonarState currentState) {
		this.tag = tag;
		this.initialized = initialized;
		this.currentState = currentState;
	}
	
	public boolean isInitialized() {return initialized;}
	
	public ClusterableSonarState currentSonar() {return currentState;}

	@Override
	public byte getTag() {
		return tag;
	}

	@Override
	public byte[] toBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(NUM_BYTES);
		buffer.put(tag);
		buffer.put(Util.bool2byte(initialized));
		buffer.put(currentState.toBytes());
		return buffer.array();
	}
	
	@Override
	public int hashCode() {return toString().hashCode();}
	
	@Override
	public String toString() {
		return String.format("Tag:%d;Initialized:%s;Sonar:{%s}", tag, initialized ? "Yes" : "No", currentState.toString());
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof RobotMessage) {
			RobotMessage that = (RobotMessage)other;
			return this.tag == that.tag && this.initialized == that.initialized && this.currentState.equals(that.currentState);
		} else {
			return false;
		}
	}
	
	public RobotMessage(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		tag = buffer.get();
		initialized = Util.byte2bool(buffer.get());
		byte[] stateBytes = new byte[SonarState.NUM_BYTES];
		buffer.get(stateBytes);
		currentState = new ClusterableSonarState(stateBytes);
	}
}
