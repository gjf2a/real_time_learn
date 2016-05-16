package edu.hendrix.ev3.webcamdemo;

import edu.hendrix.ev3.util.Duple;

public interface ConvolveFunction {
	public Duple<Integer,Integer> getIndex(int kernelIndex, int x, int y);
}
