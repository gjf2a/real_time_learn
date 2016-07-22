package edu.hendrix.ev3.imgproc;

import java.util.BitSet;

public class Patch {
	private BitSet descriptor;
	
	Patch(BitSet bits) {
		this.descriptor = (BitSet)bits.clone();
	}
	
	public long maxDistance() {
		return descriptor.size();
	}
	
	public int distance(Patch other) {
		BitSet xored = (BitSet)descriptor.clone();
		xored.xor(other.descriptor);
		return xored.cardinality();
	}
}
