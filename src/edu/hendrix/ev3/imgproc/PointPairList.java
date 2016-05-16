package edu.hendrix.ev3.imgproc;

import java.util.BitSet;
import java.util.function.IntFunction;

public class PointPairList {
	private PointPair[] pointPairs;
	
	public PointPairList(int numPairs, IntFunction<PointPair> pairMaker) {
		pointPairs = new PointPair[numPairs];
		for (int i = 0; i < pointPairs.length; i++) {
			pointPairs[i] = pairMaker.apply(i);
		}
	}
	
	public <T extends ProcessableImage<T>> Patch makePatch(ProcessableImage<T> img, int x, int y) {
		BitSet bits = new BitSet(pointPairs.length);
		for (int i = 0; i < pointPairs.length; i++) {
			bits.set(i, pointPairs[i].test(img, x, y));
		}
		return new Patch(bits);
	}
}
