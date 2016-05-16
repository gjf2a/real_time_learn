package edu.hendrix.ev3.imgproc;

import edu.hendrix.ev3.ai.cluster.Clusterable;
import edu.hendrix.ev3.util.DeepCopyable;

public class Feature implements Clusterable<Feature>, DeepCopyable<Feature> {
	private int x, y;
	
	public Feature(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Feature add(Feature other) {
		return new Feature(this.x + other.x, this.y + other.y);
	}
	
	public int X() {return x;}
	public int Y() {return y;}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Feature) {
			Feature that = (Feature)other;
			return this.x == that.x && this.y == that.y;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return String.format("(%d,%d)", x, y);
	}
	
	@Override
	public int hashCode() {return x * 10000 + y;}
	
	public static long euclideanDistanceSquared(Feature f1, Feature f2) {
		int xDiff = f2.x - f1.x;
		int yDiff = f2.y - f1.y;
		return xDiff*xDiff + yDiff*yDiff;
	}
	
	public static double angle(Feature f1, Feature f2) {
		return Math.atan2(f2.Y() - f1.Y(), f2.X() - f1.X());
	}

	@Override
	public Feature weightedCentroidWith(Feature other, long thisCount, long otherCount) {
		long xNumer = x * thisCount + other.x * otherCount;
		long yNumer = y * thisCount + other.y * otherCount;
		long denom = thisCount + otherCount;
		return new Feature((int)(xNumer/denom), (int)(yNumer/denom));
	}

	@Override
	public Feature deepCopy() {
		return new Feature(this.x, this.y);
	}
}
