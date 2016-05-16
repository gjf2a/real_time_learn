package edu.hendrix.ev3.remote.net.imitation;

import java.nio.ByteBuffer;

public class StartupMessage extends UserMessage {
	private int maxNodes, shrinkFactor;
	
	public final static int MAX_SHRINK = Byte.MAX_VALUE;
	public final static int MAX_NODES = 65535;
	
	public final static int NUM_BYTES = 2 + 2 * Integer.BYTES;
	
	private StartupMessage(Mode mode, byte tag, int maxNodes, int shrinkFactor) {
		super(mode, tag);
		setup(maxNodes, shrinkFactor);
	}
	
	public StartupMessage(int maxNodes, int shrinkFactor) {
		super(Mode.START);
		setup(maxNodes, shrinkFactor);
	}
	
	public int getMaxNodes() {return maxNodes;}
	
	public int getShrinkFactor() {return shrinkFactor;}
	
	private void setup(int maxNodes, int shrinkFactor) {
		if (shrinkFactor > MAX_SHRINK) {
			throw new IllegalArgumentException("Can't shrink by more than " + MAX_SHRINK);
		}
		if (maxNodes > MAX_NODES) {
			throw new IllegalArgumentException("No more than " + MAX_NODES + " nodes");
		}
		this.maxNodes = maxNodes;
		this.shrinkFactor = shrinkFactor;
	}

	@Override
	public byte[] toBytes() {
		ByteBuffer result = ByteBuffer.allocate(UserMessage.NUM_BYTES);
		result.put((byte)(Mode.START.ordinal()));
		result.put(getTag());
		result.putInt(maxNodes);
		result.putInt(shrinkFactor);
		return result.array();
	}	
	
	public static StartupMessage fromBytes(byte[] bytes) {
		ByteBuffer input = ByteBuffer.wrap(bytes);
		Mode mode = Mode.values()[input.get()];
		byte tag = input.get();
		int maxNodes = input.getInt();
		int shrinkFactor = input.getInt();
		return new StartupMessage(mode, tag, maxNodes, shrinkFactor);
	}
	
	@Override
	public String toString() {return String.format("MaxNodes: %d ShrinkFactor: %d Tag: %d", maxNodes, shrinkFactor, getTag());}
	
	@Override
	public boolean equals(Object other) {
		return toString().equals(other.toString());
	}
}
